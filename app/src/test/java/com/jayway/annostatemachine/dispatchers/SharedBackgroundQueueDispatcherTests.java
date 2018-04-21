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

package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.dispatchers.generated.MachineOneImpl;
import com.jayway.annostatemachine.dispatchers.generated.MachineTwoImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class SharedBackgroundQueueDispatcherTests {

    static final int SHARED_QUEUE_ID = 1;
    static final long DURATION_ON_START = 20;

    @Before
    public void setUp() {
        BackgroundQueuePool.reset();
    }

    @Test
    public void testTwoMachinesShareBackgroundQueue() throws InterruptedException {
        CountDownLatch finishedLatch = new CountDownLatch(1);

        MachineOneImpl spiedMachineOne = spy(new MachineOneImpl(finishedLatch));
        MachineTwoImpl spiedMachineTwo = spy(new MachineTwoImpl());

        // Fix multiple logger instances

        StateMachineEventListener eventListener1 = new StateMachineEventListener() {
            @Override
            public void onDispatchingSignal(Object o, Object o1) {
                System.out.println("1: " + o1 + " --> " + o1);
            }

            @Override
            public void onChangingState(Object o, Object o1) {
                System.out.println("1: State switch " + o + " to " + o1);
            }

            @Override
            public void onThrowable(Throwable t) {

            }
        };

        StateMachineEventListener eventListener2 = new StateMachineEventListener() {
            @Override
            public void onDispatchingSignal(Object o, Object o1) {
                System.out.println("2: " + o1 + " --> " + o1);
            }

            @Override
            public void onChangingState(Object o, Object o1) {
                System.out.println("2: State switch " + o + " to " + o1);
            }

            @Override
            public void onThrowable(Throwable t) {

            }
        };

        spiedMachineOne.init(MachineOne.State.Init, eventListener1);
        spiedMachineTwo.init(MachineTwo.State.Init, eventListener2);

        spiedMachineOne.send(MachineOne.Signal.Start);
        spiedMachineTwo.send(MachineTwo.Signal.Start);
        spiedMachineTwo.send(MachineTwo.Signal.Start2);
        spiedMachineTwo.send(MachineTwo.Signal.Start3);
        spiedMachineOne.send(MachineOne.Signal.Start2);

        finishedLatch.await();

        InOrder inOrder = Mockito.inOrder(spiedMachineOne, spiedMachineTwo);
        inOrder.verify(spiedMachineOne).onStart(Matchers.<SignalPayload>any());
        inOrder.verify(spiedMachineTwo).onStart(Matchers.<SignalPayload>any());
        inOrder.verify(spiedMachineTwo).onStart2(Matchers.<SignalPayload>any());
        inOrder.verify(spiedMachineTwo).onStart3(Matchers.<SignalPayload>any());
        inOrder.verify(spiedMachineOne).onStart2(Matchers.<SignalPayload>any());

        assertEquals(spiedMachineOne.onStartCallingThreadId, spiedMachineTwo.onStartCallingThreadId);
    }

    @StateMachine(dispatchMode = StateMachine.DispatchMode.SHARED_BACKGROUND_QUEUE, queueId = SHARED_QUEUE_ID)
    public static class MachineOne {

        private final CountDownLatch mFinishedLatch;
        public long onStartCallingThreadId;

        public MachineOne(CountDownLatch finishedLatch) {
            mFinishedLatch = finishedLatch;
        }

        @Signals
        public enum Signal {
            Start, Start2
        }

        @States
        public enum State {
            Init
        }

        @Connection(from = "Init", to = "*", on = "Start")
        public boolean onStart(SignalPayload payload) {
            onStartCallingThreadId = Thread.currentThread().getId();
            try {
                Thread.sleep(DURATION_ON_START);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Connection(from = "Init", to = "*", on = "Start2")
        public boolean onStart2(SignalPayload payload) {
            try {
                Thread.sleep(DURATION_ON_START);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mFinishedLatch.countDown();
            return true;
        }
    }

    @StateMachine(dispatchMode = StateMachine.DispatchMode.SHARED_BACKGROUND_QUEUE, queueId = SHARED_QUEUE_ID)
    public static class MachineTwo {

        public long onStartCallingThreadId;

        @Signals
        public enum Signal {
            Start, Start2, Start3
        }

        @States
        public enum State {
            Init
        }

        @Connection(from = "Init", to = "*", on = "Start")
        public boolean onStart(SignalPayload payload) {
            onStartCallingThreadId = Thread.currentThread().getId();
            try {
                Thread.sleep(DURATION_ON_START);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Connection(from = "Init", to = "*", on = "Start2")
        public boolean onStart2(SignalPayload payload) {
            try {
                Thread.sleep(DURATION_ON_START);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Connection(from = "Init", to = "*", on = "Start2")
        public boolean onStart3(SignalPayload payload) {
            try {
                Thread.sleep(DURATION_ON_START);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
