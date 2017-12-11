package com.maxml.timer.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.maxml.timer.R;
import com.maxml.timer.api.UserAPI;

/**
 * Created by Lantar on 22.04.2015.
 */
public class ForgotPasswordActivity extends Activity {

    protected static final int CONNECTION_OK = 1;
    private TextView tvEmail;
    private ProgressBar progressBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        tvEmail = (TextView) findViewById(R.id.textFPEmail);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
    }

    public void onClick(View v) {
        progressBar.setVisibility(View.VISIBLE);
        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                progressBar.setVisibility(View.INVISIBLE);
                if (msg.what == CONNECTION_OK) {
                    Toast.makeText(getApplicationContext(),
                            "An email was successfully sent with reset instructions.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong email.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        UserAPI c = new UserAPI(this, handler);
        c.sentPassword(tvEmail.getText().toString());
    }
}
