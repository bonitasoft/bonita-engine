package org.bonitasoft.engine.tracking;

public interface Clock {

    boolean sleep(final long millis) throws InterruptedException;

}
