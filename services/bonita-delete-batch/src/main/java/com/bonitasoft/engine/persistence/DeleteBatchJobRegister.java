/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobRegister;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.bonitasoft.engine.services.PersistenceService;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class DeleteBatchJobRegister implements JobRegister {

    private volatile boolean mustStartJob = false;

    private static final String DELETE_BATCH_JOB = "DeleteBatchJob";

    private static DeleteBatchJobRegister INSTANCE;

    private final SchedulerService schedulerService;

    private final TechnicalLoggerService loggerService;

    private final String repeat;

    /**
     * @param persistenceService
     * @param schedulerService
     * @param loggerService
     * @param transactionService
     * @param classesToPurge
     * @param repeat
     *            cron expression to tell when the job must be run
     *            e.g. * *\/2 * * * ? to run it every 2 minutes
     */
    public DeleteBatchJobRegister(final PersistenceService persistenceService, final SchedulerService schedulerService,
            final TechnicalLoggerService loggerService, final List<String> classesToPurge, final String repeat) {
        this.schedulerService = schedulerService;
        this.loggerService = loggerService;
        this.repeat = repeat;
        DeleteBatchJob.setPersistenceService(persistenceService);
        DeleteBatchJob.setClassesToPurge(classesToPurge);
        mustStartJob = true;
        INSTANCE = this;
    }

    public static DeleteBatchJobRegister getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerJobIfNotRegistered() {
        if (mustStartJob) {
            synchronizedRegister();
        }
    }

    private synchronized void synchronizedRegister() {
        if (mustStartJob) {
            try {
                List<String> jobs;
                jobs = schedulerService.getAllJobs();
                if (!jobs.contains(DELETE_BATCH_JOB)) {
                    scheduleDeleteJob();
                } else {
                    loggerService.log(this.getClass(), TechnicalLogSeverity.INFO, "The delete job was already started");
                }
            } catch (final SSchedulerException e) {
                loggerService.log(this.getClass(), TechnicalLogSeverity.ERROR, "Unable to register job because " + e.getMessage());
                if (loggerService.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                    loggerService.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
                }
            } finally {
                mustStartJob = false;
            }
        }
    }

    /**
     * @throws SSchedulerException
     */
    private void scheduleDeleteJob() throws SSchedulerException {
        loggerService.log(this.getClass(), TechnicalLogSeverity.INFO, "Register delete batch job with repeat cron: " + repeat);
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class).createNewInstance(DeleteBatchJob.class.getName(), DELETE_BATCH_JOB, true).done();
        final ArrayList<SJobParameter> jobParameters = new ArrayList<SJobParameter>();
        final Trigger trigger = new UnixCronTrigger("UnixCronTrigger" + UUID.randomUUID().getLeastSignificantBits(), new Date(), repeat);
        schedulerService.schedule(jobDescriptor, jobParameters, trigger);
    }

}
