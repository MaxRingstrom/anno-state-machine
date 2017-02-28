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

package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.DispatchCallback;
import com.jayway.annostatemachine.SignalDispatcher;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.utils.StateMachineLogger;

public class CallingThreadDispatcher extends SignalDispatcher {

    private static final String TAG = CallingThreadDispatcher.class.getSimpleName();

    public CallingThreadDispatcher() {
        super();
    }

    @Override
    public void dispatch(Enum signal, SignalPayload payload, DispatchCallback callback, StateMachineLogger logger) {
        if (callback != null) {
            callback.dispatchBlocking(signal, payload);
        } else {
            logger.e(TAG, "Ignoring signal dispatch, callback is null");
        }
    }

    @Override
    public void shutDown() {
        // No necessary when we don't have a worker thread.
    }
}
