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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ConnectionRef {

    public static final String WILDCARD = "*";
    public static final String AUTO = "!";

    private final LinkedList<ParameterRef> mParameters;
    private final String mName;
    private final String mFrom;
    private final String mTo;
    private final List<String> mSignals;
    private final boolean mRunOnMainThread;
    private final boolean mHasGuard;
    private final String mSignalsAsString;

    public String getName() {
        return mName;
    }

    public String getFrom() {
        return mFrom;
    }

    public String getTo() {
        return mTo;
    }

    public List<String> getSignals() {
        return mSignals;
    }

    public String getSignalsAsString() {
        return mSignalsAsString;
    }

    public boolean hasGuard() { return mHasGuard; }

    public ConnectionRef(String name, String from, String to, String signals, boolean runOnMainThread, boolean hasGuard, LinkedList<ParameterRef> parameters) {
        mName = name;
        mFrom = from;
        mTo = to;
        mSignalsAsString = signals;
        mSignals = collectSignals(signals);
        mRunOnMainThread = runOnMainThread;
        mHasGuard = hasGuard;
        mParameters = parameters;
    }

    private List<String> collectSignals(String signals) {
        ArrayList<String> signalsList = new ArrayList<>();
        String[] signalSplits = signals.split(",");
        for (String unTrimmedSignalString : signalSplits) {
            signalsList.add(unTrimmedSignalString.trim());
        }
        return signalsList;
    }

    @Override
    public String toString() {
        return mName + ": " + mFrom + " --" + mSignals + "--> " + mTo + (mHasGuard ? " has guard" : "");
    }

    public boolean getRunOnMainThread() {
        return mRunOnMainThread;
    }

    public LinkedList<ParameterRef> getParameters() {
        return mParameters;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ConnectionRef && mName.equals(((ConnectionRef) o).getName());

    }
}