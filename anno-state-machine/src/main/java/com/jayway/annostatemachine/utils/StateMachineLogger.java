package com.jayway.annostatemachine.utils;


public interface StateMachineLogger {

    void e(String tag, String msg);
    void e(String tag, String msg, Throwable t);
    void d(String tag, String msg);
    void w(String tag, String msg);
}
