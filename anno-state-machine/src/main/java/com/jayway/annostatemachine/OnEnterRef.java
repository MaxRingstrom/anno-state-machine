package com.jayway.annostatemachine;


public class OnEnterRef {
    private String mStateName;
    private String mConnectionName;
    private boolean mRunOnUiThread;

    public OnEnterRef(String stateName, String connectionName, boolean runOnUiThread) {
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
