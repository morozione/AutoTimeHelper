package com.maxml.timer.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxml.timer.R;
import com.maxml.timer.controllers.Controller;
import com.maxml.timer.entity.Table;
import com.maxml.timer.entity.eventBus.Events;
import com.maxml.timer.util.Constants;
import com.maxml.timer.util.EventBusType;
import com.maxml.timer.util.FragmentUtils;
import com.maxml.timer.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;

public class CalendarFragment extends Fragment implements View.OnClickListener {

    private Controller controller;
    private EventBus eventBus;
    private CalendarView calendarView;
    private TextView callsCount;
    private TextView restCount;
    private TextView workCount;
    private TextView walkCount;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        eventBus = new EventBus();
        controller = new Controller(getContext(),eventBus);
        initUI(view);
        initListener();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // register EventBus
        eventBus.register(this);
        controller.registerEventBus(eventBus);
        // get current date
        Date selectedDate = new Date(calendarView.getDate());
        controller.getTableFromDb(selectedDate);
    }


    @Subscribe
    public void receiveTableFromDb(Table table) {
        if (table != null) {
            updateUI(table);
        }
    }

    @Subscribe()
    public void getDbMessage(Events.DbResult result) {
        switch (result.getResultStatus()) {
            case Constants.EVENT_DB_RESULT_OK:
                break;
            case Constants.EVENT_DB_RESULT_ERROR:
                Toast.makeText(getActivity(),R.string.db_error,Toast.LENGTH_LONG).show();
                break;
        }
    }


    private void updateUI(Table table) {
        int calls = 0;
        int work = 0;
        int rests = 0;
        int walks = 0;
        if (table != null) {
            calls = table.getCallList().size();
            walks = table.getWalkList().size();
            rests = table.getRestList().size();
            work = table.getWorkList().size();
        }
        callsCount.setText(calls + "");
        workCount.setText(work + "");
        walkCount.setText(walks + "");
        restCount.setText(rests + "");
    }

    @Override
    public void onClick(View view) {
        ActionListViewFragment sliceFragment = new ActionListViewFragment();
        FragmentUtils.setFragment(getActivity(), sliceFragment);
    }

    @Override
    public void onStop() {
        controller.unregisterEventBus(eventBus);
        eventBus.unregister(this);
        super.onStop();
    }


    private void initUI(View view) {
        calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        callsCount = (TextView) view.findViewById(R.id.tvCallsCount);
        restCount = (TextView) view.findViewById(R.id.tvvtRestCount);
        workCount = (TextView) view.findViewById(R.id.tvWorkCount);
        walkCount = (TextView) view.findViewById(R.id.tvWalkCount);
    }

    private void initListener() {
        callsCount.setOnClickListener(this);
        walkCount.setOnClickListener(this);
        workCount.setOnClickListener(this);
        restCount.setOnClickListener(this);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                controller.getTableFromDb(Utils.getDate(dayOfMonth, month, year));
            }
        });
    }
}