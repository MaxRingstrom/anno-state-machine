package com.jayway.annostatemachine;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signal;
import com.jayway.annostatemachine.annotations.State;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.generated.MainViewStateMachineImpl;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainViewStateMachineImpl stateMachine = new MainViewStateMachineImpl();
    }

    @StateMachine
    public static class MainViewStateMachine {

        @State
        public String init = "Init";

        @State
        public String waiting = "Waiting";

        @Signal
        public String ready = "Ready";

        @Connection(from="Init", to="Waiting", signal="Ready")
        public void readyConnection(SignalPayload payload) {
        }
    }
}
