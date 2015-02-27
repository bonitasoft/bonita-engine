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
package org.bonitasoft.engine.jobs;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * @author Matthieu Chaffotte
 */
public class ExecuteAgainJobSynchronization implements BonitaTransactionSynchronization {

    private final String jobName;

    private final JobService jobService;

    private final SchedulerService schedulerService;

    private final TechnicalLoggerService loggerService;

    public ExecuteAgainJobSynchronization(final String jobName, final JobService jobService, final SchedulerService schedulerService,
            final TechnicalLoggerService loggerService) {
        this.jobName = jobName;
        this.jobService = jobService;
        this.schedulerService = schedulerService;
        this.loggerService = loggerService;
    }

    @Override
    public void beforeCommit() {
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(new FilterOption(SJobDescriptor.class, "jobName", jobName));
        final QueryOptions queryOptions = new QueryOptions(0, 1, null, filters, null);
        try {
            final List<SJobDescriptor> jobDescriptors = jobService.searchJobDescriptors(queryOptions);
            final SJobDescriptor sJobDescriptor = jobDescriptors.get(0);
            schedulerService.executeAgain(sJobDescriptor.getId());
        } catch (final SBonitaException sbe) {
            if (loggerService.isLoggable(ExecuteAgainJobSynchronization.class, TechnicalLogSeverity.WARNING)) {
                loggerService.log(ExecuteAgainJobSynchronization.class, TechnicalLogSeverity.WARNING, "Unable to reschedule the job: " + jobName, sbe);
            }
        }
    }

    @Override
    public void afterCompletion(final TransactionState txState) {
        // Nothing to do (not in an active transaction)
    }

}
