/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.job;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.test.CommonAPILocalIT;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// because of waituntil but its the only class where we use failed jobs... so i don't want to add a handler and so on
// only for jobs
@SuppressWarnings("deprecation")
public class JobExecutionIT extends CommonAPILocalIT {

    private static final String THROWS_EXCEPTION_JOB = "ThrowsExceptionJob";

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();

        createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        setSessionInfo(getSession());
    }

    @After
    public void after() throws Exception {
        VariableStorage.clearAll();
        deleteUser(USERNAME);
        logoutOnTenant();
        cleanSession();
    }

    @Test
    public void getFailedJobs_should_return_zero_when_there_are_no_failed_jobs() {
        final List<FailedJob> failedJobs = getProcessAPI().getFailedJobs(0, 100);
        assertThat(failedJobs).hasSize(0);
    }

    @Test
    public void retryAJob_should_execute_again_a_failed_job_and_clean_related_job_logs_and_jobDescriptor_if_not_recurrent()
            throws Exception {
        //given
        getCommandAPI().register("except", "Throws Exception when scheduling a job", AddJobCommand.class.getName());
        final Map<String, Serializable> parameters = new HashMap<>();
        try {
            getCommandAPI().execute("except", parameters);
            final FailedJob failedJob = waitForFailedJob();
            assertThat(failedJob.getJobName()).isEqualTo(THROWS_EXCEPTION_JOB);
            assertThat(failedJob.getRetryNumber()).isEqualTo(0);
            assertThat(failedJob.getDescription()).isEqualTo("Throw an exception when 'throwException'=true");

            final List<SJobDescriptor> jobDescriptors = waitForJobDescriptorsToHaveSize(1);
            waitForJobLogsToHaveSize(jobDescriptors.get(0).getId(), 1);

            //when
            getProcessAPI().replayFailedJob(failedJob.getJobDescriptorId(),
                    singletonMap("throwException", Boolean.FALSE));

            //then
            assertJobWasExecutedWithSuccess();

            waitForJobDescriptorsToHaveSize(0);
            waitForJobLogsToHaveSize(jobDescriptors.get(0).getId(), 0);

            // clean up:
            deleteJobLogsAndDescriptors(failedJob.getJobDescriptorId());
        } finally {
            getCommandAPI().unregister("except");
        }
    }

    private void waitForJobLogsToHaveSize(final long jobDescriptorId, final int nbOfExpectedJobLogs) {
        final QueryOptions options = new QueryOptions(0, 1, null,
                singletonList(new FilterOption(SJobLog.class, "jobDescriptorId", jobDescriptorId)), null);

        await().until(() -> {
            setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();
            final JobService jobService = tenantAccessor.getJobService();
            return transactionService.executeInTransaction(() -> jobService.searchJobLogs(options));
        }, hasSize(nbOfExpectedJobLogs));
    }

    private List<SJobDescriptor> waitForJobDescriptorsToHaveSize(final int nbOfExpectedJobDescriptors) {
        final List<FilterOption> filters = singletonList(
                new FilterOption(SJobDescriptor.class, "jobClassName", ThrowsExceptionJob.class.getName()));
        final QueryOptions queryOptions = new QueryOptions(0, 1, null, filters, null);

        return await().until(() -> {
            setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();
            final JobService jobService = tenantAccessor.getJobService();
            return transactionService.executeInTransaction(() -> jobService.searchJobDescriptors(queryOptions));
        }, hasSize(nbOfExpectedJobDescriptors));
    }

    @Test
    public void retryAJob_should_update_job_log_when_execution_fails_again() throws Exception {
        //given
        getCommandAPI().register("except", "Throws Exception when scheduling a job", AddJobCommand.class.getName());
        final Map<String, Serializable> parameters = new HashMap<>();
        try {
            getCommandAPI().execute("except", parameters);
            FailedJob failedJob = await().until(() -> getProcessAPI().getFailedJobs(0, 100), hasSize(1)).get(0);

            //when
            getProcessAPI().replayFailedJob(failedJob.getJobDescriptorId(), emptyMap());

            //then
            failedJob = await().until(() -> getProcessAPI().getFailedJobs(0, 100), hasSize(1)).get(0);
            assertThat(failedJob.getJobName()).isEqualTo(THROWS_EXCEPTION_JOB);
            assertThat(failedJob.getDescription()).isEqualTo("Throw an exception when 'throwException'=true");
            assertThat(failedJob.getLastMessage()).contains(
                    "org.bonitasoft.engine.scheduler.exception.SJobExecutionException: This job throws an arbitrary exception");
            assertThat(failedJob.getNumberOfFailures()).isEqualTo(1);

            deleteJobLogsAndDescriptors(failedJob.getJobDescriptorId());
        } finally {
            getCommandAPI().unregister("except");
        }
    }

    private void deleteJobLogsAndDescriptors(final long jobDescriptorId) throws Exception {
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final UserTransactionService transactionService = serviceAccessor.getUserTransactionService();
        final JobService jobService = serviceAccessor.getJobService();
        transactionService.executeInTransaction((Callable) () -> {
            jobService.deleteJobLogs(jobDescriptorId);
            jobService.deleteJobDescriptor(jobDescriptorId);
            return null;
        });
    }

    private FailedJob waitForFailedJob() throws Exception {
        new WaitUntil(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT) {

            @Override
            protected boolean check() {
                return getFailedJob() != null;
            }
        }.waitUntil();
        final FailedJob failedJob = getFailingJob();
        assertThat(failedJob).isNotNull();
        assertThat(failedJob.getRetryNumber()).isEqualTo(0);
        return failedJob;
    }

    private void assertJobWasExecutedWithSuccess() throws Exception {
        new WaitUntil(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT) {

            @Override
            protected boolean check() {
                final FailedJob failedJob = getFailingJob();
                return failedJob == null;
            }
        }.waitUntil();
        final FailedJob failedJob = getFailingJob();
        assertThat(failedJob).isNull();
    }

    private FailedJob getFailedJob() {
        final FailedJob failedJob = getFailingJob();
        if (failedJob != null && failedJob.getRetryNumber() == 0) {
            return failedJob;
        }
        return null;
    }

    private FailedJob getFailingJob() {
        final List<FailedJob> failedJobs = getProcessAPI().getFailedJobs(0, 100);
        FailedJob searchJob = null;
        for (final FailedJob failedJob : failedJobs) {
            if (failedJob.getJobName().equals(JobExecutionIT.THROWS_EXCEPTION_JOB)) {
                searchJob = failedJob;
            }
        }
        return searchJob;
    }

    @Override
    protected TenantServiceAccessor getTenantAccessor() {
        try {
            return TenantServiceSingleton.getInstance();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

}
