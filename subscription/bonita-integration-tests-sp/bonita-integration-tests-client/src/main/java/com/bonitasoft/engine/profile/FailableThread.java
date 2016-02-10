/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

final class FailableThread extends Thread {

    private Throwable exception;
    private final FailableRunnable r;

    FailableThread(final String string, final FailableRunnable r) {
        super(r, string);
        this.r = r;
    }

    @Override
    public void run() {
        super.run();
        exception = r.getException();
    }


    public Throwable getException() {
        return exception;
    }
}