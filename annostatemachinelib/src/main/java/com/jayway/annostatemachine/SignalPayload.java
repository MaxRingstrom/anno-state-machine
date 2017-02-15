package com.jayway.annostatemachine;

import java.util.HashMap;

public class SignalPayload {
    private HashMap<String, Boolean> mBooleanMap;

    public void put(String key, Boolean value) {
        if (mBooleanMap == null) {
            mBooleanMap = new HashMap<>();
        }
        mBooleanMap.put(key, value);
    }

    public boolean getBoolean(String key, Boolean defaultValue) {
        if (mBooleanMap == null) {
            return defaultValue;
        }
        return mBooleanMap.get(key);
    }
}
