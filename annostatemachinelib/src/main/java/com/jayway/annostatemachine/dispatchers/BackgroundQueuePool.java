package com.jayway.annostatemachine.dispatchers;


import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class BackgroundQueuePool {

    private static BackgroundQueuePool sInstance;

    private WeakHashMap<BackgroundQueueDispatcher, Info> mPool = new WeakHashMap<>();

    private BackgroundQueuePool() {
        super();
    }

    synchronized static BackgroundQueuePool getInstance() {
        if (sInstance == null) {
            sInstance = new BackgroundQueuePool();
        }
        return sInstance;
    }

    synchronized static void reset() {
        if (sInstance != null) {
            sInstance.destroy();
            sInstance = null;
        }
    }

    private synchronized void destroy() {
        for (BackgroundQueueDispatcher dispatcher : mPool.keySet()) {
            dispatcher.shutDown();
        }
        mPool.clear();
    }

    synchronized BackgroundQueueDispatcher acquire(int sharedId) {
        BackgroundQueueDispatcher dispatcher = null;
        Info info = null;
        for (Map.Entry<BackgroundQueueDispatcher, Info> entry : mPool.entrySet()) {
            if (entry.getValue().id == sharedId) {
                info = entry.getValue();
                dispatcher = entry.getKey();
                break;
            }
        }

        if (info == null) {
            info = new Info(sharedId);
            info.count.incrementAndGet();
            dispatcher = new BackgroundQueueDispatcher();
        } else {
            info.count.incrementAndGet();
        }

        mPool.put(dispatcher, info);
        return dispatcher;
    }

    void relinquish(int sharedId) {
        BackgroundQueueDispatcher dispatcher = null;
        Info info = null;
        for (Map.Entry<BackgroundQueueDispatcher, Info> entry : mPool.entrySet()) {
            if (entry.getValue().id == sharedId) {
                info = entry.getValue();
                dispatcher = entry.getKey();
                break;
            }
        }

        if (info != null) {
            if (info.count.decrementAndGet() == 0) {
                dispatcher.shutDown();
                mPool.remove(dispatcher);
            }
        }
    }

    private static class Info {
        int id;
        AtomicInteger count;

        public Info(int id) {
            this.id = id;
            count = new AtomicInteger(0);
        }
    }
}
