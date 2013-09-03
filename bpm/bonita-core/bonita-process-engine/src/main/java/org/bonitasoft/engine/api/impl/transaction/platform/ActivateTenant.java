/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.platform;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.jobs.BPMEventHandlingJob;
import org.bonitasoft.engine.jobs.CleanInvalidSessionsJob;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.scheduler.JobRegister;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
public final class ActivateTenant implements TransactionContent {

    public static final String CLEAN_INVALID_SESSIONS = "CleanInvalidSessions";

    public static final String BPM_EVENT_HANDLING = "BPMEventHandling";

    private final long tenantId;

    private final PlatformService platformService;

    private final SchedulerService schedulerService;

    private final NodeConfiguration plaformConfiguration;

    private final TechnicalLoggerService logger;

    private final WorkService workService;

    public ActivateTenant(final long tenantId, final PlatformService platformService, final SchedulerService schedulerService,
            final NodeConfiguration plaformConfiguration, final TechnicalLoggerService logger, final WorkService workService) {
        this.tenantId = tenantId;
        this.platformService = platformService;
        this.schedulerService = schedulerService;
        this.plaformConfiguration = plaformConfiguration;
        this.logger = logger;
        this.workService = workService;
    }

    @Override
    public void execute() throws SBonitaException {
        // pass -1 because the user is the technical user, which is inexistant in DB:
        final boolean tenantWasActivated = platformService.activateTenant(tenantId);
        // we execute that only if the tenant was not already activated
        if (tenantWasActivated) {
            workService.start(tenantId);
            startEventHandling();
            startCleanInvalidSessionsJob();
            final List<JobRegister> jobsToRegister = plaformConfiguration.getJobsToRegister();
            for (final JobRegister jobRegister : jobsToRegister) {
                jobRegister.registerJobIfNotRegistered();
            }
        }
    }

    private void startEventHandling() throws SSchedulerException, FireEventException {
        final String jobClassName = BPMEventHandlingJob.class.getName();
        if (schedulerService.isStarted()) {
            if (plaformConfiguration.shouldStartEventHandlingJob()) {
                final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder().createNewInstance(jobClassName, BPM_EVENT_HANDLING, true).done();
                final ArrayList<SJobParameter> jobParameters = new ArrayList<SJobParameter>();
                final String cron = plaformConfiguration.getEventHandlingJobCron(); //
                final Trigger trigger = new UnixCronTrigger("UnixCronTrigger" + UUID.randomUUID().getLeastSignificantBits(), new Date(), cron);
                logger.log(ProcessAPI.class, TechnicalLogSeverity.INFO, "Starting event handling job with frequency: " + cron);
                schedulerService.schedule(jobDescriptor, jobParameters, trigger);
            }
        } else {
            if (logger.isLoggable(ActivateTenant.class, TechnicalLogSeverity.WARNING)) {
                logger.log(ActivateTenant.class, TechnicalLogSeverity.WARNING, "The scheduler is not started: impossible to schedule job " + jobClassName);
            }
        }
    }

    private void startCleanInvalidSessionsJob() throws SSchedulerException, FireEventException {
        final String jobClassName = CleanInvalidSessionsJob.class.getName();
        if (schedulerService.isStarted()) {
            final String cron = plaformConfiguration.getCleanInvalidSessionsJobCron();
            if (!cron.equalsIgnoreCase("none")) {
                final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder().createNewInstance(jobClassName, CLEAN_INVALID_SESSIONS, true).done();
                final ArrayList<SJobParameter> jobParameters = new ArrayList<SJobParameter>();
                final Trigger trigger = new UnixCronTrigger("UnixCronTrigger" + UUID.randomUUID().getLeastSignificantBits(), new Date(), cron);
                logger.log(ProcessAPI.class, TechnicalLogSeverity.INFO, "Starting clean invalid sessions job with frequency: " + cron);
                schedulerService.schedule(jobDescriptor, jobParameters, trigger);
            }
        } else {
            if (logger.isLoggable(ActivateTenant.class, TechnicalLogSeverity.WARNING)) {
                logger.log(ActivateTenant.class, TechnicalLogSeverity.WARNING, "The scheduler is not started: impossible to schedule job " + jobClassName);
            }
        }
    }

}
