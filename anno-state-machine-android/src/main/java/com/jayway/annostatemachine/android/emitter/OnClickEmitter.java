package com.jayway.annostatemachine.android.emitter;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineFront;

import java.lang.ref.WeakReference;

/**
 * Sends a signal to a state machine when a view is clicked.
 *
 * @param <Signal> The class Signal class
 */
public class OnClickEmitter<Signal extends Enum> implements View.OnClickListener {

    private final WeakReference<StateMachineFront<Signal>> mStateMachine;
    private final Signal mSignal;
    private final SignalPayload mPayload;

    public OnClickEmitter(@NonNull StateMachineFront<Signal> stateMachine, @NonNull Signal signal,
                          @Nullable SignalPayload payload) {
        mSignal = signal;
        mPayload = payload;
        mStateMachine = new WeakReference<>(stateMachine);
    }

    @Override
    public void onClick(View v) {
        StateMachineFront<Signal> machine = mStateMachine.get();
        machine.send(mSignal, mPayload);
    }
}
