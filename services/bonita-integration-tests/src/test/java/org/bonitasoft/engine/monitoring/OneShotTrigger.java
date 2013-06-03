package org.bonitasoft.engine.monitoring;

import java.util.Date;

import org.bonitasoft.engine.scheduler.Trigger;

/**
 * @author Matthieu Chaffotte
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

}
