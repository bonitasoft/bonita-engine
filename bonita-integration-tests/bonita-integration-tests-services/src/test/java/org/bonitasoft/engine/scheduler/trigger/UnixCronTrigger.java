package org.bonitasoft.engine.scheduler.trigger;

import java.util.Date;

/**
 * @author Matthieu Chaffotte
 */
public class UnixCronTrigger extends OneExecutionTrigger implements CronTrigger {

    private final String expression;

    private final Date endDate;

    public UnixCronTrigger(final String name, final Date startDate, final int priority, final String expression) {
        super(name, startDate, priority);
        this.expression = expression;
        this.endDate = null;
    }

    public UnixCronTrigger(final String name, final Date startDate, final int priority, final String expression, final MisfireRestartPolicy misfireRestartPolicy) {
        super(name, startDate, priority, misfireRestartPolicy);
        this.expression = expression;
        this.endDate = null;
    }

    public UnixCronTrigger(final String name, final Date startDate, final int priority, final String expression, final Date endDate) {
        super(name, startDate, priority);
        this.expression = expression;
        this.endDate = endDate;
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

    @Override
    public Date getEndDate() {
        return this.endDate;
    }

}
