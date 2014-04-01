/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bonitasoft.engine.scheduler.JobRegister;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger.MisfireRestartPolicy;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class DeleteBatchJobRegister implements JobRegister {

    private static final String DELETE_BATCH_JOB = "DeleteBatchJob";

    private final String repeat;

    private final List<String> classesToPurge;

    /**
     * @param classesToPurge
     * @param repeat
     *            cron expression to tell when the job must be run
     *            e.g. * *\/2 * * * ? to run it every 2 minutes
     */
    public DeleteBatchJobRegister(final List<String> classesToPurge, final String repeat) {
        this.classesToPurge = classesToPurge;
        this.repeat = repeat;
    }

    @Override
    public String getJobName() {
        return DELETE_BATCH_JOB;
    }

    @Override
    public boolean canBeExecutedConcurrently() {
        return true;
    }

    @Override
    public Trigger getTrigger() {
        return new UnixCronTrigger("UnixCronTrigger" + UUID.randomUUID().getLeastSignificantBits(), new Date(), repeat, MisfireRestartPolicy.NONE);
    }

    @Override
    public Class<?> getJobClass() {
        return DeleteBatchJob.class;
    }

    @Override
    public Map<String, Serializable> getJobParameters() {
        return Collections.singletonMap(DeleteBatchJob.ATTR_CLASSES_TO_PURE, (Serializable) classesToPurge);
    }

    @Override
    public String getJobDescription() {
        return "delete batch job with repeat cron: " + repeat;
    }

}
