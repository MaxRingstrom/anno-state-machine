package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.DispatchCallback;
import com.jayway.annostatemachine.SignalDispatcher;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.utils.StateMachineLogger;


public class SharedBackgroundQueueDispatcher extends SignalDispatcher {
    public SharedBackgroundQueueDispatcher(DispatchCallback dispatchCallback, StateMachineLogger logger) {
        super(dispatchCallback, logger);
    }

    @Override
    public void dispatch(Enum signal, SignalPayload payload) {

    }

    @Override
    public void shutDown() {

    }
}
