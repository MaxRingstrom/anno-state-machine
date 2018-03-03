package com.jayway.annostatemachine.semanticsTests;


import com.jayway.annostatemachine.MainThreadPoster;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.semanticsTests.generated.MachineImpl;

import org.junit.Test;

import static com.jayway.annostatemachine.semanticsTests.guardNoGuardCompiles.Machine.State.Init;

public class guardNoGuardCompiles {

    @Test
    public void machineIsCompiled() {
        MachineImpl machine = new MachineImpl();
        machine.init(Init, new MainThreadPoster() {
            @Override
            public void runOnMainThread(Runnable runnable) {
                runnable.run();
            }
        });
        machine.send(Machine.Signal.Start);
    }

    @StateMachine(dispatchMode = StateMachine.DispatchMode.BACKGROUND_QUEUE)
    public static class Machine {

        private static final String TAG = Machine.class.getSimpleName();

        @Signals
        public enum Signal {Start}

        @States
        public enum State { Init, Next }

        public Machine() {
        }

        // NOTE: The order is important. If a no guard connection is placed before a guard connection
        // the guard connection will get a compile error since it can not be reached. Unreachable statement.

        @Connection(from = "Init", to = "Init", on = "Start")
        protected boolean withGuardNoMainThread(SignalPayload signal) {
            return false;
        }

        @Connection(from = "Init", to = "Init", on = "Start")
        protected void withoutGuardNoMainThread(SignalPayload signal) {
        }

        @Connection(from = "Next", to = "Init", on = "Start", runOnMainThread = true)
        protected boolean withGuardRunOnMainThread(SignalPayload signal) {
            return true;
        }

        @Connection(from = "Next", to = "Init", on = "Start", runOnMainThread = true)
        protected void withoutGuardRunOnMainThread(SignalPayload signal) {
        }
    }
}
