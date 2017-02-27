package com.jayway.annostatemachine;

public interface UiThreadPoster {
    void runOnUiThread(Runnable runnable);
}
