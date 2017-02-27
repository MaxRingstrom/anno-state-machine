package com.jayway.annostatemachine;

public class ConnectionRef {

    public static final String WILDCARD = "*";

    public String getName() {
        return mName;
    }

    public String getFrom() {
        return mFrom;
    }

    public String getTo() {
        return mTo;
    }

    public String getSignal() {
        return mSignal;
    }

    private final String mName;
    private final String mFrom;
    private final String mTo;
    private final String mSignal;
    private final boolean mRunOnUiThread;

    public ConnectionRef(String name, String from, String to, String signal, boolean runOnUiThread) {
        mName = name;
        mFrom = from;
        mTo = to;
        mSignal = signal;
        mRunOnUiThread = runOnUiThread;
    }

    @Override
    public String toString() {
        return mName + ": " + mFrom + " --" + mSignal + "--> " + mTo;
    }

    public boolean getRunOnUiThread() {
        return mRunOnUiThread;
    }
}