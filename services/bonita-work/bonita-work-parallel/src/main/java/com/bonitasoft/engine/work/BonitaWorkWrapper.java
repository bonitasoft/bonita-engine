/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.work;

import org.bonitasoft.engine.work.BonitaRunnable;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Baptiste Mesta
 */
public class BonitaWorkWrapper extends BonitaRunnable {

    /**
     * 
     */
    private static final long serialVersionUID = 2083826139943818362L;

    private final BonitaWork work;

    private boolean cancelled = false;

    public BonitaWorkWrapper(final long tenantId, final BonitaWork work) {
        super(tenantId);
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
