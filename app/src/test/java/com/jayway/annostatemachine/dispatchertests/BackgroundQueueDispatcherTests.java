package com.jayway.annostatemachine.dispatchertests;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.dispatchertests.generated.TestMachineImpl;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BackgroundQueueDispatcherTests {

    public static final int CONNECTION_BLOCKING_TIME = 200;

    @Mock
    private StateMachineEventListener mMockEventListener;

    @Test
    public void testDispatchNotBlockingCallingThread() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean callbackCalled = new AtomicBoolean(false);

        TestMachine.Callback callback = new TestMachine.Callback() {
            @Override
            public void onStart() {
                callbackCalled.set(true);
                latch.countDown();
            }

            @Override
            public void onStartingAgain() {

            }
        };

        TestMachineImpl machine = new TestMachineImpl(callback);
        machine.init(TestMachine.State.Init, mMockEventListener);
        machine.send(TestMachine.Signal.Start);

        // The callback should not be called when sending the signal
        assertFalse(callbackCalled.get());

        latch.await((long) (CONNECTION_BLOCKING_TIME * 1.1f), TimeUnit.MILLISECONDS);

        verify(mMockEventListener).onDispatchingSignal(eq(TestMachine.State.Init), eq(TestMachine.Signal.Start));
        // The callback should be called when the signal has been handled
        assertTrue(callbackCalled.get());

        verify(mMockEventListener).onChangingState(eq(TestMachine.State.Init), eq(TestMachine.State.Started));
    }

    @Test
    /**
     * Checks that the garbage collector recycles the thread pool executor when the state machine
     * is no longer used. Such as when an Android activity finishes.
     */
    public void testBackgroundExecutorShutDownWhenNoRefToStateMachine() throws InterruptedException {

        TestMachine.Callback callback = new TestMachine.Callback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onStartingAgain() {
            }
        };

        TestMachineImpl machine = new TestMachineImpl(callback);
        machine.init(TestMachine.State.Init, mMockEventListener);
        machine.send(TestMachine.Signal.Start);

        for (int i = 0; i < 10; i++) {
            // Queue a lot of signals
            machine.send(TestMachine.Signal.Start);
        }

        // Null the machine which should lead to 0 instances of the TestMachine class when
        // garbage collection has run.
        machine = null;

        while (!TestMachine.FINALIZE_LATCH.await(100, TimeUnit.MILLISECONDS)) {
            System.runFinalization();
            System.gc();
            System.out.println("TestMachine instance count: " + TestMachine.FINALIZE_LATCH.getCount());
        }
        System.out.println("TestMachine instance count: " + TestMachine.FINALIZE_LATCH.getCount());

        // The latch is 0 which means that there are no more instances of the state machine.

        // I would like the machine to shut down as soon as the machine is nulled, however there is
        // no time in between task executions on the background thread executor to finish a garbage
        // collection. This means that the tasks in the thread pool executor keeps a local variable
        // of the MessageDispatcher which is in fact the state machine. They do so when running and
        // releases it when they finish, but the next task starts just after. If a finalize and gc
        // is run before retrieving the weak reference in BackgroundQueueDispatcher it works as I'd like.

        // This solution will prevent the state machine from being leaked and also ensures that the
        // executor gets garbage collected when no-one has a reference to the state machine.

        // The executor can be stopped directly when explicitly told to.

    }

    @Test
    public void testShutdownCancelsTasks() throws InterruptedException {
        final int numExtraStartSignals = 200;
        final AtomicInteger numCalls = new AtomicInteger();
        TestMachine.Callback callback = new TestMachine.Callback() {
            @Override
            public void onStart() {
            }

            @Override
            public void onStartingAgain() {
                numCalls.incrementAndGet();
            }
        };

        TestMachineImpl machine = new TestMachineImpl(callback);
        machine.init(TestMachine.State.Init, mMockEventListener);
        machine.send(TestMachine.Signal.Start);

        for (int i = 0; i < numExtraStartSignals; i++) {
            // Queue a lot of signals
            machine.send(TestMachine.Signal.Start);
        }

        // Shut down the machine. Should lead to less than all tasks being run when the state machine
        // has been garbage collected.
        machine.shutDown();
        machine = null;

        while (!TestMachine.FINALIZE_LATCH.await(100, TimeUnit.MILLISECONDS)) {
            System.runFinalization();
            System.gc();
            System.out.println("Latch count: " + TestMachine.FINALIZE_LATCH.getCount());
        }
        System.out.println("Latch count: " + TestMachine.FINALIZE_LATCH.getCount());
        Assert.assertTrue(numCalls.get() < numExtraStartSignals);
    }

    @StateMachine(dispatchMode = StateMachine.DispatchMode.BACKGROUND_QUEUE)
    public static class TestMachine {

        private static CountDownLatch FINALIZE_LATCH = new CountDownLatch(0);

        private final Callback mCallback;

        public TestMachine(Callback callback) {
            FINALIZE_LATCH = new CountDownLatch((int) (FINALIZE_LATCH.getCount() + 1));
            mCallback = callback;
        }

        @Signals
        public enum Signal {
            Start
        }

        @States
        public enum State {
            Init, Started, StartedAgain
        }

        @Connection(from = "Init", to = "Started", on = "Start")
        public boolean onStart(SignalPayload payload) {
            // Make the code take some time
            try {
                Thread.sleep(CONNECTION_BLOCKING_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mCallback.onStart();
            return true;
        }

        @Connection(from = "Started", to = "*", on = "Start")
        public boolean onStartWhenStarted(SignalPayload payload) {
            // Make the code take some time
            try {
                Thread.sleep(CONNECTION_BLOCKING_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mCallback.onStartingAgain();
            return true;
        }

        public interface Callback {
            void onStart();

            void onStartingAgain();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            FINALIZE_LATCH.countDown();
        }
    }
}
