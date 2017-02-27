package com.jayway.annostatemachine;


public class NoOpUiThreadPoster implements UiThreadPoster {

    public NoOpUiThreadPoster() {
    }

    @Override
    public void runOnUiThread(Runnable runnable) {
        throw new IllegalStateException("Missing UI thread poster. Did you forget to pass one to init()? You must provide a ui thread poster if you specify that a connection method should run on the UI thread");
    }
}
