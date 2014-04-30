package org.bonitasoft.engine.tracking;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ThreadSleepClockImplTest {

    @Test
    public void should_sleep_given_time() throws Exception {
        final ThreadSleepClockImpl clock = new ThreadSleepClockImpl();
        final long millis = 10L;
        final long startTime = System.currentTimeMillis();
        clock.sleep(millis);
        final long endTime = System.currentTimeMillis();
        assertTrue((endTime - startTime) >= millis);
    }

}
