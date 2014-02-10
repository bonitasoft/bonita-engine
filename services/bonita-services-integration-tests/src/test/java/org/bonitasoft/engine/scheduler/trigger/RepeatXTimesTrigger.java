package org.bonitasoft.engine.scheduler.trigger;

import java.util.Date;

/**
 * @author Matthieu Chaffotte
 */
public class RepeatXTimesTrigger extends OneExecutionTrigger implements RepeatTrigger {

    private final int count;

    private final long interval;

    public RepeatXTimesTrigger(final String name, final Date startDate, final int priority, final int count, final long interval) {
        super(name, startDate, priority);
        this.count = count;
        this.interval = interval;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public long getInterval() {
        return this.interval;
    }

}
