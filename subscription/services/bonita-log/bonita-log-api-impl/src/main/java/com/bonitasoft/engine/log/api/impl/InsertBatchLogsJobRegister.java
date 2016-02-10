/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log.api.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.bonitasoft.engine.scheduler.JobRegister;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger.MisfireRestartPolicy;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;

public class InsertBatchLogsJobRegister implements JobRegister {

    private static final String INSERT_BATCH_LOGS_JOB = "InsertBatchLogsJob";

    private final String cronExpression;

    /**
     * @param repeat
     *            cron expression to tell when the job must be run
     *            e.g. * *\/2 * * * ? to run it every 2 minutes
     */
    public InsertBatchLogsJobRegister(final String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public String getJobName() {
        return INSERT_BATCH_LOGS_JOB;
    }

    @Override
    public boolean canBeExecutedConcurrently() {
        return true;
    }

    @Override
    public Trigger getTrigger() {
        return new UnixCronTrigger("UnixCronTrigger" + UUID.randomUUID().getLeastSignificantBits(), new Date(), cronExpression, MisfireRestartPolicy.NONE);
    }

    @Override
    public Class<?> getJobClass() {
        return InsertBatchLogsJob.class;
    }

    @Override
    public Map<String, Serializable> getJobParameters() {
        return Collections.emptyMap();
    }

    @Override
    public String getJobDescription() {
        return "insert batch logs with repeat cron: " + cronExpression;
    }

}
