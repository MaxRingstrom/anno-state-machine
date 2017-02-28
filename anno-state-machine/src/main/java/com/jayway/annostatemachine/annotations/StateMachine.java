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

package com.jayway.annostatemachine.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface StateMachine {

    /**
     * A globally shared state machine dispatch queue that runs on a background thread. If this
     * id is specified as the queue id of multiple state machines, they will all use the same queue.
     */
    int ID_GLOBAL_SHARED_QUEUE = -1;

    /**
     * The mode for the signal dispatcher. By default the dispatch occurs on the calling thread,
     * which will block the calling thread until the state machine code has finished.
     * See {@link DispatchMode} for options.
     */
    DispatchMode dispatchMode() default DispatchMode.CALLING_THREAD;

    /**
     * The id of the queue to use when {@link DispatchMode#SHARED_BACKGROUND_QUEUE} is used.
     * If more than one state machine specify the same id they will share
     * the same queue ( and background thread ). If an id is not specified the id
     * {@link #ID_GLOBAL_SHARED_QUEUE} will be used which means that all state machines that do
     * not specify an id uses the same queue.
     */
    int queueId() default ID_GLOBAL_SHARED_QUEUE;

    /**
     * The possible dispatch modes.
     */
    enum DispatchMode {
        /**
         * The thread that sends a signal executes the dispatch code as well as the connection
         * methods which will block the calling thread. This is ok if you do not trigger long
         * running operations.
         */
        CALLING_THREAD,

        /**
         * A background thread is created for the state machine on which the dispatch code is run.
         * This makes signal sending asynchronous and allows the calling thread to continue.
         */
        BACKGROUND_QUEUE,

        /**
         * A background thread is used for signal dispatch. If two state machines use the same
         * queue id, they will also share the same queue and thread. If a queue id is not specified,
         * a global background queue is used.
         */
        SHARED_BACKGROUND_QUEUE
    }
}
