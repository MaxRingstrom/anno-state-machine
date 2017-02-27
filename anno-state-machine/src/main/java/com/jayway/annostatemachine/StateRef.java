package com.jayway.annostatemachine;

public class StateRef {
    private final String mName;

    public StateRef(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }

    public String getName() {
        return mName;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StateRef)) {
            return false;
        }
        StateRef castObj = (StateRef)obj;
        if (mName != null) {
            return mName.equals(castObj.mName);
        } else if (castObj.mName == null) {
            return true;
        } else {
            return false;
        }
    }
}