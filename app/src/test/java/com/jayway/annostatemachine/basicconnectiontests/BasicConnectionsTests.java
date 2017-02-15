package com.jayway.annostatemachine.basicconnectiontests;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.basicconnectiontests.generated.TestStateMachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BasicConnectionsTests {

    @Mock
    StateMachineEventListener mMockEventListener;

    @Test(expected = IllegalStateException.class)
    public void testIllegalStateExceptionThrownIfInitNotCalled() {
        TestStateMachineImpl stateMachine = new TestStateMachineImpl();
        stateMachine.send(TestStateMachine.Signal.START);
    }

    @Test
    public void testFullySpecifiedConnectionFromInitialStateGoesToTargetState() {
        TestStateMachineImpl stateMachine = spy(new TestStateMachineImpl());
        stateMachine.init(TestStateMachine.State.INITIAL_STATE, mMockEventListener);
        stateMachine.send(TestStateMachine.Signal.START);
        verify(stateMachine).onStartSignal(Matchers.<SignalPayload>any());
        verify(mMockEventListener).onChangingState(TestStateMachine.State.INITIAL_STATE,
                TestStateMachine.State.STARTED);
    }

    @StateMachine
    public static class TestStateMachine {

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

}
