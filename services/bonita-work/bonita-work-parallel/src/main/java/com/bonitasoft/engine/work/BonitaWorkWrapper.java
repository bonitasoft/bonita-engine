/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.work;

import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.NotifyingRunnable;
import org.bonitasoft.engine.work.RunnableListener;

/**
 * @author Baptiste Mesta
 */
public class BonitaWorkWrapper extends NotifyingRunnable {

    private final BonitaWork work;

    private boolean cancelled = false;

    /**
     * @param runnableListener
     * @param tenantId
     */
    public BonitaWorkWrapper(final RunnableListener runnableListener, final long tenantId, final BonitaWork work) {
        super(runnableListener, tenantId);
        this.work = work;
    }

    @Override
    public void innerRun() {
        if (!cancelled) {
            work.run();
        }
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

}
