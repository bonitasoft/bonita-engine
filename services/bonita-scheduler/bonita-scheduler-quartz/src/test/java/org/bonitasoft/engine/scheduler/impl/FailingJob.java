package org.bonitasoft.engine.scheduler.impl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class FailingJob implements Job {

    private FailingJob() {
    }

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        throw new JobExecutionException();
    }

}
