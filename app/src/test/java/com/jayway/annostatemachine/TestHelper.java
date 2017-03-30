package com.jayway.annostatemachine;

import com.jayway.annostatemachine.utils.StateMachineLogger;


/**
 * Helper class that helps with calling package private methods that should be hid from clients but
 * not from the framework itself.
 */
public class TestHelper {
    public static void setLoggerForTest(StateMachineLogger logger) {
        Config.get().setLoggerForTest(logger);
    }
}
