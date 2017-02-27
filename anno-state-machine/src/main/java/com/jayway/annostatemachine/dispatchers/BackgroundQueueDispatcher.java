package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.DispatchCallback;
import com.jayway.annostatemachine.SignalDispatcher;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.utils.StateMachineLogger;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundQueueDispatcher extends SignalDispatcher {

    private static final String TAG = BackgroundQueueDispatcher.class.getSimpleName();

    private final ScheduledExecutorService mExecutor;
    private AtomicBoolean mIsShutDown = new AtomicBoolean();

    public BackgroundQueueDispatcher() {
        super();
        mExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void dispatch(Enum signal, SignalPayload payload, DispatchCallback callback, StateMachineLogger logger) {
        mExecutor.submit(new DispatchRunnable(new WeakReference<>(callback),
                new WeakReference<>(mExecutor), signal, payload, logger));
    }

    private static class DispatchRunnable implements Runnable {

        private static final boolean GC_WINDOW_ENABLED = false;
        private final WeakReference<DispatchCallback> mCallbackRef;
        private final WeakReference<ScheduledExecutorService> mExecutorRef;
        private final Enum mSignal;
        private final SignalPayload mPayLoad;
        private final StateMachineLogger mLogger;

        DispatchRunnable(WeakReference<DispatchCallback> callbackRef,
                                WeakReference<ScheduledExecutorService> executorRef,
                                Enum signal, SignalPayload payload,
                                StateMachineLogger logger) {
            mCallbackRef = callbackRef;
            mExecutorRef = executorRef;
            mSignal = signal;
            mPayLoad = payload;
            mLogger = logger;
        }

        @Override
        public void run() {
            // The WeakReference to the callback will rarely be garbage collected until all queued
            // tasks are finished due to the lack of a window for garbage collection here. The next
            // task will be run directly leading to a local hard reference to the
            // DispatchCallback(state machine) which prevents the state machine and this dispatcher
            // from being garbage collected.
            if (GC_WINDOW_ENABLED) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            DispatchCallback callback = mCallbackRef.get();
            if (callback != null) {
                callback.dispatchBlocking(mSignal, mPayLoad);
            } else {
                ScheduledExecutorService executor = mExecutorRef.get();
                if (executor != null) {
                    mLogger.d(TAG, "Shutting down executor since callback has been garbage collected");
                    executor.shutdownNow();
                }
            }
        }
    }

    @Override
    public void shutDown() {
        mIsShutDown.set(true);
        mExecutor.shutdownNow();
    }

    boolean isShutDown() {
        return mIsShutDown.get();
    }

}
