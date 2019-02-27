package org.bonitasoft.engine.connector.impl;

import java.util.concurrent.Callable;

public interface InterruptibleCallable<T> extends Callable<T> {

    void interrupt();
}
