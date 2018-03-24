package com.jayway.annostatemachine.autoconnections;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.autoconnections.generated.AutoTestMachineImpl;
import com.jayway.annostatemachine.autoconnections.generated.AutoTestMachineWithUiThreadImpl;
import com.jayway.annostatemachine.utils.SynchronousMainThreadPoster;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class AutoConnectionTests {

    @Test
    public void testAutoConnectionCalledOnInitialState() {

        AutoTestMachineImpl spiedMachine = spy(new AutoTestMachineImpl());
        spiedMachine.init(AutoTestMachine.State.Init);

        InOrder inOrder = inOrder(spiedMachine);
        inOrder.verify(spiedMachine).onAutoInit(any());
        inOrder.verify(spiedMachine).onAutoStarted(any());
        inOrder.verify(spiedMachine, never()).onStart(any());
    }

    @Test
    public void testMainThreadCalled() {

        SynchronousMainThreadPoster poster = new SynchronousMainThreadPoster();

        // The machine it self checks that it is run on the proper threads

        AutoTestMachineWithUiThreadImpl spiedMachine = spy(new AutoTestMachineWithUiThreadImpl(poster));
        spiedMachine.init(AutoTestMachineWithUiThreadImpl.State.First, poster);

        InOrder inOrder = inOrder(spiedMachine);
        inOrder.verify(spiedMachine).goToSecond(any());
        inOrder.verify(spiedMachine).goToThird(any());
        inOrder.verify(spiedMachine).goToFourth(any());
    }

    @StateMachine
    public static class AutoTestMachine {

        @Signals
        public enum Signal {
            Start
        }

        @States
        public enum State {
            Init, Started, Next
        }

        @Connection(from = "Init", to = "Started", on="!")
        public void onAutoInit(SignalPayload payload) {
            System.out.println("Called");
        }

        @Connection(from = "Started", to = "Next", on="!")
        public boolean onAutoStarted(SignalPayload payload) {
            System.out.println("Called 2");
            return true;
        }

        @Connection(from = "Init", to = "Started", on="Start")
        public boolean onStart(SignalPayload payload) {
            return true;
        }
    }

    @StateMachine
    public static class AutoTestMachineWithUiThread {

        private final SynchronousMainThreadPoster mMainThreadPoster;

        public AutoTestMachineWithUiThread(SynchronousMainThreadPoster poster) {
            mMainThreadPoster = poster;
        }

        @Signals
        public enum Signal {
        }

        @States
        public enum State {
            First, Second, Third, Fourth
        }

        @Connection(from = "First", to = "Second", on="!")
        public boolean goToSecond(SignalPayload payload) {
            assertFalse(mMainThreadPoster.isOnUiThreadNow());
            return true;
        }

        @Connection(from = "Second", to = "Third", on="!", runOnMainThread = true)
        public boolean goToThird(SignalPayload payload) {
            assertTrue(mMainThreadPoster.isOnUiThreadNow());
            return true;
        }

        @Connection(from = "Third", to = "Fourth", on="!")
        public boolean goToFourth(SignalPayload payload) {
            assertFalse(mMainThreadPoster.isOnUiThreadNow());
            return true;
        }
    }
}
