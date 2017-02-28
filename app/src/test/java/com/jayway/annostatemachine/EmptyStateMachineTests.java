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
