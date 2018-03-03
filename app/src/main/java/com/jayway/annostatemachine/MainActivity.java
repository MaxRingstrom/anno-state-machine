/*
 * Copyright 2017 Jayway (http://www.jayway.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jayway.annostatemachine;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.jayway.annostatemachine.android.util.AndroidMainThreadPoster;
import com.jayway.annostatemachine.annotations.Connection;
import com.jayway.annostatemachine.annotations.OnEnter;
import com.jayway.annostatemachine.annotations.OnExit;
import com.jayway.annostatemachine.annotations.Signals;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.jayway.annostatemachine.annotations.States;
import com.jayway.annostatemachine.generated.MainViewStateMachineImpl;

import static com.jayway.annostatemachine.MainActivity.MainViewStateMachine.KEY_CHECKBOX_CHECKED;
import static com.jayway.annostatemachine.MainActivity.MainViewStateMachine.Signal.CheckBoxCheckStateChanged;
import static com.jayway.annostatemachine.MainActivity.MainViewStateMachine.Signal.ContentLoaded;
import static com.jayway.annostatemachine.MainActivity.MainViewStateMachine.Signal.Next;
import static com.jayway.annostatemachine.MainActivity.MainViewStateMachine.Signal.Start;
import static com.jayway.annostatemachine.android.emitter.Emitters.onCheckedChanged;
import static com.jayway.annostatemachine.android.emitter.Emitters.onClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Handler mHandler;
    private MainViewStateMachineImpl mStateMachine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStateMachine = new MainViewStateMachineImpl(this);
        mStateMachine.init(MainViewStateMachine.State.Init, new StateMachineEventListener() {
            @Override
            public void onDispatchingSignal(Object o, Object o1) {
                Log.d(TAG, o1 + "->[" + o + "]");
            }

            @Override
            public void onChangingState(Object o, Object o1) {
                Log.d(TAG, "State switch from [" + o + "] to [" + o1 + "]");
            }
        }, new AndroidMainThreadPoster(this));

        onCheckedChanged((CheckBox) findViewById(R.id.checkbox),
                mStateMachine, CheckBoxCheckStateChanged, KEY_CHECKBOX_CHECKED);

        onClick(findViewById(R.id.nextButton), mStateMachine, Next);

        mStateMachine.send(Start, null);
        loadContentAsync(mStateMachine);
    }

    @StateMachine(dispatchMode = StateMachine.DispatchMode.BACKGROUND_QUEUE)
    public abstract static class MainViewStateMachine {

        private final View mLoadingView;
        private final TextView mText;
        private final View mNextButton;
        private final View mCheckBox;

        public static final String KEY_CHECKBOX_CHECKED = "checkbox_checked";
        private final MainActivity mActivity;

        public MainViewStateMachine(MainActivity activity) {
            mLoadingView = activity.findViewById(R.id.loadingContent);
            mText = (TextView) activity.findViewById(R.id.text);
            mNextButton = activity.findViewById(R.id.nextButton);
            mCheckBox = activity.findViewById(R.id.checkbox);
            mActivity = activity;
        }

        @States
        public enum State {
            Init, LoadingContent, UpAndRunning, Done, Error, Finish
        }

        @Signals
        public enum Signal {
            Start, ContentLoaded, Next, CheckBoxCheckStateChanged
        }

        @Connection(from = "Init", to = "LoadingContent", on = "Start", runOnMainThread = true)
        public void startLoadingContent() {
            mLoadingView.setVisibility(View.VISIBLE);
            mCheckBox.setVisibility(View.INVISIBLE);
            mNextButton.setVisibility(View.INVISIBLE);
            // We do not have a guard on this connection so we always return true
        }

        @Connection(from = "LoadingContent", to = "UpAndRunning", on = "ContentLoaded", runOnMainThread = true)
        public void onContentLoaded() {
            mLoadingView.setVisibility(View.INVISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
            mCheckBox.setVisibility(View.VISIBLE);
            mText.setText("Welcome! - content has been loaded");
        }

        @Connection(from = "LoadingContent", to = "*", on = "ContentLoaded")
        public void eavesdropOnContentLoaded() {
            Log.d(TAG, "Eavesdropped that content has loaded");
        }

        // Safe check from states
        @Connection(from = "UpAndRunning", to = "Done", on = "CheckBoxCheckStateChanged", runOnMainThread = true)
        public boolean onUserReadyToContinue(SignalPayload payload) {
            if (!payload.getBoolean(KEY_CHECKBOX_CHECKED, false)) {
                // We only continue if the check box is checked
                return false;
            }
            mNextButton.setEnabled(true);
            return true;
        }

        @Connection(from = "Done", to = "UpAndRunning", on = "CheckBoxCheckStateChanged", runOnMainThread = true)
        public boolean onUserNoLongerReadyToContinue(SignalPayload payload) {
            if (payload.getBoolean(KEY_CHECKBOX_CHECKED, false)) {
                // We only continue if the check box is unchecked
                return false;
            }
            mNextButton.setEnabled(false);
            return true;
        }

        @Connection(from = "Done", to = "Finish", on = "Next", runOnMainThread = true)
        public void onNext() {
            Toast.makeText(mActivity, "Next!", Toast.LENGTH_SHORT).show();
        }

        @OnEnter(value = "Done", runOnMainThread = true)
        public void onEnterDone() {
            Toast.makeText(mActivity, "Done state enter!", Toast.LENGTH_SHORT).show();
        }

        @OnExit(value = "Done", runOnMainThread = true)
        public void onExitDone() {
            Toast.makeText(mActivity, "Done state exit!", Toast.LENGTH_SHORT).show();
        }

    }

    private void loadContentAsync(final MainViewStateMachineImpl stateMachine) {
        mHandler = new Handler(getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stateMachine.send(ContentLoaded);
            }
        }, 5000);
    }

}
