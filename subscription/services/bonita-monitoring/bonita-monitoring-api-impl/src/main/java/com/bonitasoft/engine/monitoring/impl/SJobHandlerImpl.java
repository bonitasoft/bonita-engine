/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

import java.util.UUID;

import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;

/**
 * * This handler will handle the following events :
 * <ul>
 * <li>JOB_EXECUTING = "JOB_EXECUTING"</li>
 * <li>JOB_FAILED = "JOB_FAILED"</li>
 * <li>JOB_COMPLETED = "JOB_COMPLETED"</li>
 * </ul>
 *
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 */
public class SJobHandlerImpl implements SHandler<SEvent> {

    private static final long serialVersionUID = 1L;

    public static final String JOB_EXECUTING = "JOB_EXECUTING";

    public static final String JOB_FAILED = "JOB_FAILED";

    public static final String JOB_COMPLETED = "JOB_COMPLETED";

    private int executingJobs = 0;

    private final String identifier;

    public SJobHandlerImpl() {
        this.identifier = UUID.randomUUID().toString();
    }

    @Override
    public void execute(final SEvent event) {
        final String type = event.getType();
        if (JOB_EXECUTING.compareToIgnoreCase(type) == 0) {
            executingJobs++;
        } else if (JOB_COMPLETED.compareToIgnoreCase(type) == 0) {
            executingJobs--;
        } else if (JOB_FAILED.compareToIgnoreCase(type) == 0) {
            executingJobs--;
        }
    }

    @Override
    public boolean isInterested(final SEvent event) {
        // FIXME filter by tenant id
        final String type = event.getType();
        if (JOB_COMPLETED.compareToIgnoreCase(type) == 0) {
            return true;
        } else if (JOB_EXECUTING.compareToIgnoreCase(type) == 0) {
            return true;
        } else if (JOB_FAILED.compareToIgnoreCase(type) == 0) {
            return true;
        }
        return false;
    }

    public int getExecutingJobs() {
        return executingJobs;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

}
