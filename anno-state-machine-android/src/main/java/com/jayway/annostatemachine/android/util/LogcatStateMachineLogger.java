package com.jayway.annostatemachine.android.util;

import android.util.Log;

import com.jayway.annostatemachine.utils.StateMachineLogger;

public class LogcatStateMachineLogger implements StateMachineLogger {

    @Override
    public void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    @Override
    public void e(String tag, String msg, Throwable t) {
        Log.e(tag, msg, t);
    }

    @Override
    public void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void w(String tag, String msg) {
        Log.w(tag, msg);
    }
}
