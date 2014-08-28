package org.bonitasoft.engine.scheduler.impl;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.bonitasoft.engine.scheduler.job.IncrementItselfJob;

/**
 * @author Baptiste Mesta
 */
public class WaitForIncrementJobToHaveValue  {

    private final int value;
    private final int timeout;
    private final int repeatEach;

    public WaitForIncrementJobToHaveValue(final int repeatEach, final int timeout, int value) {
        assertTrue("timeout is not big enough", repeatEach < timeout);
        this.repeatEach = repeatEach;
        this.timeout = timeout;
        this.value = value;
    }
    
    /**
     * @param timeout
     * @param value
     */
    public WaitForIncrementJobToHaveValue(final int timeout, final int value) {
        this(10, timeout, value);
    }

    boolean check() {
        return IncrementItselfJob.getValue() == value;
    }

    public boolean waitFor() throws InterruptedException {
        final long limit = new Date().getTime() + timeout;
        while (new Date().getTime() < limit) {
            Thread.sleep(repeatEach);
            if (check()) {
                return true;
            }
        }
        return check();
    }
}
