/*
 * Copyright 2017 Jayway (http://www.jayway.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        @Connection(from = "Initial", to = "Ignored", on = "Ignore", runOnUiThread = false)
        public boolean onIgnoreExplicitBg(SignalPayload payload) {
            assertFalse(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        // Not setting runOnUiThread to false. False should be the default value
        @Connection(from = "Initial", to = "Started", on = "Ignore")
        public boolean onIgnoreImplicitBg(SignalPayload payload) {
            assertFalse(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "Initial", to = "Started", on = "Ignore", runOnUiThread = true)
        public boolean onIgnoreFg(SignalPayload payload) {
            assertTrue(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "Initial", to = "*", on = "Ignore", runOnUiThread = true)
        public boolean onSpyIgnoreFg(SignalPayload payload) {
            assertTrue(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "Initial", to = "*", on = "Ignore")
        public boolean onSpyIgnoreBg(SignalPayload payload) {
            assertFalse(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "*", to = "*", on = "Ignore")
        public boolean onGlobalSpyIgnoreBg(SignalPayload payload) {
            assertFalse(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "*", to = "*", on = "Ignore", runOnUiThread = true)
        public boolean onGlobalSpyIgnoreFg(SignalPayload payload) {
            assertTrue(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "*", to = "*", on = "*", runOnUiThread = true)
        public boolean onGlobalAnySignalSpyFg(SignalPayload payload) {
            assertTrue(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "*", to = "*", on = "*")
        public boolean onGlobalAnySignalSpyBg(SignalPayload payload) {
            assertFalse(mUiThreadPoster.isOnUiThreadNow());
            // We want all methods to be called so all guards are unsatisfied
            return false;
        }

        @Connection(from = "Initial", to = "Started", on = "Finish")
        public boolean onFinish(SignalPayload payload) {
            mFinishedLatch.countDown();
            return true;
        }
    }
}
