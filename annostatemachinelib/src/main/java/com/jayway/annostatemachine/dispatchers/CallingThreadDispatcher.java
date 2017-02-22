package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.DispatchCallback;
import com.jayway.annostatemachine.SignalDispatcher;
import com.jayway.annostatemachine.SignalPayload;

public class CallingThreadDispatcher extends SignalDispatcher {
    public CallingThreadDispatcher(DispatchCallback implementation) {
        super(implementation);
    }

    @Override
    public void dispatch(Enum signal, SignalPayload payload) {
        dispatchBlocking(signal, payload);
    }
}
