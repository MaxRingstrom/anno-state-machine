package com.jayway.annostatemachine.connectionprioritytests;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.connectionprioritytests.generated.MultiMachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_2;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionPriorityTests {

    @Mock
    StateMachineEventListener mMockEventListener;

    @Test
    public void testPriorityOrderIfNoSatisfiedGuards() {
        MultiMachineImpl stateMachine = spy(new MultiMachineImpl());
        stateMachine.init(MultiMachine.State.INITIAL_STATE, mMockEventListener);

        stateMachine.send(MultiMachine.Signal.START, new SignalPayload()
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_2, false)
        );

        InOrder inOrder = Mockito.inOrder(stateMachine, mMockEventListener);

        inOrder.verify(stateMachine).localSpecificSignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localSpecificSignalSpy2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).localAnySignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localAnySignalSpy2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).localSpecificSignalTransition1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localSpecificSignalTransition2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).globalSpecificSignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalSpy2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).globalAnySignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalSpy2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).globalSpecificSignalTransition(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalTransition2(Matchers.<SignalPayload>any());

        inOrder.verify(mMockEventListener, never()).onChangingState(Matchers.any(), Matchers.any());
    }

    @StateMachine
    public static class MultiMachine {

        public static final String KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_1 = "satisfyLocalSpecificSignalTransition1";
        public static final String KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_2 = "satisfyLocalSpecificSignalTransition2";
        public static final String KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_1 = "satisfyLocalSpecificSignalSpy1";
        public static final String KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_2 = "satisfyLocalSpecificSignalSpy2";
        public static final String KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_1 = "satisfyGlobalSpecificSignalTransition1";
        public static final String KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_2 = "satisfyGlobalSpecificSignalTransition2";
        public static final String KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_1 = "satisfyGlobalSpecificSignalSpy1";
        public static final String KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_2 = "satisfyGlobalSpecificSignalSpy2";
        public static final String KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_1 = "satisfyLocalAnySignalSpy1";
        public static final String KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_2 = "satisfyLocalAnySignalSpy2";
        public static final String KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_1 = "satisfyGlobalAnySignalSpy1";
        public static final String KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_2 = "satisfyGlobalAnySignalSpy2";
        public static final String KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1 = "satisfyLocalAnySignalTransition1";
        public static final String KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_2 = "satisfyLocalAnySignalTransition2";

        @Signals
        public enum Signal {
            START,
        }

        @States
        public enum State {
            INITIAL_STATE,
            STARTED,
            ERROR
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", signal = "START")
        public boolean localSpecificSignalTransition1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", signal = "START")
        public boolean localSpecificSignalTransition2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_2, false);
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", signal = "*")
        public boolean localAnySignalTransition1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", signal = "*")
        public boolean localAnySignalTransition2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", signal = "START")
        public boolean localSpecificSignalSpy1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", signal = "START")
        public boolean localSpecificSignalSpy2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_2, false);
        }

        @Connection(from = "*", to = "ERROR", signal = "START")
        public boolean globalSpecificSignalTransition(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_1, false);
        }

        @Connection(from = "*", to = "ERROR", signal = "START")
        public boolean globalSpecificSignalTransition2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_2, false);
        }

        @Connection(from = "*", to = "*", signal = "START")
        public boolean globalSpecificSignalSpy1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_1, false);
        }

        @Connection(from = "*", to = "*", signal = "START")
        public boolean globalSpecificSignalSpy2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_2, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", signal = "*")
        public boolean localAnySignalSpy1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", signal = "*")
        public boolean localAnySignalSpy2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_2, false);
        }

        @Connection(from = "*", to = "*", signal = "*")
        public boolean globalAnySignalSpy1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_1, false);
        }

        @Connection(from = "*", to = "*", signal = "*")
        public boolean globalAnySignalSpy2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_2, false);
        }
    }
}
