package org.bonitasoft.engine.scheduler.impl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class LogJob implements Job {

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        System.out.println("It works !!!!!!!!!!!! ");
    }

}
