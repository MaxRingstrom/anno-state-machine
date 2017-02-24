package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.DispatchCallback;
import com.jayway.annostatemachine.SignalDispatcher;
import com.jayway.annostatemachine.SignalPayload;
import com.jayway.annostatemachine.utils.StateMachineLogger;

import java.util.concurrent.atomic.AtomicBoolean;


public class SharedBackgroundQueueDispatcher extends SignalDispatcher {

    private final int mSharedId;

    private BackgroundQueueDispatcher mBackgroundQueueDispatcher;

    private AtomicBoolean mIsShutDown = new AtomicBoolean(false);

    public SharedBackgroundQueueDispatcher(DispatchCallback dispatchCallback, StateMachineLogger logger, int sharedId) {
        super(dispatchCallback, logger);
        mSharedId = sharedId;
        mBackgroundQueueDispatcher = BackgroundQueuePool.getInstance().acquire(sharedId, dispatchCallback, logger);
    }

    @Override
    public void dispatch(Enum signal, SignalPayload payload) {
        if (!mIsShutDown.get()) {
            mBackgroundQueueDispatcher.dispatch(signal, payload);
        }
    }

    @Override
    public void shutDown() {
        mIsShutDown.set(true);
        mBackgroundQueueDispatcher = null;
        // Relinquish shuts down the queue if possible

        BackgroundQueuePool.getInstance().relinquish(mSharedId);
    }

    @Override
    protected void finalize() throws Throwable {
        shutDown();
        super.finalize();
    }
}
