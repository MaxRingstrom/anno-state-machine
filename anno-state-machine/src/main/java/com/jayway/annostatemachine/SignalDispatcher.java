package com.jayway.annostatemachine;

import com.jayway.annostatemachine.utils.StateMachineLogger;

public abstract class SignalDispatcher {

    public SignalDispatcher() {
    }

    public abstract void dispatch(Enum signal, SignalPayload payload, DispatchCallback callback, StateMachineLogger logger);

    public abstract void shutDown();
}
