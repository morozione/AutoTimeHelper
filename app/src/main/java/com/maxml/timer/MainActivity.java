package com.maxml.timer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.maxml.timer.controllers.DbController;
import com.maxml.timer.entity.Events;
import com.maxml.timer.entity.ShowFragmentListener;
import com.maxml.timer.entity.ShowProgressListener;
import com.maxml.timer.entity.StatisticControl;
import com.maxml.timer.entity.User;
import com.maxml.timer.ui.fragments.DayCalendarFragment;
import com.maxml.timer.ui.fragments.GoogleMapFragment;
import com.maxml.timer.ui.fragments.HomeFragment;
import com.maxml.timer.ui.fragments.MainUserPageFragment;
import com.maxml.timer.ui.fragments.MonthCalendarFragment;
import com.maxml.timer.ui.fragments.SettingsFragment;
import com.maxml.timer.ui.fragments.WeekCalendarFragment;
import com.maxml.timer.util.Constants;
import com.maxml.timer.util.FragmentUtils;
import com.maxml.timer.util.ImageUtil;
import com.maxml.timer.util.NetworkUtil;
import com.maxml.timer.util.SharedPrefUtils;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ShowProgressListener, StatisticControl, ShowFragmentListener {

    private DrawerLayout drawerLayout;
    private ProgressBar pbLoad;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private LinearLayout statisticLayout;
    private TextView eventTime;

    private DbController dbController;
    private EventBus eventBus;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pbLoad = (ProgressBar) findViewById(R.id.pb_load);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        statisticLayout = (LinearLayout) findViewById(R.id.layoutStatistic);
        eventTime = (TextView) findViewById(R.id.tvTime);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        hideProgressBar();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        checkLocationPermission();

        initDrawer();
        initController();
        setHomeFragment();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.i_home:
                setupFragment(new HomeFragment());
                break;
            case R.id.i_calendar_month:
                setupFragment(new MonthCalendarFragment());
                break;
            case R.id.i_calendar_week:
                setupFragment(new WeekCalendarFragment());
                break;
            case R.id.i_calendar_day:
                setupFragment(new DayCalendarFragment());
                break;
            case R.id.i_user:
                setupFragment(new MainUserPageFragment());
                break;
            case R.id.i_map:
                setupFragment(new GoogleMapFragment());
                break;
            case R.id.i_setting:
                Log.d(Constants.TAG, "Select setting");
                setupFragment(new SettingsFragment());
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        eventBus.register(this);
        dbController.registerEventBus(eventBus);
        dbController.sentUser();
        super.onStart();
    }

    @Override
    protected void onStop() {
        dbController.unregisterEventBus(eventBus);
        eventBus.unregister(this);
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CODE_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (NetworkUtil.isNetworkAvailable(this)) {
                        if (data != null && data.getData() != null) {
                            loadImageFromGallery(data);
                        } else if (ImageUtil.fPhoto != null) {
                            loadImageFromCamera();
                        }
                        showProgressBar();
                        dbController.sentUser();
                    } else {
                        Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public void showStatisticLayout() {
        statisticLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideStatisticLayout() {
        statisticLayout.setVisibility(View.GONE);
    }

    @Override
    public void setEventTime(String time) {
        String text = getString(R.string.statistic_text);
        String displayText = text + " " + time;
        eventTime.setText(displayText);
    }

    @Override
    public void showFragment(Fragment fragment) {
        setupFragment(fragment);
    }


    @Subscribe
    public void onReceiveUser(User user) {
        initDrawerHeader(user);
    }

    @Subscribe
    public void onDatabaseEvent(Events.DbResult event) {
        switch (event.getResultStatus()) {
            case Constants.EVENT_DB_RESULT_OK:
                if (FragmentUtils.getCurrentFragment(this) instanceof MainUserPageFragment) {
                    ((MainUserPageFragment) FragmentUtils.getCurrentFragment(this))
                            .updateUI();

                    dbController.sentUser();

                    hideProgressBar();
                }
                break;
        }
    }

    @AfterPermissionGranted(Constants.REQUEST_LOCATION_PERMISSIONS)
    private void checkLocationPermission() {
        String[] perms = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.text_dialog_location_permission),
                    Constants.REQUEST_LOCATION_PERMISSIONS, perms);
        }
    }

    public void showProgressBar() {
        pbLoad.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        pbLoad.setVisibility(View.INVISIBLE);
    }

    public void setupFragment(Fragment fragment) {
        FragmentUtils.setFragment(this, fragment, fragment.getClass().getName());
    }

    private void initController() {
        eventBus = new org.greenrobot.eventbus.EventBus();
        dbController = new DbController(this, eventBus);
    }

    private void setHomeFragment() {
        FragmentUtils.setFragment(this, new HomeFragment());
    }

    private void initDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                dbController.sentUser();
            }
        };

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.setStatusBarBackground(R.color.primary_dark);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void initDrawerHeader(User user) {
        if (user == null) {
            return;
        }
        NavigationView nv = (NavigationView) findViewById(R.id.navigationView);
        View header = nv.getHeaderView(0);

        TextView name = (TextView) header.findViewById(R.id.user_name);
        ImageView icon = (ImageView) header.findViewById(R.id.profile_image);

        nv.setNavigationItemSelectedListener(this);

        user = SharedPrefUtils.getCurrentUser(this);
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            if (user.getUsername() != null) {
                name.setText(user.getUsername());
            } else name.setText(user.getEmail());
            if (user.getPhoto() != null && !user.getPhoto().equalsIgnoreCase("")) {
                if (user.getPhoto().contains("content")) {
                    Picasso.with(this)
                            .load(user.getPhoto())
                            .into(icon);
                } else {
                    File file = new File(user.getPhoto());

                    Picasso.with(this)
                            .load(file)
                            .into(icon);
                }
            }
        }
    }

    private void loadImageFromGallery(Intent data) {
        dbController.updateUserPhoto(data.getData().toString());
    }

    private void loadImageFromCamera() {
        dbController.updateUserPhoto(ImageUtil.fPhoto.toString());
    }
}