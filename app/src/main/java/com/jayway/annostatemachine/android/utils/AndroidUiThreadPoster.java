package com.jayway.annostatemachine.android.utils;

import android.content.Context;
import android.os.Handler;

import com.jayway.annostatemachine.UiThreadPoster;

public class AndroidUiThreadPoster implements UiThreadPoster {
    private final Handler mHandler;

    public AndroidUiThreadPoster(Context context) {
        mHandler = new Handler(context.getApplicationContext().getMainLooper());
    }

    @Override
    public void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }
}
