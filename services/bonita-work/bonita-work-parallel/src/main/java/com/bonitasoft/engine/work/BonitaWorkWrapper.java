/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
