package com.maxml.timer.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.maxml.timer.R;
import com.maxml.timer.controllers.DbController;
import com.maxml.timer.entity.Action;
import com.maxml.timer.entity.Events;
import com.maxml.timer.entity.ShowFragmentListener;
import com.maxml.timer.entity.ShowProgressListener;
import com.maxml.timer.entity.StatisticControl;
import com.maxml.timer.entity.Table;
import com.maxml.timer.ui.adapter.CalendarDayAdapter;
import com.maxml.timer.ui.adapter.CalendarDayAdapterNew;
import com.maxml.timer.ui.adapter.CalendarDayTimeAdapter;
import com.maxml.timer.util.ActionUtils;
import com.maxml.timer.util.Constants;
import com.maxml.timer.util.OptionButtons;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DayCalendarFragmentNew extends Fragment implements CalendarDayAdapterNew.OnClickListener {

    private RecyclerView recyclerViewTime;
    private RecyclerView recyclerViewActions;

    private boolean isJoined;

    private List<OptionButtons> options = new ArrayList<>();
    private List<Action> list = new ArrayList<>();
    private CalendarDayAdapterNew adapter;
    private DbController controller;
    private EventBus eventBus;
    private Action lastAction;
    private Menu menu;

    private StatisticControl statisticControl;
    private ShowFragmentListener fragmentListener;
    private ShowProgressListener progressListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StatisticControl) {
            statisticControl = (StatisticControl) context;
        }
        if (context instanceof ShowFragmentListener) {
            fragmentListener = (ShowFragmentListener) context;
        }
        if (context instanceof ShowProgressListener) {
            progressListener = (ShowProgressListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerEventBus();
        initOptionButtons();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_calendar_new, container, false);
        recyclerViewActions = view.findViewById(R.id.recyclerView);
        recyclerViewTime = view.findViewById(R.id.recyclerViewTime);
        initRecyclerView();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
        controller.registerEventBus(eventBus);

        if (getArguments() != null) {
            loadActions(getArguments().getLong(Constants.EXTRA_TIME_ACTION));
        } else {
            loadActions(System.currentTimeMillis());
        }
    }

    @Override
    public void onStop() {
        eventBus.unregister(this);
        controller.unregisterEventBus(eventBus);
        if (statisticControl != null) {
            statisticControl.hideStatisticLayout();
        }

        progressListener.hideProgressBar();

        list.clear();
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.calendar_day_menu, menu);

        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.i_v_cancel:
                isJoined = false;
                changeMenuVisible();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickOption(OptionButtons optionType, Action item) {
        if (optionType == OptionButtons.EDIT) {
            Bundle args = new Bundle();
            args.putString(Constants.EXTRA_ID_ACTION, item.getId());
            DetailsActionFragment fragment = new DetailsActionFragment();
            fragment.setArguments(args);

            fragmentListener.showFragment(fragment);
        } else if (optionType == OptionButtons.DELETE) {
            controller.removeActionInDb(String.valueOf(item.getDayCount()), item.getId());

            progressListener.showProgressBar();
        } else if (optionType == OptionButtons.JOIN) {
            isJoined = true;
            lastAction = item;
            changeMenuVisible();
        }
        adapter.resetList();
    }

    @Override
    public void onClick(Action action) {
        if (isJoined) {
            adapter.resetList();
            if (action != lastAction) {
                Action newAction = ActionUtils.joinActions(lastAction, action);

                controller.removeActionInDb(String.valueOf(action.getDayCount()), action.getId());
                controller.updateActionInDb(newAction);
                progressListener.showProgressBar();
            } else {
                Toast.makeText(getContext(), R.string.message_two_identical_action, Toast.LENGTH_SHORT).show();
            }
            isJoined = false;
            changeMenuVisible();
        }
    }

    @Subscribe
    public void receiveTableFromDb(Table table) {
/*
        list.clear();
        list.addAll(table.getWorkList());
        list.addAll(table.getCallList());
        list.addAll(table.getRestList());
        list.addAll(table.getWalkList());

        updateUI();
        initStatistic();
*/
        progressListener.hideProgressBar();
    }

    @Subscribe
    public void onDatabaseEvent(Events.DbResult event) {
        switch (event.getResultStatus()) {
            case Constants.EVENT_DB_RESULT_OK:
                controller.getTableFromDb(new Date(System.currentTimeMillis()));
                break;
            case Constants.EVENT_DB_RESULT_ERROR:
                progressListener.hideProgressBar();
                break;
        }
    }

    private void loadActions(long time) {
        if (time == 0) {
            controller.getTableFromDb(new Date(System.currentTimeMillis()));
        } else {
            controller.getTableFromDb(new Date(time));
        }
        progressListener.showProgressBar();
    }

    private void updateUI() {
        adapter.swapData(list);
        initStatistic();
    }

    private void registerEventBus() {
        eventBus = new EventBus();
        controller = new DbController(getContext(), eventBus);
    }

    private void initStatistic() {
        if (statisticControl != null) {
            String time = getStatisticTime();
            statisticControl.setEventTime(time);
            statisticControl.showStatisticLayout();
        }
    }

    private String getStatisticTime() {
        long timeInMillis = 0;
        for (Action entity : list) {
            if (entity.getType().equalsIgnoreCase(Constants.EVENT_WORK_ACTION)) {
                Date startDate = entity.getStartDate();
                Date endDate = entity.getEndDate();
                long different = endDate.getTime() - startDate.getTime();
                timeInMillis += different;
            }
        }
        NumberFormat f = new DecimalFormat("00");
        long hours = timeInMillis / 1000 / 60 / 60;
        long min = timeInMillis / 1000 / 60 % 60;
        return f.format(hours) + ":" + f.format(min);
    }

    private void initRecyclerView() {
        //todo
        list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Action action = new Action();
            action.setType("item - " + i);
            action.setStartDate(new Date(20000000));
            action.setStartDate(new Date(20500000));
            list.add(action);
        }

        recyclerViewTime.setLayoutManager(new LinearLayoutManager(getContext()));
        CalendarDayTimeAdapter timeAdapter = new CalendarDayTimeAdapter(getContext(), list);
        recyclerViewTime.setAdapter(timeAdapter);
//        recyclerViewTime.setNestedScrollingEnabled(false);
//        recyclerViewTime.setHasFixedSize(true);

        recyclerViewActions.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        adapter = new CalendarDayAdapterNew(getContext(), list, options, this);
        recyclerViewActions.setAdapter(adapter);
//        recyclerViewActions.setNestedScrollingEnabled(false);
//        recyclerViewActions.setHasFixedSize(true);

        //rcv and leftrcv are two recyclerviews
        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (recyclerView == recyclerViewTime) {
                    recyclerViewActions.removeOnScrollListener(this);
                    recyclerViewActions.scrollBy(0, dy);
                    recyclerViewActions.addOnScrollListener(this);
                } else if (recyclerView == recyclerViewActions) {
                    recyclerViewTime.removeOnScrollListener(this);
                    recyclerViewTime.scrollBy(0, dy);
                    recyclerViewTime.addOnScrollListener(this);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        };

        recyclerViewTime.addOnScrollListener(scrollListener);
        recyclerViewActions.addOnScrollListener(scrollListener);
    }

    private void initOptionButtons() {
        options.add(OptionButtons.DELETE);
        options.add(OptionButtons.EDIT);
        options.add(OptionButtons.JOIN);
    }

    private void changeMenuVisible() {
        menu.setGroupVisible(R.id.g_cancel_option, isJoined);
    }
}
