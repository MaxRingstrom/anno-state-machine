package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.DispatchCallback;
import com.jayway.annostatemachine.SignalDispatcher;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.utils.StateMachineLogger;

public class CallingThreadDispatcher<SignalType extends Enum> extends SignalDispatcher<SignalType> {

    private static final String TAG = CallingThreadDispatcher.class.getSimpleName();

    public CallingThreadDispatcher(DispatchCallback<SignalType> implementation, StateMachineLogger logger) {
        super(implementation, logger);
    }

    @Override
    public void dispatch(SignalType signal, SignalPayload<SignalType> payload) {
        DispatchCallback<SignalType> callback = getCallback();
        if (callback != null) {
            callback.dispatchBlocking(signal, payload);
        } else {
            getLogger().e(TAG, "Ignoring signal dispatch, callback is null");
        }
    }

    @Override
    public void shutDown() {
        // No necessary when we don't have a worker thread.
    }
}
