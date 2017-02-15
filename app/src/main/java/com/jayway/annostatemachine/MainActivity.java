package com.jayway.annostatemachine;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.generated.MainViewStateMachineImpl;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MainViewStateMachineImpl stateMachine = new MainViewStateMachineImpl(this);
        stateMachine.init(MainViewStateMachine.State.INIT);
        stateMachine.send(MainViewStateMachine.Signal.START, null);

        mHandler = new Handler(getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    stateMachine.send(MainViewStateMachine.Signal.CONTENT_LOADED);
            }
        }, 5000);
    }

    @StateMachine
    public abstract static class MainViewStateMachine {

        private final View mLoadingView;
        private final TextView mText;

        public MainViewStateMachine(MainActivity activity) {
            mLoadingView = activity.findViewById(R.id.loadingContent);
            mText = (TextView)activity.findViewById(R.id.text);
        }

        @States
        public enum State {
            INIT,
            LOADING_CONTENT,
            IDLE,
            ERROR
        }

        @Signals
        public enum Signal {
            START,
            CONTENT_LOADED,
            ERROR
        }

        @Connection(from="INIT", to="LOADING_CONTENT", signal="START")
        public boolean startLoadingContent(SignalPayload payload) {
            mLoadingView.setVisibility(View.VISIBLE);
            // We do not have a guard on this connection so we always return true
            return true;
        }

        @Connection(from="LOADING_CONTENT", to="IDLE", signal="CONTENT_LOADED")
        public boolean onContentLoaded(SignalPayload payload) {
            mLoadingView.setVisibility(View.INVISIBLE);
            mText.setText("Welcome! - content has been loaded");
            mText.setVisibility(View.VISIBLE);
            return true;
        }

    }
}
