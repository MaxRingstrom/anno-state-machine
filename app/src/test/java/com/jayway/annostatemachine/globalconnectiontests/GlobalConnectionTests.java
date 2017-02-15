package com.jayway.annostatemachine.globalconnectiontests;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
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

    @Test
    public void testGlobalConnectionsNotCalledIfExplicitConnectionHandlesSignal() {
        GlobalMachineImpl stateMachine = spy(new GlobalMachineImpl());
        stateMachine.init(GlobalMachineImpl.State.INITIAL_STATE, mMockEventListener);

        // Move to started state
        stateMachine.send(GlobalMachineImpl.Signal.START);
        stateMachine.send(GlobalMachineImpl.Signal.ERROR);

        // The explicit error handling connection should get called but not the global ones.
        InOrder inOrder = Mockito.inOrder(stateMachine, mMockEventListener);
        inOrder.verify(stateMachine).onNon404ErrorWhenStarted((SignalPayload) any());
        inOrder.verify(mMockEventListener).onChangingState(GlobalMachine.State.STARTED, GlobalMachine.State.ERROR_WHEN_STARTED);

        verify(stateMachine, never()).onGlobalErrorWithUnsatisfiedGuard(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).onGlobalErrorWithSatisfiedGuard(Matchers.<SignalPayload>any());
    }

    @Test
    public void testGlobalConnectionsCalledIfExplicitConnectionGuardIsUnsatisfied() {
        GlobalMachineImpl stateMachine = spy(new GlobalMachineImpl());
        stateMachine.init(GlobalMachineImpl.State.INITIAL_STATE, mMockEventListener);

        // Move to started state
        stateMachine.send(GlobalMachineImpl.Signal.START);

        // Send a ERROR signal with code ERROR_CODE_NOT_FOUND that fails the explicit connection's
        // guard.
        stateMachine.send(GlobalMachineImpl.Signal.ERROR,
                new SignalPayload()
                        .put(GlobalMachine.KEY_ERROR_CODE, GlobalMachine.ERROR_CODE_NOT_FOUND));

        // The explicit error handling connection should get called but its guard is not satisfied.
        // This should lead to the global ones being called.
        InOrder inOrder = Mockito.inOrder(stateMachine, mMockEventListener);
        inOrder.verify(stateMachine).onNon404ErrorWhenStarted((SignalPayload) any());
        inOrder.verify(stateMachine).onGlobalErrorWithUnsatisfiedGuard((SignalPayload) any());
        inOrder.verify(stateMachine).onGlobalErrorWithSatisfiedGuard((SignalPayload) any());
        inOrder.verify(mMockEventListener).onChangingState(GlobalMachine.State.STARTED, GlobalMachine.State.ERROR);

        verify(mMockEventListener, never()).onChangingState(GlobalMachine.State.STARTED, GlobalMachine.State.ERROR_WHEN_STARTED);
    }

    @StateMachine
    public static class GlobalMachine {

        public static final String KEY_ERROR_CODE = "error_code";
        public static final int ERROR_CODE_NOT_FOUND = 404;

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
            ERROR_WHEN_STARTED
        }

        @Connection(from = "INITIAL_STATE", to="STARTED", signal = "START")
        public boolean onStartSignal(SignalPayload payload) {
            // Non eavesdropping connection that should be called last and should result
            // in a state switch to the STARTED state.
            return true;
        }

        @Connection(from = "STARTED", to="ERROR_WHEN_STARTED", signal = "ERROR")
        public boolean onNon404ErrorWhenStarted(SignalPayload payload) {
            // An explicit connection for non ERROR_CODE_NOT_FOUND errors. If a ERROR_CODE_NOT_FOUND
            // is received the guard will fail.
            return !(payload != null && payload.getInt(KEY_ERROR_CODE, -1) == ERROR_CODE_NOT_FOUND);
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
