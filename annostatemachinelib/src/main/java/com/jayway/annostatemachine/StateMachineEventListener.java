package com.jayway.annostatemachine;

public interface StateMachineEventListener {
    void onDispatchingSignal(Object currentState, Object signal);
    void onUnhandledSignal(Object currentState, Object signal);
    void onChangingState(Object currentState, Object nextState);
}
