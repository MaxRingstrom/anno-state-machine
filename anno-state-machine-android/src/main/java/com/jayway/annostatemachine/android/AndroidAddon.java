package com.jayway.annostatemachine.android;


import com.jayway.annostatemachine.AnnoStateMachine;
import com.jayway.annostatemachine.FrameworkAddon;
import com.jayway.annostatemachine.android.util.LogcatStateMachineLogger;

public class AndroidAddon implements FrameworkAddon {
    private static final String TAG = AndroidAddon.class.getSimpleName();

    @Override
    public void init() {
        AnnoStateMachine.setLogger(new LogcatStateMachineLogger());
    }
}
