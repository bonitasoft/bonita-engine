package org.bonitasoft.engine.tracking;

public class FlushThread extends Thread {

    private final TimeTracker timeTracker;

    private final int flushIntervalInMiliSeconds;

    public FlushThread(final int flushIntervalInSeconds, final TimeTracker timeTracker) {
        super("TimeTracker-FlushThread");
        this.flushIntervalInMiliSeconds = flushIntervalInSeconds * 1000;
        this.timeTracker = timeTracker;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(flushIntervalInMiliSeconds);
                this.timeTracker.flush();
            }
        } catch (Exception e) {
            // TODO use logger
            e.printStackTrace();
        }
    }

}
