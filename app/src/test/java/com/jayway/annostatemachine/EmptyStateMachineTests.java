package com.jayway.annostatemachine;

import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.generated.EmptyStatemachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.TestCase.assertNotNull;

@RunWith(JUnit4.class)
public class EmptyStateMachineTests {

    @Test
    public void testEmptyMachineCompiles() {
        EmptyStatemachineImpl statemachine = new EmptyStatemachineImpl();
        assertNotNull(statemachine);
    }

    @StateMachine
    public static class EmptyStatemachine {

        @Signals
        public enum Signal {
        }

        @States
        public enum State {
        }
    }

}
