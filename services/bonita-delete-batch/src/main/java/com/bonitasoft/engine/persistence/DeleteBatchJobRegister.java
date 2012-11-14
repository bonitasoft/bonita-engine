/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package com.bonitasoft.engine.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SJobDescriptor;
import org.bonitasoft.engine.scheduler.SJobParameter;
import org.bonitasoft.engine.scheduler.SSchedulerException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.Trigger;
import org.bonitasoft.engine.scheduler.UnixCronTrigger;
import org.bonitasoft.engine.services.PersistenceService;

/**
 * @author Baptiste Mesta
 */
public class DeleteBatchJobRegister implements RestartHandler {

    private boolean mustStartJob = false;

    private static final String DELETE_BATCH_JOB = "DeleteBatchJob";

    private static DeleteBatchJobRegister INSTANCE;

    private final SchedulerService schedulerService;

    private final TechnicalLoggerService loggerService;

    private final String repeat;

    /**
     * @param persistenceService
     * @param schedulerService
     * @param loggerService
     * @param repeat
     *            cron expression to tell when the job must be run
     *            e.g. * *\/2 * * * ? to run it every 2 minutes
     */
    public DeleteBatchJobRegister(final PersistenceService persistenceService, final SchedulerService schedulerService,
            final TechnicalLoggerService loggerService, final String repeat) {
        this.schedulerService = schedulerService;
        this.loggerService = loggerService;
        this.repeat = repeat;
        DeleteBatchJob.setPersistenceService(persistenceService);
        INSTANCE = this;
    }

    /**
     * @return
     */
    public static DeleteBatchJobRegister getInstance() {
        return INSTANCE;
    }

    public void registerWorkIfNotRegistered() {
        if (mustStartJob) {
            try {
                List<String> jobs;
                jobs = schedulerService.getAllJobs();
                if (!jobs.contains(DELETE_BATCH_JOB)) {
                    loggerService.log(this.getClass(), TechnicalLogSeverity.INFO, "Register delete batch job with repeat cron: " + repeat);
                    final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                            .createNewInstance(DeleteBatchJob.class.getName(), DELETE_BATCH_JOB).done();
                    final ArrayList<SJobParameter> jobParameters = new ArrayList<SJobParameter>();
                    final Trigger trigger = new UnixCronTrigger("UnixCronTrigger" + UUID.randomUUID().getLeastSignificantBits(), new Date(), repeat);
                    schedulerService.schedule(jobDescriptor, jobParameters, trigger);
                } else {
                    loggerService.log(this.getClass(), TechnicalLogSeverity.INFO, "The delete job was already started");
                }
            } catch (final SSchedulerException e) {
                loggerService.log(this.getClass(), TechnicalLogSeverity.ERROR, "Unable to register job because " + e.getMessage());
                if (loggerService.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                    loggerService.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
                }
            } catch (final FireEventException e) {
                loggerService.log(this.getClass(), TechnicalLogSeverity.ERROR, "Unable to register job because " + e.getMessage());
                if (loggerService.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                    loggerService.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
                }
            }
            mustStartJob = false;
        }
    }

    /*
     * Executed when the platform is started
     */
    @Override
    public void execute() throws SBonitaException {
        mustStartJob = true;
    }
}
