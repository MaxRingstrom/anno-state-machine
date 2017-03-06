package com.jayway.annostatemachine.android.util;

import android.content.Context;
import android.os.Handler;

import com.jayway.annostatemachine.MainThreadPoster;

public class AndroidMainThreadPoster implements MainThreadPoster {
    private final Handler mHandler;

    public AndroidMainThreadPoster(Context context) {
        mHandler = new Handler(context.getApplicationContext().getMainLooper());
    }

    @Override
    public void runOnMainThread(Runnable runnable) {
        mHandler.post(runnable);
    }
}
