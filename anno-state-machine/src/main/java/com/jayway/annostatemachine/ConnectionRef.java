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

public class ConnectionRef {

    public static final String WILDCARD = "*";
    public static final String AUTO = "!";

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

    public boolean hasGuard() { return mHasGuard; }

    private final String mName;
    private final String mFrom;
    private final String mTo;
    private final String mSignal;
    private final boolean mRunOnMainThread;
    private final boolean mHasGuard;

    public ConnectionRef(String name, String from, String to, String signal, boolean runOnMainThread, boolean hasGuard) {
        mName = name;
        mFrom = from;
        mTo = to;
        mSignal = signal;
        mRunOnMainThread = runOnMainThread;
        mHasGuard = hasGuard;
    }

    @Override
    public String toString() {
        return mName + ": " + mFrom + " --" + mSignal + "--> " + mTo + (mHasGuard ? " has guard" : "");
    }

    public boolean getRunOnMainThread() {
        return mRunOnMainThread;
    }
}