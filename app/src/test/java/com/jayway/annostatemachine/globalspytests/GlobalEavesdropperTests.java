package com.jayway.annostatemachine.globalspytests;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.globalspytests.generated.GlobalEavesdroppingMachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.jayway.annostatemachine.globalspytests.GlobalEavesdropperTests.GlobalEavesdroppingMachine.Signal;
import static com.jayway.annostatemachine.globalspytests.GlobalEavesdropperTests.GlobalEavesdroppingMachine.State;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class GlobalEavesdropperTests {

    @Mock
    StateMachineEventListener mMockEventListener;

    @Test
    public void testGlobalEavesdropperCalledBeforeAllNonEavesdroppers() {
        GlobalEavesdroppingMachineImpl stateMachine = spy(new GlobalEavesdroppingMachineImpl());
        stateMachine.init(State.INITIAL_STATE, mMockEventListener);
        stateMachine.send(Signal.ERROR);

        InOrder inOrder = Mockito.inOrder(stateMachine, mMockEventListener);
        inOrder.verify(stateMachine).globalEavesdropperOnError(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).onGlobalErrorWithSatisfiedGuard(Matchers.<SignalPayload>any());
        inOrder.verify(mMockEventListener).onChangingState(State.INITIAL_STATE, State.ERROR);
    }

    @StateMachine
    public static class GlobalEavesdroppingMachine {

        @Signals
        public enum Signal {
            START,
            ERROR
        }

        @States
        public enum State {
            INITIAL_STATE,
            STARTED,
            ERROR,
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", signal = "START")
        public boolean onStartSignal(SignalPayload payload) {
            return true;
        }

        // A global error connection with an unsatisfied guard that should only trigger if the current state does not handle
        // the error. This should also be true if the guards fail for the connections dealing with the
        // error event. Since the guard isn't satisfied a state transition should not occur.
        @Connection(from = "*", to = "ERROR", signal = "ERROR")
        public boolean onGlobalErrorWithSatisfiedGuard(SignalPayload payload) {
            return true;
        }

        // A Global eavesdropper that should be called for the ERROR signal even though a global
        // connection handles it.
        @Connection(from = "*", to = "*", signal = "ERROR")
        public boolean globalEavesdropperOnError(SignalPayload payload) {
            return true;
        }

    }
}
