package com.jayway.annostatemachine.globalconnectiontests;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.eavesdroppertests.generated.MixedEavesDropAndNormalConnectionsMachineImpl;
import com.jayway.annostatemachine.eavesdroppertests.generated.SimpleEavesDroppingMachineImpl;
import com.jayway.annostatemachine.globalconnectiontests.generated.GlobalMachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GlobalConnectionTests {

    @Mock
    StateMachineEventListener mMockEventListener;

    @Test
    public void testGlobalConnectionCalledIfNoExplicitConnection() {
        GlobalMachineImpl stateMachine = spy(new GlobalMachineImpl());
        stateMachine.init(GlobalMachineImpl.State.INITIAL_STATE, mMockEventListener);
        stateMachine.send(GlobalMachineImpl.Signal.ERROR);
        verify(stateMachine).onGlobalErrorWithUnsatisfiedGuard(Matchers.<SignalPayload>any());
        verify(mMockEventListener).onChangingState(GlobalMachine.State.INITIAL_STATE, GlobalMachine.State.ERROR);
    }

    @Test
    public void testUnsatisfiedGlobalConnectionPassesOnSignal() {
        GlobalMachineImpl stateMachine = spy(new GlobalMachineImpl());
        stateMachine.init(GlobalMachineImpl.State.INITIAL_STATE, mMockEventListener);
        stateMachine.send(GlobalMachineImpl.Signal.ERROR);
        InOrder inOrder = Mockito.inOrder(stateMachine, mMockEventListener);
        inOrder.verify(stateMachine).onGlobalErrorWithUnsatisfiedGuard(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).onGlobalErrorWithSatisfiedGuard(Matchers.<SignalPayload>any());
        inOrder.verify(mMockEventListener).onChangingState(GlobalMachine.State.INITIAL_STATE, GlobalMachine.State.ERROR);
    }

    @StateMachine
    public static class GlobalMachine {

        @Signals
        public enum Signal {
            START,
            ERROR
        }

        @States
        public enum State {
            INITIAL_STATE,
            STARTED,
            ERROR
        }

        @Connection(from = "INITIAL_STATE", to="STARTED", signal = "START")
        public boolean onStartSignal(SignalPayload payload) {
            // Non eavesdropping connection that should be called last and should result
            // in a state switch to the STARTED state.
            return true;
        }

        // A global error connection with an unsatisfied guard that should only trigger if the current state does not handle
        // the error. This should also be true if the guards fail for the connections dealing with the
        // error event. Since the guard isn't satisfied a state transition should not occur.
        @Connection(from = "*", to="ERROR", signal = "ERROR")
        public boolean onGlobalErrorWithUnsatisfiedGuard(SignalPayload payload) {
            return false;
        }

        // A global error connection that should only trigger if the current state does not handle
        // the error. This should also be true if the guards fail for the connections dealing with the
        // error event. Since the guard is satisfied a state transition should occur if it is triggered.
        @Connection(from = "*", to="ERROR", signal = "ERROR")
        public boolean onGlobalErrorWithSatisfiedGuard(SignalPayload payload) {
            return true;
        }

    }
}
