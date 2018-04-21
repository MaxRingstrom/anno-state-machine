package com.jayway.annostatemachine.parameters;


import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.TestHelper;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.parameters.generated.ParameterLoadTestMachineImpl;
import com.jayway.annostatemachine.utils.SystemOutLogger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static com.jayway.annostatemachine.parameters.ParameterLoadTests.ParameterLoadTestMachine.State.Init;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

public class ParameterLoadTests {

    private static final int TEST_INT = 48;
    private static final boolean TEST_BOOLEAN = true;
    private static final String TEST_STRING = "itsateststring";

    private static final CustomType TEST_CUSTOM_TYPE = new CustomType();

    @Mock
    ParameterLoadTestMachine.Callback mockCallback;

    @Mock
    StateMachineEventListener mockEventListener;

    @Before
    public void setUp() {
        TestHelper.setLoggerForTest(new SystemOutLogger());
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void primitiveParamsAreReceived() {
        ParameterLoadTestMachineImpl machine = new ParameterLoadTestMachineImpl(mockCallback);
        machine.init(Init);

        machine.send(ParameterLoadTestMachine.Signal.First, new SignalPayload()
                .put("anIntValue", TEST_INT)
                .put("aBoolean", TEST_BOOLEAN)
                .put("aString", TEST_STRING)
        );

        verify(mockCallback).first(TEST_INT, TEST_BOOLEAN, TEST_STRING);
    }

    @Test
    public void nonPrimitiveParamsAreReceived() {
        ParameterLoadTestMachineImpl machine = new ParameterLoadTestMachineImpl(mockCallback);
        machine.init(Init);

        machine.send(ParameterLoadTestMachine.Signal.Second, new SignalPayload()
                .put("aCustomType", TEST_CUSTOM_TYPE)
        );

        verify(mockCallback).second(TEST_CUSTOM_TYPE);
    }

    @Test
    public void genericTypeParamsAreReceived() {
        ParameterLoadTestMachineImpl machine = new ParameterLoadTestMachineImpl(mockCallback);
        machine.init(Init);

        String testString1 = "test1";
        String testString2 = "test2";
        ArrayList<String> testStrings = new ArrayList<>();
        testStrings.add(testString1);
        testStrings.add(testString2);

        machine.send(ParameterLoadTestMachine.Signal.Third, new SignalPayload()
                .put("strings", testStrings));

        verify(mockCallback).third(argThat(new ArgumentMatcher<ArrayList<String>>() {
            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof ArrayList)) {
                    return false;
                }
                ArrayList strings = (ArrayList<String>)argument;
                return strings.size() == testStrings.size() && strings.get(0) == testStrings.get(0) && strings.get(1) == testStrings.get(1);
            }
        }));
    }

    @Test
    public void wrongClassTypeForSignalPayloadThrowsIllegalArgumentException() {
        ParameterLoadTestMachineImpl machine = new ParameterLoadTestMachineImpl(mockCallback);
        machine.init(Init, mockEventListener);

        // Sending a signal with a float array as "param". The connection expects an in array so we should get an
        // IllegalArgumentException
        machine.send(ParameterLoadTestMachine.Signal.Fourth, new SignalPayload().put("param", new float[]{}));
        verify(mockEventListener).onThrowable(any(IllegalArgumentException.class));
    }

    @Test
    public void payloadCanBeReceived() {
        ParameterLoadTestMachineImpl machine = new ParameterLoadTestMachineImpl(mockCallback);
        machine.init(Init);

        SignalPayload<ParameterLoadTestMachine.Signal> payload = new SignalPayload<>();

        machine.send(ParameterLoadTestMachine.Signal.Fifth, payload);

        verify(mockCallback).fifth(argThat(new ArgumentMatcher<SignalPayload<ParameterLoadTestMachine.Signal>>() {
            @Override
            public boolean matches(Object argument) {
                return argument == payload;
            }
        }));
    }

    @StateMachine
    public static class ParameterLoadTestMachine {

        private static final String TAG = ParameterLoadTestMachine.class.getSimpleName();
        private final Callback callback;

        @Signals
        public enum Signal { First, Second, Third, Fourth, Fifth}

        @States
        public enum State { Init }

        public ParameterLoadTestMachine(Callback callback) {
            this.callback = callback;
        }

        @Connection(from = "Init", to = "Init", on = "First")
        protected void first(int anIntValue, boolean aBoolean, String aString) {
            callback.first(anIntValue, aBoolean, aString);
        }

        @Connection(from = "Init", to = "Init", on = "Second")
        protected void second(CustomType aCustomType) {
            callback.second(aCustomType);
        }
        
        @Connection(from = "Init", to = "Init", on = "Third")
        protected void third(ArrayList<String> strings) {
            callback.third(strings);
        }

        @Connection(from = "Init", to = "Init", on = "Fourth")
        protected void fourth(int[] param) {
            callback.fourth(param);
        }

        @Connection(from = "Init", to = "Init", on = "Fifth")
        protected void fifth(int[] param, SignalPayload<Signal> thePayload) {
            callback.fifth(thePayload);
        }

        public interface Callback {

            void first(int anIntValue, boolean aBoolean, String aString);

            void second(CustomType testCustomType);

            void third(ArrayList<String> strings);

            void fourth(int[] strings);

            void fifth(SignalPayload<Signal> thePayload);
        }
    }

    public static class CustomType {

    }
}
