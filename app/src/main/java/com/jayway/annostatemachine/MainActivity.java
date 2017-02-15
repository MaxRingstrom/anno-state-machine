package com.jayway.annostatemachine;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
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

        final CheckBox checkbox = (CheckBox) findViewById(R.id.checkbox);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SignalPayload payload = new SignalPayload();
                payload.boolValue = checkbox.isChecked();
                stateMachine.send(MainViewStateMachine.Signal.CHECKBOX_CHECK_STATE_CHANGED, payload);
            }
        });
    }

    @StateMachine
    public abstract static class MainViewStateMachine {

        private final View mLoadingView;
        private final TextView mText;
        private final View mNextButton;
        private final View mCheckBox;

        public MainViewStateMachine(MainActivity activity) {
            mLoadingView = activity.findViewById(R.id.loadingContent);
            mText = (TextView)activity.findViewById(R.id.text);
            mNextButton = activity.findViewById(R.id.nextButton);
            mCheckBox = activity.findViewById(R.id.checkbox);
        }

        @States
        public enum State {
            INIT,
            LOADING_CONTENT,
            UP_AND_RUNNING,
            DONE,
            ERROR
        }

        @Signals
        public enum Signal {
            START,
            CONTENT_LOADED,
            ERROR,
            CHECKBOX_CHECK_STATE_CHANGED
        }

        @Connection(from="INIT", to="LOADING_CONTENT", signal="START")
        public boolean startLoadingContent(SignalPayload payload) {
            mLoadingView.setVisibility(View.VISIBLE);
            mCheckBox.setVisibility(View.INVISIBLE);
            mNextButton.setVisibility(View.INVISIBLE);

            // We do not have a guard on this connection so we always return true
            return true;
        }
// Safe check from states
        @Connection(from="UP_AND_RUNNING", to="DONE", signal="CHECKBOX_CHECK_STATE_CHANGED")
        public boolean onUserReadyToContinue(SignalPayload payload) {
            if (!payload.boolValue) {
                // We only continue if the check box is checked
                return false;
            }
            mNextButton.setEnabled(true);
            return true;
        }

        @Connection(from="DONE", to="UP_AND_RUNNING", signal="CHECKBOX_CHECK_STATE_CHANGED")
        public boolean onUserNoLongerReadyToContinue(SignalPayload payload) {
            if (payload.boolValue) {
                // We only continue if the check box is unchecked
                return false;
            }
            mNextButton.setEnabled(false);
            return true;
        }

        @Connection(from="LOADING_CONTENT", to="UP_AND_RUNNING", signal="CONTENT_LOADED")
        public boolean onContentLoaded(SignalPayload payload) {
            mLoadingView.setVisibility(View.INVISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
            mCheckBox.setVisibility(View.VISIBLE);
            mText.setText("Welcome! - content has been loaded");
            mText.setVisibility(View.VISIBLE);
            return true;
        }

    }
}
