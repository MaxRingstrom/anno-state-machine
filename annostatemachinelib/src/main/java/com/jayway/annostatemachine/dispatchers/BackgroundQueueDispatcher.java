package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.SignalDispatcher;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.DispatchCallback;

public class BackgroundQueueDispatcher extends SignalDispatcher {
    public BackgroundQueueDispatcher(DispatchCallback dispatchCallback) {
        super(dispatchCallback);
    }

    @Override
    public void dispatch(Enum signal, SignalPayload payload) {

    }
}
