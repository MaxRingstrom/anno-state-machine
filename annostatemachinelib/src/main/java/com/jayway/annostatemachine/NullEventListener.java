package com.jayway.annostatemachine;

public class NullEventListener implements StateMachineEventListener {
    @Override
    public void onDispatchingSignal(Object currentState, Object signal) {

    }

    @Override
    public void onUnhandledSignal(Object currentState, Object signal) {

    }

    @Override
    public void onChangingState(Object currentState, Object nextState) {

    }
}
