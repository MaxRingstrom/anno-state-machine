package com.jayway.annostatemachine.dispatchertests;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.UiThreadPoster;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.dispatchertests.generated.UiNonUiStateMachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UiThreadHandoverTests {

    @Mock
    StateMachineEventListener mMockEventListener;

    public static class SynchronousUiThreadPoster implements UiThreadPoster {
        public AtomicBoolean mIsOnUiThreadNow = new AtomicBoolean();

        @Override
        public void runOnUiThread(Runnable runnable) {
            mIsOnUiThreadNow.set(true);
            runnable.run();
            mIsOnUiThreadNow.set(false);
        }

        public boolean isOnUiThreadNow() {
            return mIsOnUiThreadNow.get();
        }
    }

    SynchronousUiThreadPoster mUiThreadPoster = new SynchronousUiThreadPoster();

    @Test
    public void testConnectionsCalledOnCorrectThreads() {
        CountDownLatch latch = new CountDownLatch(1);
        UiNonUiStateMachineImpl spiedMachine = spy(new UiNonUiStateMachineImpl(latch, mUiThreadPoster));
        spiedMachine.init(UiNonUiStateMachine.State.Initial, mMockEventListener, mUiThreadPoster);

        spiedMachine.send(UiNonUiStateMachine.Signal.Ignore);
        spiedMachine.send(UiNonUiStateMachine.Signal.Finish);

        // Wait for the machine to be finished
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify all called
        verify(spiedMachine, times(2)).onGlobalAnySignalSpyBg(Matchers.<SignalPayload>any());
        verify(spiedMachine, times(2)).onGlobalAnySignalSpyFg(Matchers.<SignalPayload>any());
        verify(spiedMachine, times(1)).onIgnoreImplicitBg(Matchers.<SignalPayload>any());
        verify(spiedMachine, times(1)).onIgnoreExplicitBg(Matchers.<SignalPayload>any());
        verify(spiedMachine, times(1)).onIgnoreFg(Matchers.<SignalPayload>any());
        verify(spiedMachine, times(1)).onSpyIgnoreBg(Matchers.<SignalPayload>any());
        verify(spiedMachine, times(1)).onSpyIgnoreFg(Matchers.<SignalPayload>any());
        verify(spiedMachine, times(1)).onGlobalSpyIgnoreBg(Matchers.<SignalPayload>any());
        verify(spiedMachine, times(1)).onGlobalSpyIgnoreFg(Matchers.<SignalPayload>any());
        verify(spiedMachine, times(1)).onFinish(Matchers.<SignalPayload>any());
    }

    @StateMachine(dispatchMode = StateMachine.DispatchMode.BACKGROUND_QUEUE)
    public static class UiNonUiStateMachine {

        private final CountDownLatch mFinishedLatch;
        private final SynchronousUiThreadPoster mUiThreadPoster;

        @Signals
        public enum Signal {
            Ignore, Finish
        }

        @States
        public enum State {
            Initial, Ignored, Started
        }

        public UiNonUiStateMachine(CountDownLatch finishedLatch, SynchronousUiThreadPoster uiThreadPoster) {
            mFinishedLatch = finishedLatch;
            mUiThreadPoster = uiThreadPoster;
        }

        // Explicitly setting runOnUiThread to false even though false should be default
        @Connection(from = "Initial", to = "Ignored", signal = "Ignore", runOnUiThread = false)
        public boolean onIgnoreExplicitBg(SignalPayload payload) {
            assertFalse(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        // Not setting runOnUiThread to false. False should be the default value
        @Connection(from = "Initial", to = "Started", signal = "Ignore")
        public boolean onIgnoreImplicitBg(SignalPayload payload) {
            assertFalse(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "Initial", to = "Started", signal = "Ignore", runOnUiThread = true)
        public boolean onIgnoreFg(SignalPayload payload) {
            assertTrue(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "Initial", to = "*", signal = "Ignore", runOnUiThread = true)
        public boolean onSpyIgnoreFg(SignalPayload payload) {
            assertTrue(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "Initial", to = "*", signal = "Ignore")
        public boolean onSpyIgnoreBg(SignalPayload payload) {
            assertFalse(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "*", to = "*", signal = "Ignore")
        public boolean onGlobalSpyIgnoreBg(SignalPayload payload) {
            assertFalse(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "*", to = "*", signal = "Ignore", runOnUiThread = true)
        public boolean onGlobalSpyIgnoreFg(SignalPayload payload) {
            assertTrue(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "*", to = "*", signal = "*", runOnUiThread = true)
        public boolean onGlobalAnySignalSpyFg(SignalPayload payload) {
            assertTrue(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "*", to = "*", signal = "*")
        public boolean onGlobalAnySignalSpyBg(SignalPayload payload) {
            assertFalse(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "Initial", to = "Started", signal = "Finish")
        public boolean onFinish(SignalPayload payload) {
            mFinishedLatch.countDown();
            return true;
        }
    }
}
