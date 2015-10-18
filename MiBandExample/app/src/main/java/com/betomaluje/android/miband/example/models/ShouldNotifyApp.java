package com.betomaluje.android.miband.example.models;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

/**
 * Created by betomaluje on 8/12/15.
 */
public class ShouldNotifyApp {

    public String packageName;
    private ArrayList<ShouldNotifyApp> queueApps;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public ShouldNotifyApp(String packageName, ArrayList<ShouldNotifyApp> queueApps) {
        this.packageName = packageName;
        this.queueApps = queueApps;

        startTimer();
    }

    private void startTimer() {
        mHandler.postDelayed(mRunnable, 5000);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            queueApps.remove(ShouldNotifyApp.this);
        }
    };
}
