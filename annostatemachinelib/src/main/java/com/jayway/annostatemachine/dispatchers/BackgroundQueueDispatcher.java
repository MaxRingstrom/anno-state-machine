package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.DispatchCallback;
import com.jayway.annostatemachine.SignalDispatcher;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.utils.StateMachineLogger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BackgroundQueueDispatcher<SignalType extends Enum> extends SignalDispatcher<SignalType> {

    private static final String TAG = BackgroundQueueDispatcher.class.getSimpleName();

    private final ScheduledExecutorService mExecutor;

    public BackgroundQueueDispatcher(DispatchCallback<SignalType> dispatchCallback, StateMachineLogger logger) {
        super(dispatchCallback, logger);
        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        System.out.println("uncaught exception in background queue dispatcher");
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    @Override
    public void dispatch(final SignalType signal, final SignalPayload<SignalType> payload) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                DispatchCallback<SignalType> callback = getCallback();
                if (callback != null) {
                    callback.dispatchBlocking(signal, payload);
                } else {
                    mExecutor.shutdownNow();
                    getLogger().e(TAG, "Shutting down executor, callback is null");
                }
            }
        });
    }

    @Override
    public void shutDown() {
        getLogger().d(TAG, "Shutting down executor");
        mExecutor.shutdownNow();
    }
}
