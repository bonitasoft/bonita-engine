package org.bonitasoft.engine.tracking;

public class FlushResult {

    private final FlushEvent flushEvent;

    public FlushResult(final FlushEvent flushEvent) {
        super();
        this.flushEvent = flushEvent;
    }

    public FlushEvent getFlushEvent() {
        return flushEvent;
    }

}
