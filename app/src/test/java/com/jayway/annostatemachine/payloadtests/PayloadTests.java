package com.jayway.annostatemachine.payloadtests;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.payloadtests.generated.PayloadMachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PayloadTests {

    private static final String KEY_INT = "int";
    private static final String KEY_BOOLEAN = "bool";
    private static final String KEY_STRING = "string";
    private static final String KEY_OBJECT = "obj";
    private static final int TEST_INT = 76;
    private static final boolean TEST_BOOLEAN = true;
    private static final String TEST_STRING = "the test string";
    private static final CustomObject TEST_OBJECT = new CustomObject();

    @Test
    public void testPayloadDataReceived() {
        PayloadMachineImpl machine = new PayloadMachineImpl();
        machine.init(PayloadMachine.State.Start);
        machine.send(PayloadMachine.Signal.Start,
                new SignalPayload().put(KEY_INT, TEST_INT)
                        .put(KEY_BOOLEAN, TEST_BOOLEAN)
                        .put(KEY_STRING, TEST_STRING)
                        .put(KEY_OBJECT, TEST_OBJECT));
    }

    @StateMachine
    public static class PayloadMachine {

        @Signals
        public enum Signal {
            Start
        }

        @States
        public enum State {
            Start, Next
        }

        @Connection(from = "Start", to = "Next", on = "Start")
        public boolean onNext(SignalPayload payload) {
            int integer = payload.getInt(KEY_INT, 0);
            assertEquals(TEST_INT, integer);

            boolean bool = payload.getBoolean(KEY_BOOLEAN, false);
            assertEquals(TEST_BOOLEAN, bool);

            String string = payload.getString(KEY_STRING, "");
            assertEquals(TEST_STRING, string);

            Object object = payload.getObject(KEY_OBJECT, new Object());
            assertEquals(TEST_OBJECT, object);

            return true;
        }
    }

    private static class CustomObject {
    }
}
