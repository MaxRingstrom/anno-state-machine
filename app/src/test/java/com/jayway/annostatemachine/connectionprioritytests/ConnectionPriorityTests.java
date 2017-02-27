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

import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1;
import static com.jayway.annostatemachine.connectionprioritytests.ConnectionPriorityTests.MultiMachine.KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2, false)
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

        inOrder.verify(stateMachine).globalSpecificSignalTransition1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalTransition2(Matchers.<SignalPayload>any());

        inOrder.verify(stateMachine).globalAnySignalTransition1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalTransition2(Matchers.<SignalPayload>any());

        inOrder.verify(mMockEventListener, never()).onChangingState(Matchers.any(), Matchers.any());
    }

    @Test
    public void testAllSpiesCalledIfLocalTransitionGuardSatisfied() {
        MultiMachineImpl stateMachine = spy(new MultiMachineImpl());
        stateMachine.init(MultiMachine.State.INITIAL_STATE, mMockEventListener);

        stateMachine.send(MultiMachine.Signal.START, new SignalPayload()
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_1, true)
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
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2, false)
        );

        InOrder inOrder = Mockito.inOrder(stateMachine, mMockEventListener);
        inOrder.verify(stateMachine).localSpecificSignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localSpecificSignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localAnySignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localAnySignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalSpy2(Matchers.<SignalPayload>any());
    }

    @Test
    public void testAllSpiesCalledIfLocalAnySignalTransitionGuardSatisfied() {
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
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1, true)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2, false)
        );

        InOrder inOrder = Mockito.inOrder(stateMachine, mMockEventListener);
        inOrder.verify(stateMachine).localSpecificSignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localSpecificSignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localAnySignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localAnySignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalSpy2(Matchers.<SignalPayload>any());
    }

    @Test
    public void testAllSpiesCalledIfGlobalSpecificSignalTransitionGuardSatisfied() {
        MultiMachineImpl stateMachine = spy(new MultiMachineImpl());
        stateMachine.init(MultiMachine.State.INITIAL_STATE, mMockEventListener);

        stateMachine.send(MultiMachine.Signal.START, new SignalPayload()
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_1, true)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2, false)
        );

        InOrder inOrder = Mockito.inOrder(stateMachine, mMockEventListener);
        inOrder.verify(stateMachine).localSpecificSignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localSpecificSignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localAnySignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localAnySignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalSpy2(Matchers.<SignalPayload>any());
    }

    @Test
    public void testAllSpiesCalledIfGlobalAnySignalTransitionGuardSatisfied() {
        MultiMachineImpl stateMachine = spy(new MultiMachineImpl());
        stateMachine.init(MultiMachine.State.INITIAL_STATE, mMockEventListener);

        stateMachine.send(MultiMachine.Signal.START, new SignalPayload()
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_1, true)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_2, true)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1, true)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2, false)
        );

        InOrder inOrder = Mockito.inOrder(stateMachine, mMockEventListener);
        inOrder.verify(stateMachine).localSpecificSignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localSpecificSignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localAnySignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).localAnySignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalSpecificSignalSpy2(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalSpy1(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).globalAnySignalSpy2(Matchers.<SignalPayload>any());
    }

    @Test
    public void testSatisfiedLocalSpecificSignalTransitionBlocksFurtherTransitionConnections() {
        MultiMachineImpl stateMachine = spy(new MultiMachineImpl());
        stateMachine.init(MultiMachine.State.INITIAL_STATE, mMockEventListener);

        stateMachine.send(MultiMachine.Signal.START, new SignalPayload()
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_1, true)
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
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2, false)
        );

        verify(stateMachine).localSpecificSignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).localSpecificSignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).localAnySignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).localAnySignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalSpecificSignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalSpecificSignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalAnySignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalAnySignalTransition2(Matchers.<SignalPayload>any());
    }

    @Test
    public void testSatisfiedLocalAnySignalTransitionBlocksFurtherTransitionConnections() {
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
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1, true)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2, false)
        );

        verify(stateMachine).localSpecificSignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine).localSpecificSignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine).localAnySignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).localAnySignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalSpecificSignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalSpecificSignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalAnySignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalAnySignalTransition2(Matchers.<SignalPayload>any());
    }

    @Test
    public void testSatisfiedGlobalSpecificSignalTransitionBlocksFurtherTransitionConnections() {
        MultiMachineImpl stateMachine = spy(new MultiMachineImpl());
        stateMachine.init(MultiMachine.State.INITIAL_STATE, mMockEventListener);

        stateMachine.send(MultiMachine.Signal.START, new SignalPayload()
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_1, true)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_2, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_2, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1, false)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2, false)
        );

        verify(stateMachine).localSpecificSignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine).localSpecificSignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine).localAnySignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine).localAnySignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine).globalSpecificSignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalSpecificSignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalAnySignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalAnySignalTransition2(Matchers.<SignalPayload>any());
    }

    @Test
    public void testSatisfiedGlobalAnySignalTransitionBlocksFurtherTransitionConnections() {
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
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1, true)
                .put(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2, false)
        );

        verify(stateMachine).localSpecificSignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine).localSpecificSignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine).localAnySignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine).localAnySignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine).globalSpecificSignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine).globalSpecificSignalTransition2(Matchers.<SignalPayload>any());
        verify(stateMachine).globalAnySignalTransition1(Matchers.<SignalPayload>any());
        verify(stateMachine, never()).globalAnySignalTransition2(Matchers.<SignalPayload>any());
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
        public static final String KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1 = "satisfyGlobalAnySignalTransition1";
        public static final String KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2 = "satisfyGlobalAnySignalTransition2";

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

        @Connection(from = "INITIAL_STATE", to = "STARTED", on = "START")
        public boolean localSpecificSignalTransition1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", on = "START")
        public boolean localSpecificSignalTransition2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_TRANSITION_2, false);
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", on = "*")
        public boolean localAnySignalTransition1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "STARTED", on = "*")
        public boolean localAnySignalTransition2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_ANY_SIGNAL_TRANSITION_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", on = "START")
        public boolean localSpecificSignalSpy1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", on = "START")
        public boolean localSpecificSignalSpy2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_SPECIFIC_SIGNAL_SPY_2, false);
        }

        @Connection(from = "*", to = "ERROR", on = "START")
        public boolean globalSpecificSignalTransition1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_1, false);
        }

        @Connection(from = "*", to = "ERROR", on = "START")
        public boolean globalSpecificSignalTransition2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_TRANSITION_2, false);
        }

        @Connection(from = "*", to = "*", on = "START")
        public boolean globalSpecificSignalSpy1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_1, false);
        }

        @Connection(from = "*", to = "*", on = "START")
        public boolean globalSpecificSignalSpy2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_SPECIFIC_SIGNAL_SPY_2, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", on = "*")
        public boolean localAnySignalSpy1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_1, false);
        }

        @Connection(from = "INITIAL_STATE", to = "*", on = "*")
        public boolean localAnySignalSpy2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_LOCAL_ANY_SIGNAL_SPY_2, false);
        }

        @Connection(from = "*", to = "*", on = "*")
        public boolean globalAnySignalSpy1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_1, false);
        }

        @Connection(from = "*", to = "*", on = "*")
        public boolean globalAnySignalSpy2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_ANY_SIGNAL_SPY_2, false);
        }

        @Connection(from = "*", to = "ERROR", on = "*")
        public boolean globalAnySignalTransition1(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_1, false);
        }

        @Connection(from = "*", to = "ERROR", on = "*")
        public boolean globalAnySignalTransition2(SignalPayload payload) {
            return payload.getBoolean(KEY_SATISFY_GLOBAL_ANY_SIGNAL_TRANSITION_2, false);
        }
    }
}
