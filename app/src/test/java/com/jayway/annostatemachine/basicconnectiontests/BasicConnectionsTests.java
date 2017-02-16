package com.jayway.annostatemachine.basicconnectiontests;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.basicconnectiontests.generated.GuardedMachineImpl;
import com.jayway.annostatemachine.basicconnectiontests.generated.MultiConnectionsWithSameSignalMachineImpl;
import com.jayway.annostatemachine.basicconnectiontests.generated.UnguardedMachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BasicConnectionsTests {

    @Mock
    StateMachineEventListener mMockEventListener;

    @Test(expected = IllegalStateException.class)
    public void testIllegalStateExceptionThrownIfInitNotCalled() {
        UnguardedMachineImpl stateMachine = new UnguardedMachineImpl();
        stateMachine.send(UnguardedMachine.Signal.START);
    }

    @Test
    public void testFullySpecifiedConnectionFromInitialStateGoesToTargetState() {
        UnguardedMachineImpl stateMachine = spy(new UnguardedMachineImpl());
        stateMachine.init(UnguardedMachine.State.INITIAL_STATE, mMockEventListener);
        stateMachine.send(UnguardedMachine.Signal.START);
        verify(stateMachine).onStartSignal(Matchers.<SignalPayload>any());
        verify(mMockEventListener).onChangingState(UnguardedMachine.State.INITIAL_STATE,
                UnguardedMachine.State.STARTED);
    }

    @Test
    public void testGuardBlocksConnection() {
        GuardedMachineImpl stateMachine = spy(new GuardedMachineImpl());
        stateMachine.init(GuardedMachine.State.INITIAL_STATE, mMockEventListener);
        stateMachine.send(GuardedMachine.Signal.START);
        verify(stateMachine).onStartSignal(Matchers.<SignalPayload>any());
        verify(mMockEventListener, never()).onChangingState(GuardedMachine.State.INITIAL_STATE,
                GuardedMachine.State.STARTED);
    }

    @Test
    public void testBlockingGuardPassesSignalOn() {
        MultiConnectionsWithSameSignalMachineImpl stateMachine = spy(new MultiConnectionsWithSameSignalMachineImpl());
        stateMachine.init(MultiConnectionsWithSameSignalMachineImpl.State.INITIAL_STATE, mMockEventListener);
        stateMachine.send(MultiConnectionsWithSameSignalMachineImpl.Signal.START);

        // We also verify that the connections are called in the order that they are declared.
        // It is up to the developer normally to make sure that two connections for the same signal
        // do not have a satisfied guard at the same time. If that is the case then the first connection
        // to be invoked will be run.
        InOrder inOrder = inOrder(stateMachine, mMockEventListener);
        inOrder.verify(stateMachine).onStartSignalDontDoAnything(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).onStartSignalShouldRun(Matchers.<SignalPayload>any());
        inOrder.verify(mMockEventListener).onChangingState(MultiConnectionsWithSameSignalMachineImpl.State.INITIAL_STATE,
                MultiConnectionsWithSameSignalMachineImpl.State.STARTED);
    }

    @StateMachine
    public static class UnguardedMachine {

        @Signals
        public enum Signal {
            START
        }

        @States
        public enum State {
            INITIAL_STATE,
            STARTED
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", signal = "START")
        public boolean onStartSignal(SignalPayload payload) {
            return true;
        }
    }

    @StateMachine
    public static class GuardedMachine {

        @Signals
        public enum Signal {
            START
        }

        @States
        public enum State {
            INITIAL_STATE,
            STARTED
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", signal = "START")
        public boolean onStartSignal(SignalPayload payload) {
            // Return false so the state transition shouldn't be run. This is called a "guard" where
            // returning false means that the guard blocks the request.
            return false;
        }
    }

    @StateMachine
    public static class MultiConnectionsWithSameSignalMachine {

        @Signals
        public enum Signal {
            START
        }

        @States
        public enum State {
            INITIAL_STATE,
            STARTED
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", signal = "START")
        public boolean onStartSignalDontDoAnything(SignalPayload payload) {
            // Return false indicating that the guard is not satisfied
            return false;
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", signal = "START")
        public boolean onStartSignalShouldRun(SignalPayload payload) {
            // Return true indicating that the guard is satisfied
            return true;
        }
    }
}
