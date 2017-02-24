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
