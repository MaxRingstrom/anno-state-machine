package com.jayway.annostatemachine.utils;


public class SystemOutLogger implements StateMachineLogger {

    @Override
    public void d(String tag, String msg) {
        System.out.println("d " + tag + ": " + msg);
    }

    @Override
    public void e(String tag, String msg) {
        System.out.println("e " + tag + ": " + msg);
    }

    @Override
    public void e(String tag, String msg, Throwable t) {
        System.out.println("e " + tag + ": " + msg);
        t.printStackTrace();
        ;
    }
}
