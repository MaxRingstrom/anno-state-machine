package com.jayway.annostatemachine.dispatchertests;


import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.dispatchertests.generated.MachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ShutdownTests {

    @Test
    public void testSignalIgnoredAfterStateMachineShutDown() {
        MachineImpl spiedMachine = spy(new MachineImpl());
        spiedMachine.init(Machine.State.Init);
        spiedMachine.send(Machine.Signal.Start);
        spiedMachine.shutDown();
        spiedMachine.send(Machine.Signal.Next);

        verify(spiedMachine).onStart(Matchers.<SignalPayload>any());
        verify(spiedMachine, never()).onNext(Matchers.<SignalPayload>any());
    }

    @StateMachine(dispatchMode = StateMachine.DispatchMode.CALLING_THREAD)
    public static class Machine {

        @Signals
        public enum Signal {
            Start, Next
        }

        @States
        public enum State {
            Init, Started, Continued
        }

        @Connection(from = "Init", to = "Started", on = "Start")
        public boolean onStart(SignalPayload payload) {
            return true;
        }

        @Connection(from = "Started", to = "Continued", on = "Next")
        public boolean onNext(SignalPayload payload) {
            return true;
        }

    }
}
