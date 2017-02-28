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

package com.jayway.annostatemachine.tutorial;


import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.tutorial.generated.MyStateMachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.jayway.annostatemachine.tutorial.SayHelloTest.MyStateMachine.Signal.SayHello;
import static com.jayway.annostatemachine.tutorial.SayHelloTest.MyStateMachine.State.Strangers;

@RunWith(MockitoJUnitRunner.class)
public class SayHelloTest {

    @Test
    public void testTutorialCodeCompiles() {
        MyStateMachineImpl stateMachine = new MyStateMachineImpl();
        stateMachine.init(Strangers);
        stateMachine.send(SayHello);
    }

    @StateMachine
    public static class MyStateMachine {

        @Signals
        public enum Signal { SayHello }

        @States
        public enum State { Strangers, Introduced }

        @Connection(from = "Strangers", to = "Introduced", on = "SayHello")
        protected boolean sayHello(SignalPayload payload) {
            System.out.println("Hello");
            return true;
        }
    }

}
