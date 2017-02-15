package com.jayway.annostatemachine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.generated.MainViewStateMachineImpl;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainViewStateMachineImpl stateMachine = new MainViewStateMachineImpl();
        stateMachine.init(MainViewStateMachine.State.INIT);
        stateMachine.send(MainViewStateMachine.Signal.READY, null);
    }

    @StateMachine
    public static class MainViewStateMachine {

        @States
        public enum State {
            INIT,
            WAITING,
            ERROR
        }

        @Signals
        public enum Signal {
            READY,
            ERROR
        }

        @Connection(from="INIT", to="WAITING", signal="READY")
        public boolean readyConnection(SignalPayload payload) {
            return true;
        }

        @Connection(from="INIT", to="ERROR", signal="ERROR")
        public boolean errorInInitConnection(SignalPayload payload) {
            return true;
        }

        @Connection(from="WAITING", to="ERROR", signal="ERROR")
        public boolean errorInWaitingConnectionGeneric(SignalPayload payload) {
            return true;
        }

        @Connection(from="WAITING", to="ERROR", signal="ERROR")
        public boolean errorInWaitingConnection404(SignalPayload payload) {
            return false;
        }
    }
}
