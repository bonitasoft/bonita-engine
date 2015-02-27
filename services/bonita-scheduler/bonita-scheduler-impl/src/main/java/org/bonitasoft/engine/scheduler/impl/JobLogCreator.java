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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorReadException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogCreationException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class JobLogCreator {

    private final JobService jobService;
    private final TechnicalLoggerService logger;

    public JobLogCreator(JobService jobService, TechnicalLoggerService logger) {
        this.jobService = jobService;
        this.logger = logger;
    }

    public void createJobLog(final Exception jobException, final Long jobDescriptorId) throws SJobLogCreationException, SJobDescriptorReadException {
        SJobDescriptor jobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
        if (jobDescriptor != null) {
            final SJobLogImpl jobLog = new SJobLogImpl(jobDescriptorId);
            jobLog.setLastMessage(getStackTrace(jobException));
            jobLog.setRetryNumber(0L);
            jobLog.setLastUpdateDate(System.currentTimeMillis());
            jobService.createJobLog(jobLog);
        } else {
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(getClass(), TechnicalLogSeverity.WARNING, "Impossible to mark the job with id '" + jobDescriptorId
                        + "' as failed because no job was found for this identifier. It was probably removed just after its failure and before this action.");
            }
        }
    }

    private String getStackTrace(final Exception jobException) {
        final StringWriter exceptionWriter = new StringWriter();
        jobException.printStackTrace(new PrintWriter(exceptionWriter));
        return exceptionWriter.toString();
    }

}
