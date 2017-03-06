package com.jayway.annostatemachine;


public class OnExitRef {

    private final String mConnectionName;
    private final String mStateName;
    private boolean mRunOnMainThread;

    public OnExitRef(String stateName, String connectionName, boolean runOnMainThread) {
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
