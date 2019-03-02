package org.bonitasoft.engine.tracking;

import java.util.List;

/**
 * @author Charles Souillard
 */
public class FlushResult {

    private final long flushTime;

    private final List<FlushEventListenerResult> flushEventListenerResults;

    public FlushResult(final long flushTime, final List<FlushEventListenerResult> flushEventListenerResults) {
        this.flushTime = flushTime;
        this.flushEventListenerResults = flushEventListenerResults;
    }

    public List<FlushEventListenerResult> getFlushEventListenerResults() {
        return this.flushEventListenerResults;
    }

    public long getFlushTime() {
        return this.flushTime;
    }
}
