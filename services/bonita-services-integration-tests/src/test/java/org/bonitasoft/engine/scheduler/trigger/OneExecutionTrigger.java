package org.bonitasoft.engine.scheduler.trigger;

import java.util.Date;

/**
 * @author Matthieu Chaffotte
 */
public class OneExecutionTrigger implements Trigger {

    private final String name;

    private final Date startDate;

    private final int priority;

    public OneExecutionTrigger(final String name, final Date startDate, final int priority) {
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
