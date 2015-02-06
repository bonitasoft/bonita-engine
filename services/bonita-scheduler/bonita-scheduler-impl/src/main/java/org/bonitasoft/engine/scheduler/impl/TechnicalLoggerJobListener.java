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
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.bonitasoft.engine.scheduler.model.SJobData;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class TechnicalLoggerJobListener extends AbstractBonitaPlatformJobListener {

    private static final long serialVersionUID = 2830540082890033377L;

    private static final String jobToBeFiredMessage = "Job FIRED : group=''{1}'', name=''{0}'', class=''{5}'', data=''{6}'', triggerGroup=''{4}'', triggerName=''{3}'', at=''{2, date,HH:mm:ss MM/dd/yyyy}''";

    private static final String jobSuccessMessage = "Job COMPLETED : group=''{1}'', name=''{0}'', class=''{4}'', data=''{5}'', at=''{2, date,HH:mm:ss MM/dd/yyyy}'', reports=''{3}''";

    private static final String jobFailedMessage = "Job FAILED : group=''{1}'', name=''{0}'', class=''{4}'', data=''{5}'', at=''{2, date,HH:mm:ss MM/dd/yyyy}'', reports=''{3}''";

    private static final String jobWasVetoedMessage = "Job VETOED : group=''{1}'', name=''{0}'', class=''{5}'', triggerGroup=''{4}'', triggerName=''{3}'', at=''{2, date,HH:mm:ss MM/dd/yyyy}''";

    private final TechnicalLoggerService logger;

    private final boolean trace;

    private final boolean warning;

    public TechnicalLoggerJobListener(final TechnicalLoggerService logger) {
        this.logger = logger;
        trace = logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE);
        warning = logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING);
    }

    @Override
    public String getName() {
        return "TechnicalLoggerJobListener";
    }

    @Override
    public void jobToBeExecuted(final Map<String, Serializable> context) {
        if (trace) {
            final String jobName = (String) context.get(JOB_NAME);
            final String jobGroup = (String) context.get(JOB_GROUP);
            final String triggerName = (String) context.get(TRIGGER_NAME);
            final String triggerGroup = (String) context.get(TRIGGER_GROUP);
            final Date triggerNextFireTime = (Date) context.get(TRIGGER_NEXT_FIRE_TIME);
            final Date triggerPreviousFireTime = (Date) context.get(TRIGGER_PREVIOUS_FIRE_TIME);
            final String jobType = (String) context.get(JOB_TYPE);
            final Integer refireCount = (Integer) context.get(REFIRE_COUNT);
            final List<SJobData> jobDataValueAndTypes = (List<SJobData>) context.get(JOB_DATAS);

            final Object[] args = { jobName, jobGroup, new java.util.Date(), triggerName, triggerGroup, jobType, jobDataValueAndTypes, triggerPreviousFireTime,
                    triggerNextFireTime, refireCount };

            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, MessageFormat.format(jobToBeFiredMessage, args));
        }
    }

    @Override
    public void jobExecutionVetoed(final Map<String, Serializable> context) {
        if (trace) {
            final String jobName = (String) context.get(JOB_NAME);
            final String jobGroup = (String) context.get(JOB_GROUP);
            final String triggerName = (String) context.get(TRIGGER_NAME);
            final String triggerGroup = (String) context.get(TRIGGER_GROUP);
            final Date triggerNextFireTime = (Date) context.get(TRIGGER_NEXT_FIRE_TIME);
            final Date triggerPreviousFireTime = (Date) context.get(TRIGGER_PREVIOUS_FIRE_TIME);
            final String jobType = (String) context.get(JOB_TYPE);
            final Integer refireCount = (Integer) context.get(REFIRE_COUNT);

            final Object[] args = { jobName, jobGroup, new java.util.Date(), triggerName, triggerGroup, jobType, triggerPreviousFireTime, triggerNextFireTime,
                    refireCount };
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, MessageFormat.format(jobWasVetoedMessage, args));
        }
    }

    @Override
    public void jobWasExecuted(final Map<String, Serializable> context, final Exception jobException) {
        final String jobName = (String) context.get(JOB_NAME);
        final String jobGroup = (String) context.get(JOB_GROUP);
        final String triggerName = (String) context.get(TRIGGER_NAME);
        final String triggerGroup = (String) context.get(TRIGGER_GROUP);
        final Date triggerNextFireTime = (Date) context.get(TRIGGER_NEXT_FIRE_TIME);
        final Date triggerPreviousFireTime = (Date) context.get(TRIGGER_PREVIOUS_FIRE_TIME);
        final String jobType = (String) context.get(JOB_TYPE);
        final String jobResult = (String) context.get(JOB_RESULT);
        final Integer refireCount = (Integer) context.get(REFIRE_COUNT);
        final List<SJobData> jobDataValueAndTypes = (List<SJobData>) context.get(JOB_DATAS);

        if (jobException != null) {
            if (warning) {
                final Object[] args = new Object[] { jobName, jobGroup, new java.util.Date(), jobException.getMessage(), jobType, jobDataValueAndTypes,
                        triggerName, triggerGroup, triggerPreviousFireTime, triggerNextFireTime, refireCount };
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING, MessageFormat.format(jobFailedMessage, args), jobException);
            }
        } else {
            if (trace) {
                final Object[] args = new Object[] { jobName, jobGroup, new java.util.Date(), jobResult, jobType, jobDataValueAndTypes, triggerName,
                        triggerGroup, triggerPreviousFireTime, triggerNextFireTime, refireCount };
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, MessageFormat.format(jobSuccessMessage, args));
            }
        }
    }

}
