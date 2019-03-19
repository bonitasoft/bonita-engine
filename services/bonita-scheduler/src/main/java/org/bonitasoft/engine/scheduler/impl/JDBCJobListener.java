/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.scheduler.impl;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.BonitaJobListener;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class JDBCJobListener implements BonitaJobListener {

    private final JobService jobService;
    private final TechnicalLogger logger;
    private final SchedulerService schedulerService;
    private final SchedulerExecutor schedulerExecutor;

    public JDBCJobListener(final SchedulerService schedulerService, final JobService jobService, final SchedulerExecutor schedulerExecutor,
            final TechnicalLoggerService logger) {
        super();
        this.schedulerService = schedulerService;
        this.jobService = jobService;
        this.schedulerExecutor = schedulerExecutor;
        this.logger = logger.asLogger(JDBCJobListener.class);
    }

    @Override
    public void jobToBeExecuted(final Map<String, Serializable> context) {
        final Long jobDescriptorId = (Long) context.get(JOB_DESCRIPTOR_ID);
        final Long tenantId = (Long) context.get(TENANT_ID);
        if (isSessionRelated(jobDescriptorId, tenantId)) {
            deleteRelatedJob(context, jobDescriptorId, tenantId);
        }
    }

    private void deleteRelatedJob(final Map<String, Serializable> context, final Long jobDescriptorId, final Long tenantId) {
        try {
            final SJobDescriptor sJobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
            if (sJobDescriptor == null) {
                deleteJob(context, jobDescriptorId, tenantId);
            }
        } catch (final Exception e) {
            logger.warn("An exception occurs during the check of the existence of the job descriptor '{}'.", jobDescriptorId, e);
        }
    }

    private void deleteJob(final Map<String, Serializable> context, final Long jobDescriptorId, final Long tenantId) throws SSchedulerException {

        logger.warn("No job descriptor found with id '{}'. It was probably deleted during the scheduler executed it.", jobDescriptorId);

        final String jobName = (String) context.get(JOB_NAME);
        schedulerExecutor.delete(jobName, String.valueOf(tenantId));
    }

    @Override
    public void jobExecutionVetoed(final Map<String, Serializable> context) {
        // nothing to do
    }

    @Override
    public void jobWasExecuted(final Map<String, Serializable> context, final Exception jobException) {
        final StatelessJob bosJob = (StatelessJob) context.get(BOS_JOB);
        if (bosJob == null) {
            return;
        }

        final Long jobDescriptorId = (Long) context.get(JOB_DESCRIPTOR_ID);
        final Long tenantId = (Long) context.get(TENANT_ID);
        if (isSessionRelated(jobDescriptorId, tenantId)) {
            performPostExecutionActions(jobException, jobDescriptorId);
        } else {
            logger.warn("An exception occurs during the job execution.", jobException);
        }
    }

    private boolean isSessionRelated(final Long jobDescriptorId, final Long tenantId) {
        return isNotNullOrEmpty(jobDescriptorId) && isNotNullOrEmpty(tenantId);
    }

    private void performPostExecutionActions(final Exception jobException, final Long jobDescriptorId) {
        try {
            if (jobException == null) {
                jobService.deleteJobLogs(jobDescriptorId);
                deleteJobIfNotScheduledAnyMore(jobDescriptorId);
            }
        } catch (final Exception e) {
            logger.warn("Unable to delete the job logs for job {}", jobDescriptorId, e);
        }
    }

    private boolean isNotNullOrEmpty(final Long id) {
        return id != null && id != 0;
    }

    private void deleteJobIfNotScheduledAnyMore(final Long jobDescriptorId) throws SSchedulerException {
        final SJobDescriptor jobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
        if (jobDescriptor != null && !schedulerService.isStillScheduled(jobDescriptor)) {
            schedulerService.delete(jobDescriptor.getJobName());
        } else if (jobDescriptor == null) {
            logger.trace("jobDescriptor with id {} already deleted, ignore it", jobDescriptorId);
        }
    }

}
