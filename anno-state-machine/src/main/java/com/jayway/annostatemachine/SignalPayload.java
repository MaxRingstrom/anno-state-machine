/*
 * Copyright 2017 Jayway (http://www.jayway.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jayway.annostatemachine;

import java.util.HashMap;

public class SignalPayload<T extends Enum> {
    private HashMap<String, String> mStringMap;
    private HashMap<String, Boolean> mBooleanMap;
    private HashMap<String, Integer> mIntegerMap;
    private HashMap<String, Object> mObjectMap;
    private T mSignal;

    public SignalPayload put(String key, Boolean value) {
        if (mBooleanMap == null) {
            mBooleanMap = new HashMap<>();
        }
        mBooleanMap.put(key, value);
        return this;
    }

    public SignalPayload put(String key, Object value) {
        if (mObjectMap == null) {
            mObjectMap = new HashMap<>();
        }
        mObjectMap.put(key, value);
        return this;
    }

    public SignalPayload put(String key, String value) {
        if (mStringMap == null) {
            mStringMap = new HashMap<>();
        }
        mStringMap.put(key, value);
        return this;
    }

    public SignalPayload put(String key, Integer value) {
        if(mIntegerMap == null) {
            mIntegerMap = new HashMap<>();
        }

        mIntegerMap.put(key, value);
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

    public String getString(String key, String defaultValue) {
        if (mStringMap == null) {
            return defaultValue;
        }
        String mapValue = mStringMap.get(key);
        return mapValue == null ? defaultValue : mapValue;
    }

    public Object getObject(String key, Object defaultValue) {
        if (mObjectMap == null) {
            return defaultValue;
        }
        Object mapValue = mObjectMap.get(key);
        return mapValue == null ? defaultValue : mapValue;
    }

    // Intentionally set to package-private so that the signal is not modified by client code.
    void setSignal(T signal) {
        mSignal = signal;
    }

    public T getSignal() {
        return mSignal;
    }
}
