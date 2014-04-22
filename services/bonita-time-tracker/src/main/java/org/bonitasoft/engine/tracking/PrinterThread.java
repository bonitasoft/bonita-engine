package org.bonitasoft.engine.tracking;

public class PrinterThread extends Thread {

    private final RecordKeeper recordKeeper;

    private final int flushIntervalInMiliSeconds;

    public PrinterThread(final int flushIntervalInSeconds, final RecordKeeper recordKeeper) {
        this.flushIntervalInMiliSeconds = flushIntervalInSeconds * 1000;
        this.recordKeeper = recordKeeper;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(flushIntervalInMiliSeconds);
                this.recordKeeper.flush();
            }
        } catch (Exception e) {
            // TODO use logger
            e.printStackTrace();
        }
    }

}
