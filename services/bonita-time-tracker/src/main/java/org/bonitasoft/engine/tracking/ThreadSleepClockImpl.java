package org.bonitasoft.engine.tracking;

public class ThreadSleepClockImpl implements Clock {

    @Override
    public boolean sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
        return true;
    }
}
