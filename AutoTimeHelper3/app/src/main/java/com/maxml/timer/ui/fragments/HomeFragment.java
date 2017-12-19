package com.maxml.timer.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.maxml.timer.R;
import com.maxml.timer.controllers.Controller;
import com.maxml.timer.entity.eventBus.Events;
import com.maxml.timer.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment {

    private EventBus eventBus;
    private Controller controller;
    private TextView actionDate;
    private TextView actionStatuse;
    private ToggleButton butCall;
    private ToggleButton butWork;
    private ToggleButton butRest;
    private ToggleButton butWalk;

    public HomeFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manual_activity_fragment, container, false);
        butCall = (ToggleButton) view.findViewById(R.id.butCall);
        butWork = (ToggleButton) view.findViewById(R.id.butWork);
        butRest = (ToggleButton) view.findViewById(R.id.butRest);
        butWalk = (ToggleButton) view.findViewById(R.id.butWalk);
        actionDate = (TextView) view.findViewById(R.id.title);
        actionStatuse = (TextView) view.findViewById(R.id.description);
        eventBus = new EventBus();
        controller = new Controller(getContext(), eventBus);
        initListeners();
        return view;
    }

    @Subscribe()
    public void onReceiveStatusEvent(Events.ActionStatus event) {
        actionStatuse.setText(event.getActionStatus());
        Date time = event.getActionTime();
        if (time == null) {
            actionDate.setText(getString(R.string.widget_default_text));
        } else {
            actionDate.setText(charSequence(time));
        }
    }

    @Override
    public void onStart() {
        super.onStart();


        refreshActionStatus();
        eventBus.register(this);
        controller.registerEventBus(eventBus);
    }

    @Override
    public void onStop() {
        controller.unregisterEventBus(eventBus);
        eventBus.unregister(this);
        super.onStop();
    }

    private void refreshActionStatus() {
        String action = controller.getActionStatus();
        Date time = controller.getActionTime();
        actionStatuse.setText(action);
        if (time == null) {
            actionDate.setText(getString(R.string.widget_default_text));
        } else {
            actionDate.setText(charSequence(time));
        }
    }

    private void initListeners() {
        butCall.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                butWork.setChecked(false);
                butWalk.setChecked(false);
                butRest.setChecked(false);
                controller.callActionEvent();
            }
        });
        butWork.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                butCall.setChecked(false);
                butWalk.setChecked(false);
                butRest.setChecked(false);
                controller.workActionEvent();
            }
        });
        butRest.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                butCall.setChecked(false);
                butWalk.setChecked(false);
                butWork.setChecked(false);
                controller.restActionEvent();
            }
        });
        butWalk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                butCall.setChecked(false);
                butRest.setChecked(false);
                butWork.setChecked(false);
                controller.walkActionEvent();
            }
        });

    }

    private String charSequence(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
        String currentDateAndTime = "Start at:" + sdf.format(date);
        return currentDateAndTime;
    }

}
