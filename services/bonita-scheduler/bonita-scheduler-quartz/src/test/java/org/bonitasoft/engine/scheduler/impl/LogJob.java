package org.bonitasoft.engine.scheduler.impl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class LogJob implements Job {

    @SuppressWarnings("unused")
    @Override
    public void execute(final JobExecutionContext context) {
        System.out.println("It works !!!!!!!!!!!! ");
    }

}
