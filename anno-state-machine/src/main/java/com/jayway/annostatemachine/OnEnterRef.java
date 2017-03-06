package com.jayway.annostatemachine;


public class OnEnterRef {
    private String mStateName;
    private String mConnectionName;
    private boolean mRunOnMainThread;

    public OnEnterRef(String stateName, String connectionName, boolean runOnMainThread) {
        mStateName = stateName;
        mConnectionName = connectionName;
        mRunOnMainThread = runOnMainThread;
    }

    public String getState() {
        return mStateName;
    }

    public String getConnectionName() {
        return mConnectionName;
    }

    public boolean getRunOnMainThread() {
        return mRunOnMainThread;
    }
}
