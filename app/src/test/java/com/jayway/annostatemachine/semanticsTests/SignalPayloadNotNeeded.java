package com.jayway.annostatemachine.semanticsTests;


import com.jayway.annostatemachine.MainThreadPoster;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.semanticsTests.generated.PayloadMachineImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.jayway.annostatemachine.semanticsTests.SignalPayloadNotNeeded.PayloadMachine.State.Init;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class SignalPayloadNotNeeded {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void payloadPresenceDoesntAffectTransitions() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(8);

        PayloadMachine.Callback spyCallback = spy(new PayloadMachine.Callback() {
            @Override
            public void first() {
                latch.countDown();
            }

            @Override
            public void second() {
                latch.countDown();
            }

            @Override
            public void third() {
                latch.countDown();
            }

            @Override
            public void fourth() {
                latch.countDown();
            }

            @Override
            public void fifth(SignalPayload signal) {
                if (signal != null) {
                    latch.countDown();
                }
            }

            @Override
            public void sixth(SignalPayload signal) {
                if (signal != null) {
                    latch.countDown();
                }
            }

            @Override
            public void seventh(SignalPayload signal) {
                if (signal != null) {
                    latch.countDown();
                }
            }

            @Override
            public void eighth(SignalPayload signal) {
                if (signal != null) {
                    latch.countDown();
                }
            }
        });

        PayloadMachineImpl machine = new PayloadMachineImpl(spyCallback);
        machine.init(Init, new StateMachineEventListener() {
            @Override
            public void onDispatchingSignal(Object o, Object o1) {
                System.out.println(o1 + " --> " + o1);
            }

            @Override
            public void onChangingState(Object o, Object o1) {
                System.out.println("State switch " + o + " to " + o1);
            }
        }, new MainThreadPoster() {
            @Override
            public void runOnMainThread(Runnable runnable) {
                new Thread(runnable).start();
            }
        });

        machine.send(PayloadMachine.Signal.Next);
        machine.send(PayloadMachine.Signal.Next);
        machine.send(PayloadMachine.Signal.Next);
        machine.send(PayloadMachine.Signal.Next);
        machine.send(PayloadMachine.Signal.Next);
        machine.send(PayloadMachine.Signal.Next);
        machine.send(PayloadMachine.Signal.Next);
        machine.send(PayloadMachine.Signal.Next);

        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));

        System.out.println("Latch awaited");

        verify(spyCallback).first();
        verify(spyCallback).second();
        verify(spyCallback).third();
        verify(spyCallback).fourth();
        verify(spyCallback).fifth(notNull(SignalPayload.class));
        verify(spyCallback).sixth(notNull(SignalPayload.class));
        verify(spyCallback).seventh(notNull(SignalPayload.class));
        verify(spyCallback).eighth(notNull(SignalPayload.class));

        verifyNoMoreInteractions(spyCallback);

    }

    @StateMachine(dispatchMode = StateMachine.DispatchMode.BACKGROUND_QUEUE)
    public static class PayloadMachine {

        private static final String TAG = PayloadMachine.class.getSimpleName();
        private final Callback callback;

        @Signals
        public enum Signal {Next}

        @States
        public enum State { Init, First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth}

        public PayloadMachine(Callback callback) {
            this.callback = callback;
        }

        // NOTE: The order is important. If a no guard connection is placed before a guard connection
        // the guard connection will get a compile error since it can not be reached. Unreachable statement.

        @Connection(from = "Init", to = "First", on = "Next")
        protected boolean withGuardNoMainThreadNoSignal() {
            callback.first();
            return true;
        }

        @Connection(from = "First", to = "Second", on = "Next")
        protected void withoutGuardNoMainThreadNoSignal() {
            callback.second();
        }

        @Connection(from = "Second", to = "Third", on = "Next", runOnMainThread = true)
        protected boolean withGuardRunOnMainThreadNoSignal() {
            callback.third();
            return true;
        }

        @Connection(from = "Third", to = "Fourth", on = "Next", runOnMainThread = true)
        protected void withoutGuardRunOnMainThreadNoSignal() {
            callback.fourth();
        }

        @Connection(from = "Fourth", to = "Fifth", on = "Next")
        protected boolean withGuardNoMainThreadWithSignal(SignalPayload signal) {
            callback.fifth(signal);
            return true;
        }

        @Connection(from = "Fifth", to = "Sixth", on = "Next")
        protected void withoutGuardNoMainThreadWithSignal(SignalPayload signal) {
            callback.sixth(signal);
        }

        @Connection(from = "Sixth", to = "Seventh", on = "Next", runOnMainThread = true)
        protected boolean withGuardRunOnMainThreadWithSignal(SignalPayload signal) {
            callback.seventh(signal);
            return true;
        }

        @Connection(from = "Seventh", to = "Eighth", on = "Next", runOnMainThread = true)
        protected void withoutGuardRunOnMainThreadWithSignal(SignalPayload signal) {
            callback.eighth(signal);
        }

        protected interface Callback {
            void first();
            void second();
            void third();
            void fourth();
            void fifth(SignalPayload signal);
            void sixth(SignalPayload signal);
            void seventh(SignalPayload signal);
            void eighth(SignalPayload signal);
        }
    }
}
