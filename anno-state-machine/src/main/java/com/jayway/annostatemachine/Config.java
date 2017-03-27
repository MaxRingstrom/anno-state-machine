package com.jayway.annostatemachine;


import com.jayway.annostatemachine.utils.StateMachineLogger;
import com.jayway.annostatemachine.utils.SystemOutLogger;

import java.util.ArrayList;

public class Config {

    private static Config sInstance;

    private StateMachineLogger mLogger;

    public static Config get() {
        if (sInstance == null) {
            sInstance = new Config();
            sInstance.init();
        }
        return sInstance;
    }

    private void init() {
        // Platform specific versions will create a class named Addons in which platform specific
        // addons are listed. However, it is done in the platform lib so we have to use reflection
        // to get it.

        // If more than one Addon is present it's important not to modify the same static fields such
        // as the logger set in the AnnoStateMachine class
        try {
            Class clazz = Config.class.getClassLoader().loadClass("com.jayway.annostatemachine.Addons");
            AddonRepo repo = (AddonRepo) clazz.newInstance();
            ArrayList<FrameworkAddon> addons = repo.getAddons();
            if (addons == null) {
                return;
            }
            for (FrameworkAddon addon : addons) {
                addon.init();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        collectDependencies();
    }

    private void collectDependencies() {
        mLogger = AnnoStateMachine.getLogger();
    }

    public StateMachineLogger getLogger() {
        return mLogger;
    }

    void setLoggerForTest(StateMachineLogger logger) {
        mLogger = logger;
    }
}
