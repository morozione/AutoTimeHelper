package com.maxml.timer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.maxml.timer.entity.actions.CallAction;
import com.maxml.timer.entity.eventBus.CallMessage;
import com.maxml.timer.util.Constants;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

public class CallReceiver extends BroadcastReceiver {
    private CallAction call = new CallAction();

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming = false;

    public void onReceive(Context context, Intent intent) {
        int state = 0;
        String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
        if (stateStr != null) {
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
            }
        }
        onCallStateChanged(state, intent);
    }

    private void onCallStateChanged(int state, Intent intent) {
        if (lastState == state) {
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    // ring but not pick up, MISSING CALL
                    EventBus.getDefault().post(new CallMessage(Constants.EVENT_CALL_MISSING));
                } else {
                    if (isIncoming) {
                        //INCOMING CALL ENDED
                        EventBus.getDefault().post(new CallMessage(Constants.EVENT_CALL_INCOMING_ENDED));
                    } else {
                        //ONGOING CALL ENDED
                        EventBus.getDefault().post(new CallMessage(Constants.EVENT_CALL_ONGOING_ENDED));
                    }
                }
                break;

            case TelephonyManager.CALL_STATE_RINGING:
                // RINGING INCOMING CALL
                isIncoming = true;
                EventBus.getDefault().post(new CallMessage(Constants.EVENT_CALL_RINGING));
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                String phoneNumber;
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    // outgoing call answered
                    isIncoming = false;
                    // todo test
                    phoneNumber = intent.getExtras().getString("android.intent.extra.phone_number");
                    EventBus.getDefault().post(
                            new CallMessage(Constants.EVENT_CALL_ONGOING_ANSWERED, phoneNumber)
                    );
                } else {
                    // incoming call answered
                    isIncoming = true;
                    phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    EventBus.getDefault().post(
                            new CallMessage(Constants.EVENT_CALL_INCOMING_ANSWERED, phoneNumber)
                    );
                }
                break;
        }
        lastState = state;
    }
}
