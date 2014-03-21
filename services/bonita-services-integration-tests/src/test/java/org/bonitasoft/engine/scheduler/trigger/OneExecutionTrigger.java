package org.bonitasoft.engine.scheduler.trigger;

import java.util.Date;

/**
 * @author Matthieu Chaffotte
 */
public class OneExecutionTrigger implements Trigger {

    private final String name;

    private final Date startDate;

    private final int priority;

    private final MisfireRestartPolicy misfireHandlingPolicy;

    public OneExecutionTrigger(final String name, final Date startDate, final int priority) {
        this(name, startDate, priority, MisfireRestartPolicy.ALL);
    }

    public OneExecutionTrigger(final String name, final Date startDate, final int priority, final MisfireRestartPolicy misfireHandlingPolicy) {
        this.name = name;
        this.startDate = startDate;
        this.priority = priority;
        this.misfireHandlingPolicy = misfireHandlingPolicy;
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
        return misfireHandlingPolicy;
    }

}
