/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.connector.impl;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Baptiste Mesta
 */
public class ConnectorExecutorThreadFactory implements ThreadFactory {

    private static AtomicInteger nbThread = new AtomicInteger(1);

    private final String name;

    public ConnectorExecutorThreadFactory(final String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        return new Thread(runnable, name + "-" + nbThread.getAndIncrement());
    }

}
