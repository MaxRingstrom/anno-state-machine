package com.jayway.annostatemachine;

public class StateRef {
    private final String mName;

    public StateRef(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName.toUpperCase();
    }

    public String getName() {
        return mName;
    }
}