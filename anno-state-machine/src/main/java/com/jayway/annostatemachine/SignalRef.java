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

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SignalRef)) {
            return false;
        }
        SignalRef castObj = (SignalRef) obj;
        if (mName != null) {
            return mName.equals(castObj.mName);
        } else if (castObj.mName == null) {
            return true;
        } else {
            return false;
        }
    }
}