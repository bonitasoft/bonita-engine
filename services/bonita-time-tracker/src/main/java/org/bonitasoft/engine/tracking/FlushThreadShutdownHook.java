package org.bonitasoft.engine.tracking;

class FlushThreadShutdownHook extends Thread {

    private final FlushThread flushThread;

    public FlushThreadShutdownHook(final FlushThread flushThread) {
        super();
        this.flushThread = flushThread;
    }

    @Override
    public void run() {
        if (this.flushThread.isAlive()) {
            this.flushThread.interrupt();
        }

    }
}