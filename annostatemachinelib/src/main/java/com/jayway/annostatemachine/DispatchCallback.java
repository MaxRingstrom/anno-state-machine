package com.jayway.annostatemachine;

public interface DispatchCallback {
    void dispatchBlocking(Enum signal, SignalPayload payload);
}
