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
package org.bonitasoft.engine.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.CommonAPILocalIT;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// because of waituntil but its the only class where we use failed jobs... so i don't want to add a handler and so on only for jobs
@SuppressWarnings("deprecation")
@RunWith(BonitaTestRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class JobExecutionIT extends CommonAPILocalIT {

    private User john;

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();

        john = createUser(USERNAME, PASSWORD);
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
    public void retryAJob_should_execute_again_a_failed_job_and_clean_related_job_logs_and_jobDescriptor_if_not_recurrent() throws Exception {
        //given
        getCommandAPI().register("except", "Throws Exception when scheduling a job", AddJobCommand.class.getName());
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        try {
            getCommandAPI().execute("except", parameters);
            final FailedJob failedJob = waitForFailedJob("ThrowsExceptionJob", 0);
            assertThat(failedJob.getJobName()).isEqualTo("ThrowsExceptionJob");
            assertThat(failedJob.getRetryNumber()).isEqualTo(0);
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
            getProcessAPI().replayFailedJob(failedJob.getJobDescriptorId(), Collections.singletonMap("throwException", (Serializable) Boolean.FALSE));

            //then
            assertJobWasExecutedWithSucess("ThrowsExceptionJob");

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
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();
        final JobService jobService = tenantAccessor.getJobService();

        final QueryOptions options = new QueryOptions(0, 1, null, Arrays.asList(new FilterOption(SJobLog.class, "jobDescriptorId", jobDescriptorId)), null);

        final Callable<List<SJobLog>> searchJobLogs = new Callable<List<SJobLog>>() {

            @Override
            public List<SJobLog> call() throws Exception {
                return jobService.searchJobLogs(options);
            }
        };
        final List<SJobLog> jobLogs = transactionService.executeInTransaction(searchJobLogs);
        assertEquals(nbOfExpectedJobLogs, jobLogs.size());
    }

    private List<SJobDescriptor> searchJobDescriptors(final int nbOfExpectedJobDescriptors) throws Exception {
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final JobService jobService = tenantAccessor.getJobService();
        final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();

        final List<FilterOption> filters = Arrays.asList(new FilterOption(SJobDescriptor.class, "jobClassName", ThrowsExceptionJob.class.getName()));
        final QueryOptions queryOptions = new QueryOptions(0, 1, null, filters, null);

        final Callable<List<SJobDescriptor>> searchJobLogs = new Callable<List<SJobDescriptor>>() {

            @Override
            public List<SJobDescriptor> call() throws Exception {
                return jobService.searchJobDescriptors(queryOptions);
            }
        };
        final List<SJobDescriptor> jobDescriptors = transactionService.executeInTransaction(searchJobLogs);

        assertEquals(nbOfExpectedJobDescriptors, jobDescriptors.size());
        return jobDescriptors;
    }

    @Test
    public void retryAJob_should_update_job_log_when_execution_fails_again() throws Exception {
        //given
        getCommandAPI().register("except", "Throws Exception when scheduling a job", AddJobCommand.class.getName());
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        try {
            getCommandAPI().execute("except", parameters);
            FailedJob failedJob = waitForFailedJob("ThrowsExceptionJob", 0);

            //when
            getProcessAPI().replayFailedJob(failedJob.getJobDescriptorId(), Collections.<String, Serializable> emptyMap());

            //then
            failedJob = waitForFailedJob("ThrowsExceptionJob", 1);
            assertThat(failedJob.getJobName()).isEqualTo("ThrowsExceptionJob");
            assertThat(failedJob.getRetryNumber()).isEqualTo(1);
            assertThat(failedJob.getDescription()).isEqualTo("Throw an exception when 'throwException'=true");
        } finally {
            getCommandAPI().unregister("except");
        }
    }

    private FailedJob waitForFailedJob(final String name, final int numberOfRetries) throws Exception {
        new WaitUntil(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT) {

            @Override
            protected boolean check() {
                final FailedJob failedJob = getFailedJob(name, numberOfRetries);
                return failedJob != null;
            }
        }.waitUntil();
        final FailedJob failedJob = getFailedJob(name);
        assertThat(failedJob).isNotNull();
        if (numberOfRetries >= 0) {
            assertThat(failedJob.getRetryNumber()).isEqualTo(numberOfRetries);
        }
        return failedJob;
    }

    private void assertJobWasExecutedWithSucess(final String jobName) throws Exception {
        new WaitUntil(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT) {

            @Override
            protected boolean check() {
                final FailedJob failedJob = getFailedJob(jobName);
                return failedJob == null;
            }
        }.waitUntil();
        final FailedJob failedJob = getFailedJob(jobName);
        assertThat(failedJob).isNull();
    }

    private FailedJob getFailedJob(final String name, final int numberOfRetries) {
        final FailedJob failedJob = getFailedJob(name);
        if (failedJob != null && failedJob.getRetryNumber() == numberOfRetries || numberOfRetries < 0) {
            return failedJob;
        }
        return null;
    }

    private FailedJob getFailedJob(final String name) {
        final List<FailedJob> failedJobs = getProcessAPI().getFailedJobs(0, 100);
        FailedJob searchJob = null;
        for (final FailedJob failedJob : failedJobs) {
            if (failedJob.getJobName().equals(name)) {
                searchJob = failedJob;
            }
        }
        return searchJob;
    }

    @Override
    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

}
