package com.jayway.annostatemachine.dispatchers;

import com.jayway.annostatemachine.utils.StateMachineLogger;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(MockitoJUnitRunner.class)
public class BackgroundQueuePoolTests {

    @Before
    public void setUp() {
        BackgroundQueuePool.reset();
    }

    @Mock
    StateMachineLogger mMockLogger;

    @Test
    public void testSameDispatcherReturnedForSameId() {
        BackgroundQueueDispatcher dispatcher1 = BackgroundQueuePool.getInstance().acquire(0);
        BackgroundQueueDispatcher dispatcher2 = BackgroundQueuePool.getInstance().acquire(0);
        assertEquals(dispatcher1, dispatcher2);
    }

    @Test
    public void testNotSameDispatcherReturnedForDifferentIds() {
        BackgroundQueueDispatcher dispatcher1 = BackgroundQueuePool.getInstance().acquire(0);
        BackgroundQueueDispatcher dispatcher2 = BackgroundQueuePool.getInstance().acquire(1);
        assertNotEquals(dispatcher1, dispatcher2);
    }

    @Test
    public void testNewDispatcherCreatedIfAllOfSameIdRelinquished() {
        BackgroundQueueDispatcher dispatcher1 = BackgroundQueuePool.getInstance().acquire(0);
        BackgroundQueuePool.getInstance().relinquish(0);
        BackgroundQueueDispatcher dispatcher2 = BackgroundQueuePool.getInstance().acquire(0);

        assertNotEquals(dispatcher1, dispatcher2);
    }

    @Test
    public void testNewDispatcherCreatedIfNullingReferences() throws InterruptedException {
        BackgroundQueueDispatcher dispatcher1 = BackgroundQueuePool.getInstance().acquire(0);
        BackgroundQueueDispatcher dispatcher2 = BackgroundQueuePool.getInstance().acquire(0);
        int dispatcher1Hash = System.identityHashCode(dispatcher1);
        int dispatcher2Hash = System.identityHashCode(dispatcher2);
        Assert.assertEquals(dispatcher1Hash, dispatcher2Hash);
        dispatcher1 = null;
        dispatcher2 = null;
        System.runFinalization();
        System.gc();
        BackgroundQueueDispatcher dispatcher3 = BackgroundQueuePool.getInstance().acquire(0);
        int dispatcher3Hash = System.identityHashCode(dispatcher3);
        assertNotEquals(dispatcher1Hash, dispatcher3Hash);

    }
}
