package com.jayway.annostatemachine.utils;

import com.jayway.annostatemachine.MainThreadPoster;

import java.util.concurrent.atomic.AtomicBoolean;

public class SynchronousMainThreadPoster implements MainThreadPoster {
        public AtomicBoolean mIsOnUiThreadNow = new AtomicBoolean();

        @Override
        public void runOnMainThread(Runnable runnable) {
            mIsOnUiThreadNow.set(true);
            runnable.run();
            mIsOnUiThreadNow.set(false);
        }

        public boolean isOnUiThreadNow() {
            return mIsOnUiThreadNow.get();
        }
    }
