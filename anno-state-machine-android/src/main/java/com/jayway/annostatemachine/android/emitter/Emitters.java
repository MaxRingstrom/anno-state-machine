package com.jayway.annostatemachine.android.emitter;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.StateMachineFront;

/**
 * Helper methods that emit signals to state machines
 */
public class Emitters {

    public static <Signal extends Enum> View.OnClickListener onClick(View view,
                                                        @NonNull final StateMachineFront<Signal> stateMachine,
                                                        @NonNull final Signal signal,
                                                        @Nullable final SignalPayload payload) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateMachine.send(signal, payload);
            }
        };
        view.setOnClickListener(listener);
        return listener;
    }

    public static <Signal extends Enum> View.OnClickListener onClick(View view,
                                                        @NonNull final StateMachineFront<Signal> stateMachine,
                                                        @NonNull final Signal signal) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateMachine.send(signal);
            }
        };
        view.setOnClickListener(listener);
        return listener;
    }

    public static <Signal extends Enum>CheckBox.OnCheckedChangeListener onCheckedChanged(CheckBox checkBox,
                                                                            @NonNull final StateMachineFront<Signal> stateMachine,
                                                                            @NonNull final Signal signal, final String keyForCheckedState) {
        return onCheckedChanged(checkBox, stateMachine, signal, null, keyForCheckedState);
    }

    public static <Signal extends Enum>CheckBox.OnCheckedChangeListener onCheckedChanged(CheckBox checkBox,
                                                                                         @NonNull final StateMachineFront<Signal> stateMachine,
                                                                                         @NonNull final Signal signal, final SignalPayload<Signal> payload,
                                                                                         final String keyForCheckedState) {
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SignalPayload thePayload = payload != null ? payload : new SignalPayload();
                thePayload.put(keyForCheckedState, isChecked);
                stateMachine.send(signal, thePayload);
            }
        };
        checkBox.setOnCheckedChangeListener(listener);
        return listener;
    }
}
