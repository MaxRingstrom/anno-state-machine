package com.jayway.annostatemachine;

import com.jayway.annostatemachine.utils.StateMachineLogger;

import java.lang.ref.WeakReference;

public abstract class SignalDispatcher {
    private final WeakReference<DispatchCallback> mStateMachineRef;
    private final StateMachineLogger mLogger;

    public SignalDispatcher(DispatchCallback dispatchCallback, StateMachineLogger logger) {
        mStateMachineRef = new WeakReference<>(dispatchCallback);
        mLogger = logger;
    }

    public abstract void dispatch(Enum signal, SignalPayload<Enum> payload);

    protected DispatchCallback getCallback() {
        return mStateMachineRef.get();
    }

    protected WeakReference<DispatchCallback> getCallbackRef() {
        return mStateMachineRef;
    }

    protected StateMachineLogger getLogger() {
        return mLogger;
    }

    public abstract void shutDown();
}
