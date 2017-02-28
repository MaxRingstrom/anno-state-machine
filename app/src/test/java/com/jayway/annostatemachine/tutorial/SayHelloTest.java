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
