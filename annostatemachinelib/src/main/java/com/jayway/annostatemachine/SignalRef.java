package com.jayway.annostatemachine;

public class SignalRef {
    String mName;

    public SignalRef(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }
}