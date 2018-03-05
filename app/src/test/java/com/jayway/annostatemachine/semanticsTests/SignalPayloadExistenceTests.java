package com.jayway.annostatemachine.semanticsTests;


import com.jayway.annostatemachine.MainThreadPoster;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.semanticsTests.generated.PayloadMachineImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.jayway.annostatemachine.semanticsTests.SignalPayloadExistenceTests.PayloadMachine.State.Init;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class SignalPayloadExistenceTests {

    @Mock
    PayloadMachine.Callback mockCallback;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void payloadPresenceDoesntAffectTransitions() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        PayloadMachineImpl machine = new PayloadMachineImpl(mockCallback, latch);
        machine.init(Init, new MainThreadPoster() {
            @Override
            public void runOnMainThread(Runnable runnable) {
                runnable.run();
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

        latch.await(1000, TimeUnit.MILLISECONDS);

        InOrder inOrder = Mockito.inOrder(mockCallback);
        inOrder.verify(mockCallback).first();
        inOrder.verify(mockCallback).second();
        inOrder.verify(mockCallback).third();
        inOrder.verify(mockCallback).fourth();
        inOrder.verify(mockCallback).fifth(notNull(SignalPayload.class));
        inOrder.verify(mockCallback).sixth(notNull(SignalPayload.class));
        inOrder.verify(mockCallback).seventh(notNull(SignalPayload.class));
        inOrder.verify(mockCallback).eighth(notNull(SignalPayload.class));

        verifyNoMoreInteractions(mockCallback);

    }

    @StateMachine(dispatchMode = StateMachine.DispatchMode.BACKGROUND_QUEUE)
    public static class PayloadMachine {

        private static final String TAG = PayloadMachine.class.getSimpleName();
        private final Callback callback;
        private final CountDownLatch latch;

        @Signals
        public enum Signal {Next}

        @States
        public enum State { Init, First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth}

        public PayloadMachine(Callback callback, CountDownLatch latch) {
            this.latch = latch;
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
        protected void withoutGuardRunOnMainThreadWithSignalAndGenerics(SignalPayload<Signal> signal) {
            callback.eighth(signal);
            latch.countDown();
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
