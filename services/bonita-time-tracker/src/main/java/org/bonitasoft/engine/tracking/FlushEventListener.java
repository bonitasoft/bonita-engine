package org.bonitasoft.engine.tracking;


public interface FlushEventListener {

    void flush(final FlushEvent flushEvent) throws Exception;

}
