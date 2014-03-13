/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services.monitoring;

import java.util.Date;

import org.bonitasoft.engine.scheduler.trigger.Trigger;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class OneShotTrigger implements Trigger {

    private final String name;

    private final Date startDate;

    private final int priority;

    public OneShotTrigger(final String name, final Date startDate, final int priority) {
        this.name = name;
        this.startDate = startDate;
        this.priority = priority;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public MisfireRestartPolicy getMisfireHandlingPolicy() {
        return MisfireRestartPolicy.ALL;
    }

}
