package org.bonitasoft.engine.tracking;

public interface FlushEventListener {

    FlushResult flush(final FlushEvent flushEvent) throws Exception;

}
