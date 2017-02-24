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

    public BackgroundQueueDispatcher(DispatchCallback dispatchCallback, StateMachineLogger logger) {
        super(dispatchCallback, logger);
        mExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void dispatch(final Enum signal, final SignalPayload payload) {
        dispatch(signal, payload, getCallbackRef());
    }

    public void dispatch(final Enum signal, final SignalPayload payload, final WeakReference<DispatchCallback> callbackRef) {
        mExecutor.submit(new DispatchRunnable(callbackRef, new WeakReference<>(mExecutor), signal, payload, getLogger()));
    }

    private static class DispatchRunnable implements Runnable {

        private final WeakReference<DispatchCallback> mCallbackRef;
        private final WeakReference<ScheduledExecutorService> mExecutorRef;
        private final Enum mSignal;
        private final SignalPayload mPayLoad;
        private final StateMachineLogger mLogger;

        public DispatchRunnable(WeakReference<DispatchCallback> callbackRef,
                                WeakReference<ScheduledExecutorService> executorRef,
                                Enum signal, SignalPayload payload, StateMachineLogger logger) {
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
            DispatchCallback callback = mCallbackRef.get();
            if (callback != null) {
                callback.dispatchBlocking(mSignal, mPayLoad);
            } else {
                ScheduledExecutorService executor = mExecutorRef.get();
                if (executor != null) {
                    mLogger.e(TAG, "Shutting down executor, callback is null");
                    executor.shutdownNow();
                }
            }
        }
    }

    @Override
    public void shutDown() {
        getLogger().d(TAG, "Shutting down executor");
        mIsShutDown.set(true);
        mExecutor.shutdownNow();
    }

    boolean isShutDown() {
        return mIsShutDown.get();
    }

}
