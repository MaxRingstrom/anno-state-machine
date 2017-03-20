package com.jayway.annostatemachine;

import com.jayway.annostatemachine.utils.StateMachineLogger;
import com.jayway.annostatemachine.utils.SystemOutLogger;

/**
 * A global settings object that configures AnnoStateMachine.
 */
public class AnnoStateMachine {

    private static StateMachineLogger sLogger = new SystemOutLogger();

    public static void setLogger(StateMachineLogger logger) {
        sLogger = logger;
    }

    public static StateMachineLogger getLogger() {
        return sLogger;
    }
}
