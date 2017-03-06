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

package com.jayway.annostatemachine.spytests;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineEventListener;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.spytests.generated.MixedEavesDropAndNormalConnectionsMachineImpl;
import com.jayway.annostatemachine.spytests.generated.SimpleEavesDroppingMachineImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SpyTests {

    @Mock
    StateMachineEventListener mMockEventListener;

    @Test
    public void testEavesdropperConnectionDoesntChangeState() {
        SimpleEavesDroppingMachineImpl stateMachine = spy(new SimpleEavesDroppingMachineImpl());
        stateMachine.init(SimpleEavesDroppingMachine.State.INITIAL_STATE, mMockEventListener);
        verify(mMockEventListener).onChangingState(Matchers.any(), Matchers.any());
        reset(mMockEventListener);
        stateMachine.send(SimpleEavesDroppingMachine.Signal.START);
        verify(stateMachine).onStartSignalEavesdropReturningFalse(Matchers.<SignalPayload>any());
        verify(stateMachine).onStartSignalEavesdropReturningTrue(Matchers.<SignalPayload>any());
        verify(mMockEventListener, never()).onChangingState(Matchers.any(), Matchers.any());
    }

    @Test
    public void testEavesdroppersCalledBeforeNonEavesDroppers() {
        MixedEavesDropAndNormalConnectionsMachineImpl stateMachine = spy(new MixedEavesDropAndNormalConnectionsMachineImpl());
        stateMachine.init(MixedEavesDropAndNormalConnectionsMachineImpl.State.INITIAL_STATE, mMockEventListener);
        stateMachine.send(MixedEavesDropAndNormalConnectionsMachineImpl.Signal.START);

        InOrder inOrder = Mockito.inOrder(stateMachine);

        inOrder.verify(stateMachine).onStartSignalEavesdropReturningTrue(Matchers.<SignalPayload>any());
        inOrder.verify(stateMachine).onStartSignalEavesdropReturningFalse(Matchers.<SignalPayload>any());
        // The non eavesdropping connection is declared between the eavesdropping ones but should be
        // called last.
        inOrder.verify(stateMachine).onStartSignal(Matchers.<SignalPayload>any());
        verify(mMockEventListener).onChangingState(
                MixedEavesDropAndNormalConnectionsMachine.State.INITIAL_STATE,
                MixedEavesDropAndNormalConnectionsMachine.State.STARTED);
    }

    @StateMachine
    public static class SimpleEavesDroppingMachine {

        @Signals
        public enum Signal {
            START
        }

        @States
        public enum State {
            INITIAL_STATE,
            STARTED
        }

        @Connection(from = "INITIAL_STATE", to = "*", on = "START")
        public boolean onStartSignalEavesdropReturningTrue(SignalPayload payload) {
            // Return value doesn't matter if eavesdropping (to = *). This connection
            // returns true and another one returns false in order to test this.
            return true;
        }

        @Connection(from = "INITIAL_STATE", to = "*", on = "START")
        public boolean onStartSignalEavesdropReturningFalse(SignalPayload payload) {
            // Return value doesn't matter if eavesdropping (to = *). This connection
            // returns false and another one returns true in order to test this.
            return false;
        }
    }

    @StateMachine
    public static class MixedEavesDropAndNormalConnectionsMachine {

        @Signals
        public enum Signal {
            START
        }

        @States
        public enum State {
            INITIAL_STATE,
            STARTED
        }

        @Connection(from = "INITIAL_STATE", to = "*", on = "START")
        public boolean onStartSignalEavesdropReturningTrue(SignalPayload payload) {
            // Return value doesn't matter if eavesdropping (to = *). This connection
            // returns true and another one returns false in order to test this.
            return true;
        }

        @Connection(from = "INITIAL_STATE", to="STARTED", on = "START")
        public boolean onStartSignal(SignalPayload payload) {
            // Non eavesdropping connection that should be called last and should result
            // in a state switch to the STARTED state.
            return true;
        }

        @Connection(from = "INITIAL_STATE", to = "*", on = "START")
        public boolean onStartSignalEavesdropReturningFalse(SignalPayload payload) {
            // Return value doesn't matter if eavesdropping (to = *). This connection
            // returns false and another one returns true in order to test this.
            return false;
        }
    }
}
