package com.jayway.annostatemachine;


public class PayloadModifier {
    /**
     * Set the signal for a payload. This enables the package private method in {@link SignalPayload} to be used.
     */
    public static <T extends Enum> void setSignalOnPayload(T signal, SignalPayload<T> payload) {
        payload.setSignal(signal);
    }
}
