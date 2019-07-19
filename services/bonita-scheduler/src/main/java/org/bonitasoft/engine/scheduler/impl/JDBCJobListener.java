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
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.quartz.core.QuartzScheduler;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class JDBCJobListener implements BonitaJobListener {

    private final JobService jobService;
    private final TechnicalLogger logger;
    private SchedulerService schedulerService;

    public JDBCJobListener(final JobService jobService,
                           final TechnicalLoggerService logger,
                           SchedulerService schedulerService) {
        super();
        this.jobService = jobService;
        this.logger = logger.asLogger(JDBCJobListener.class);
        this.schedulerService = schedulerService;
    }

    @Override
    public void jobToBeExecuted(final Map<String, Serializable> context) {
        // nothing to do
    }

    @Override
    public void jobExecutionVetoed(final Map<String, Serializable> context) {
        // nothing to do
    }

    @Override
    public void jobWasExecuted(final Map<String, Serializable> context, final Exception jobException) {
        if (jobException != null) {
            logger.warn("An exception occurs during the job execution.", jobException);
            return;
        }
        Long jobDescriptorId = (Long) context.get(JOB_DESCRIPTOR_ID);
        if (isNullOrEmpty(jobDescriptorId)) {
            logger.warn("A quartz job was executed but is not a bonita Job, context: {}", context);
            return;
        }
        if (context.get(TRIGGER_NEXT_FIRE_TIME) == null) {

            //will not fire again
            deleteJobDescriptor(context, jobDescriptorId);
        }
    }

    private void deleteJobDescriptor(Map<String, Serializable> context, Long jobDescriptorId) {
        try {
            //delete job only if there is no other trigger
            if (schedulerService.getNumberOfTriggers(((String) context.get(JOB_GROUP)), ((String) context.get(JOB_NAME))) <= 1) {
                jobService.deleteJobDescriptor(jobDescriptorId);
            }
        } catch (final Exception e) {
            logger.warn("Unable to delete cleanup job {}", jobDescriptorId, e);
        }
    }

    private boolean isNullOrEmpty(final Long id) {
        return id == null || id == 0;
    }

}
