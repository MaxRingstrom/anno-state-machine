package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.SignalDispatcher;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.DispatchCallback;


public class SharedBackgroundQueueDispatcher extends SignalDispatcher {
    public SharedBackgroundQueueDispatcher(DispatchCallback dispatchCallback) {
        super(dispatchCallback);
    }

    @Override
    public void dispatch(Enum signal, SignalPayload payload) {

    }
}
