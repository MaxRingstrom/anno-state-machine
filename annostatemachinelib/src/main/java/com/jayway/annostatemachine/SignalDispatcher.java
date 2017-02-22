package com.jayway.annostatemachine;

public abstract class SignalDispatcher<SignalType extends Enum> {
    private final DispatchCallback mStateMachine;

    public SignalDispatcher(DispatchCallback dispatchCallback) {
        mStateMachine = dispatchCallback;
    }

    public abstract void dispatch(SignalType signal, SignalPayload<SignalType> payload);

    protected void dispatchBlocking(Enum signal, SignalPayload payload) {
        mStateMachine.dispatchBlocking(signal, payload);
    }
}
