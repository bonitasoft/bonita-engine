package org.bonitasoft.engine.work;

import static org.bonitasoft.engine.work.WorkerThreadFactory.guessPadding;
import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class WorkerThreadFactoryTest {

    @Test
    public void newThreadStartsWithName() throws Exception {
        WorkerThreadFactory threadFactory = new WorkerThreadFactory("foo", 1);
        Thread newThread = threadFactory.newThread(buildRunnable());
        assertEquals("foo-1", newThread.getName());
    }

    @Test
    public void newThreadNameNumberIsPadded() throws Exception {
        WorkerThreadFactory threadFactory = new WorkerThreadFactory("foo", 100);
        Thread newThread = threadFactory.newThread(buildRunnable());
        assertEquals("foo-001", newThread.getName());
    }

    @Test
    public void testPadding() throws Exception {
        assertEquals(1, guessPadding(1));
        assertEquals(2, guessPadding(10));
        assertEquals(3, guessPadding(100));
        assertEquals(4, guessPadding(1000));
    }

    /**
     * @return
     */
    private Runnable buildRunnable() {
        return new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

            }
        };
    }
}
