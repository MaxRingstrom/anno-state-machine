package com.jayway.annostatemachine;

public interface DispatchCallback<SignalType extends Enum> {
    void dispatchBlocking(SignalType signal, SignalPayload payload);
}
