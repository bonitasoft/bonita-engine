package org.bonitasoft.engine.scheduler.impl;

import org.quartz.JobListener;

public abstract class AbstractJobListener implements JobListener {

    private SchedulerServiceImpl schedulerService;

    public void setBOSSchedulerService(final SchedulerServiceImpl schedulerService) {
        this.schedulerService = schedulerService;
    }

    public SchedulerServiceImpl getSchedulerService() {
        return schedulerService;
    }

}
