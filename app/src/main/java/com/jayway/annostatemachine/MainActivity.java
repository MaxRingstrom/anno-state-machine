package com.jayway.annostatemachine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

        public static final int FLAG = 1;

        @State
        public void initState() {

        }

    }
}
