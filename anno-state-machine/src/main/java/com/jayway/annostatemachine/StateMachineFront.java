package com.jayway.annostatemachine;

public interface StateMachineFront<SignalType> {
    void send(SignalType signal, SignalPayload payload);

    void send(SignalType signal);
}
