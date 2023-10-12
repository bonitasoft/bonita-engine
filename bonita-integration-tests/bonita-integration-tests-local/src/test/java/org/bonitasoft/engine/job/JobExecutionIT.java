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

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.test.CommonAPILocalIT;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// because of waituntil but its the only class where we use failed jobs... so i don't want to add a handler and so on
// only for jobs
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
        assertThat(failedJobs).isEmpty();
    }

    @Test
    public void retryAJob_should_execute_again_a_failed_job_and_clean_related_job_logs_and_jobDescriptor_if_not_recurrent()
            throws Exception {
        //given
        getCommandAPI().register("except", "Throws Exception when scheduling a job", AddJobCommand.class.getName());
        try {
            getCommandAPI().execute("except", emptyMap());
            final FailedJob failedJob = waitForFailedJob();
            assertThat(failedJob.getJobName()).isEqualTo(THROWS_EXCEPTION_JOB);
            assertThat(failedJob.getNumberOfFailures()).isEqualTo(1);
            assertThat(failedJob.getDescription()).isEqualTo("Throw an exception when 'throwException'=true");

            final List<SJobDescriptor> jobDescriptors = searchJobDescriptors(1);
            final long jobDescriptorId = jobDescriptors.get(0).getId();
            try {
                searchJobLogs(jobDescriptorId, 1);
            } catch (final AssertionError e) {
                Thread.sleep(800);
                searchJobLogs(jobDescriptorId, 1);
            }

            //when
            getProcessAPI().replayFailedJob(failedJob.getJobDescriptorId(),
                    Collections.singletonMap("throwException", Boolean.FALSE));

            //then
            assertJobWasExecutedWithSuccess();

            try {
                searchJobDescriptors(0);
            } catch (final AssertionError e) {
                Thread.sleep(800);
                searchJobDescriptors(0);
            }
            searchJobLogs(jobDescriptorId, 0);

        } finally {
            getCommandAPI().unregister("except");
        }
    }

    private void searchJobLogs(final long jobDescriptorId, final int nbOfExpectedJobLogs) throws Exception {
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final UserTransactionService transactionService = serviceAccessor.getUserTransactionService();
        final JobService jobService = serviceAccessor.getJobService();

        final QueryOptions options = new QueryOptions(0, 1, null,
                Collections.singletonList(new FilterOption(SJobLog.class, "jobDescriptorId", jobDescriptorId)), null);

        final Callable<List<SJobLog>> searchJobLogs = () -> jobService.searchJobLogs(options);
        final List<SJobLog> jobLogs = transactionService.executeInTransaction(searchJobLogs);
        assertEquals(nbOfExpectedJobLogs, jobLogs.size());
    }

    private List<SJobDescriptor> searchJobDescriptors(final int nbOfExpectedJobDescriptors) throws Exception {
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final JobService jobService = serviceAccessor.getJobService();
        final UserTransactionService transactionService = serviceAccessor.getUserTransactionService();

        final List<FilterOption> filters = Collections
                .singletonList(
                        new FilterOption(SJobDescriptor.class, "jobClassName", ThrowsExceptionJob.class.getName()));
        final QueryOptions queryOptions = new QueryOptions(0, 1, null, filters, null);

        final Callable<List<SJobDescriptor>> searchJobLogs = () -> jobService.searchJobDescriptors(queryOptions);
        final List<SJobDescriptor> jobDescriptors = transactionService.executeInTransaction(searchJobLogs);

        assertEquals(nbOfExpectedJobDescriptors, jobDescriptors.size());
        return jobDescriptors;
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
        } finally {
            getCommandAPI().unregister("except");
        }
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
        assertThat(failedJob.getNumberOfFailures()).isPositive();
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
        if (failedJob != null && failedJob.getNumberOfFailures() > 0) {
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

}
