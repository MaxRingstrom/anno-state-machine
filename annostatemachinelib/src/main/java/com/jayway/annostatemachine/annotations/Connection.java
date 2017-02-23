package com.jayway.annostatemachine.annotations;

public @interface Connection {
    String from();
    String to();
    String signal();
    boolean runOnUiThread() default false;
}
