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

import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_ANY_SIGNAL_EAVESDROPPER_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_ANY_SIGNAL_EAVESDROPPER_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_EAVESDROPPER_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_EAVESDROPPER_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_EXPLICIT_CONNECTION_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_EXPLICIT_CONNECTION_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_ANY_SIGNAL_EAVESDROPPER_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_ANY_SIGNAL_EAVESDROPPER_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_CONNECTION_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_CONNECTION_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_EAVESDROPPER_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_EAVESDROPPER_2;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.State;
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
                .put(KEY_SATISFY_EXPLICIT_CONNECTION_1, false)
                .put(KEY_SATISFY_EXPLICIT_CONNECTION_2, false)
                .put(KEY_SATISFY_EAVESDROPPER_1, false)
                .put(KEY_SATISFY_EAVESDROPPER_2, false)
                .put(KEY_SATISFY_GLOBAL_CONNECTION_1, false)
                .put(KEY_SATISFY_GLOBAL_CONNECTION_2, false)
                .put(KEY_SATISFY_GLOBAL_EAVESDROPPER_1, false)
                .put(KEY_SATISFY_GLOBAL_EAVESDROPPER_2, false)
                .put(KEY_SATISFY_ANY_SIGNAL_EAVESDROPPER_1, false)
                .put(KEY_SATISFY_ANY_SIGNAL_EAVESDROPPER_2, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_EAVESDROPPER_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_EAVESDROPPER_2, false)
        );

        InOrder inOrder = Mockito.inOrder(stateMachine, mMockEventListener);

        inOrder.verify(stateMachine).eavesDropper1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).eavesDropper2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).globalEavesDropper1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalEavesDropper2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).anySignalEavesdropper1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).anySignalEavesdropper2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).globalAnySignalEavesdropper1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalEavesdropper2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).explicitConnection1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).explicitConnection2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).globalConnection1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalConnection2(Matchers.<SignalPayload>any());

        inOrder.verify(mMockEventListener).onChangingState(State.INITIAL_STATE, State.STARTED);
    }

    @StateMachine
    public static class MultiMachine {

        public static final String KEY_SATISFY_EXPLICIT_CONNECTION_1 = "satisfyExplicitConnection1";
        public static final String KEY_SATISFY_EXPLICIT_CONNECTION_2 = "satisfyExplicitConnection2";
        public static final String KEY_SATISFY_EAVESDROPPER_1 = "satisfyEavesdropper1";
        public static final String KEY_SATISFY_EAVESDROPPER_2 = "satisfyEavesdropper2";
        public static final String KEY_SATISFY_GLOBAL_CONNECTION_1 = "satisfyGlobalConnection1";
        public static final String KEY_SATISFY_GLOBAL_CONNECTION_2 = "satisfyGlobalConnection2";
        public static final String KEY_SATISFY_GLOBAL_EAVESDROPPER_1 = "satisfyGlobalEavesdropper1";
        public static final String KEY_SATISFY_GLOBAL_EAVESDROPPER_2 = "satisfyGlobalEavesdropper2";
        public static final String KEY_SATISFY_ANY_SIGNAL_EAVESDROPPER_1 = "satisfyAnySignalEavesdropper1";
        public static final String KEY_SATISFY_ANY_SIGNAL_EAVESDROPPER_2 = "satisfyAnySignalEavesdropper2";
        public static final String KEY_SATISFY_GLOBAL_ANY_SIGNAL_EAVESDROPPER_1 = "satisfyAnySignalEavesdropper1";
        public static final String KEY_SATISFY_GLOBAL_ANY_SIGNAL_EAVESDROPPER_2 = "satisfyAnySignalEavesdropper2";

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
        public boolean explicitConnection1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_EXPLICIT_CONNECTION_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", signal = "START")
        public boolean explicitConnection2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_EXPLICIT_CONNECTION_2, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", signal = "START")
        public boolean eavesDropper1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_EAVESDROPPER_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", signal = "START")
        public boolean eavesDropper2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_EAVESDROPPER_2, false);
        }

        @Connection(from = "*", to = "ERROR", signal = "START")
        public boolean globalConnection1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_CONNECTION_1, false);
        }

        @Connection(from = "*", to = "ERROR", signal = "START")
        public boolean globalConnection2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_CONNECTION_2, false);
        }

        @Connection(from = "*", to = "*", signal = "START")
        public boolean globalEavesDropper1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_EAVESDROPPER_1, false);
        }

        @Connection(from = "*", to = "*", signal = "START")
        public boolean globalEavesDropper2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_EAVESDROPPER_2, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", signal = "*")
        public boolean anySignalEavesdropper1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_ANY_SIGNAL_EAVESDROPPER_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", signal = "*")
        public boolean anySignalEavesdropper2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_ANY_SIGNAL_EAVESDROPPER_2, false);
        }

        @Connection(from = "*", to = "*", signal = "*")
        public boolean globalAnySignalEavesdropper1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_ANY_SIGNAL_EAVESDROPPER_1, false);
        }

        @Connection(from = "*", to = "*", signal = "*")
        public boolean globalAnySignalEavesdropper2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_ANY_SIGNAL_EAVESDROPPER_2, false);
        }
    }
}
