package com.jayway.annostatemachine;

import java.util.HashMap;

public class SignalPayload {
    private HashMap<String, Boolean> mBooleanMap;
    private HashMap<String, Integer> mIntegerMap;

    public SignalPayload put(String key, Boolean value) {
        if (mBooleanMap == null) {
            mBooleanMap = new HashMap<>();
        }
        mBooleanMap.put(key, value);
        return this;
    }

    public SignalPayload put(String key, Integer value) {
        if(this.mIntegerMap == null) {
            this.mIntegerMap = new HashMap();
        }

        this.mIntegerMap.put(key, value);
        return this;
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        if (mBooleanMap == null) {
            return defaultValue;
        }
        Boolean mapValue = mBooleanMap.get(key);
        return mapValue == null ? defaultValue : mapValue;
    }

    public Integer getInt(String key, Integer defaultValue) {
        if (mIntegerMap == null) {
            return defaultValue;
        }
        Integer mapValue = mIntegerMap.get(key);
        return mapValue == null ? defaultValue : mapValue;
    }
}
