package com.jayway.annostatemachine;


public class OnExitRef {

    private final String mConnectionName;
    private final String mStateName;
    private boolean mRunOnUiThread;

    public OnExitRef(String stateName, String connectionName, boolean runOnUiThread) {
        mStateName = stateName;
        mConnectionName = connectionName;
        mRunOnUiThread= runOnUiThread;
    }

    public String getState() {
        return mStateName;
    }

    public String getConnectionName() {
        return mConnectionName;
    }

    public boolean getRunOnUiThread() {
        return mRunOnUiThread;
    }
}
