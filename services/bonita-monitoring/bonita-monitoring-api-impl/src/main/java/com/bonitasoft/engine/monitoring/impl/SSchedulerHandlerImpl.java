/*******************************************************************************
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;

/**
 * @author Christophe Havard
 */
public class SSchedulerHandlerImpl implements SHandler<SEvent> {

    private static final long serialVersionUID = 1L;

    public static final String SCHEDULER_STARTED = "SCHEDULER_STARTED";

    public static final String SCHEDULER_STOPPED = "SCHEDULER_STOPPED";

    private boolean isSchedulerStarted = false;

    @Override
    public void execute(final SEvent event) {
        final String type = event.getType();
        if (SCHEDULER_STARTED.compareToIgnoreCase(type) == 0) {
            isSchedulerStarted = true;
        } else if (SCHEDULER_STOPPED.compareToIgnoreCase(type) == 0) {
            isSchedulerStarted = false;
        }
    }

    @Override
    public boolean isInterested(final SEvent event) {
        final String type = event.getType();
        if (SCHEDULER_STARTED.compareToIgnoreCase(type) == 0) {
            return true;
        } else if (SCHEDULER_STOPPED.compareToIgnoreCase(type) == 0) {
            return true;
        }
        return false;
    }

    public boolean isSchedulerStarted() {
        return isSchedulerStarted;
    }

}
