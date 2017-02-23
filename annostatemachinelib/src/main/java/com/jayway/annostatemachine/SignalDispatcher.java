package com.jayway.annostatemachine;

import com.jayway.annostatemachine.utils.StateMachineLogger;

import java.lang.ref.WeakReference;

public abstract class SignalDispatcher<SignalType extends Enum> {
    private final WeakReference<DispatchCallback<SignalType>> mStateMachineRef;
    private final StateMachineLogger mLogger;

    public SignalDispatcher(DispatchCallback<SignalType> dispatchCallback, StateMachineLogger logger) {
        mStateMachineRef = new WeakReference<>(dispatchCallback);
        mLogger = logger;
    }

    public abstract void dispatch(SignalType signal, SignalPayload<SignalType> payload);

    public DispatchCallback<SignalType> getCallback() {
        return mStateMachineRef.get();
    }

    protected StateMachineLogger getLogger() {
        return mLogger;
    }
}
