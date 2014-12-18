package org.bonitasoft.engine.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.test.WaitUntil;
import org.junit.Test;

// because of waituntil but its the only class where we use failed jobs... so i don't want to add a handler and so on only for jobs
@SuppressWarnings("deprecation")
public class JobExecutionIT extends TestWithUser {

    @Test
    public void getFailedJobs_should_return_zero_when_there_are_no_failed_jobs() {
        final List<FailedJob> failedJobs = getProcessAPI().getFailedJobs(0, 100);
        assertThat(failedJobs).hasSize(0);
    }

    @Test
    public void retryAJob_should_execute_again_a_failed_job_and_clean_related_job_logs() throws Exception {
        //given
        getCommandAPI().register("except", "Throws Exception when scheduling a job", AddJobCommand.class.getName());
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        try {
            getCommandAPI().execute("except", parameters);
            final FailedJob failedJob = waitForFailedJob("ThrowsExceptionJob", 0);
            assertThat(failedJob.getJobName()).isEqualTo("ThrowsExceptionJob");
            assertThat(failedJob.getRetryNumber()).isEqualTo(0);
            assertThat(failedJob.getDescription()).isEqualTo("Throw an exception when 'throwException'=true");

            //when
            getProcessAPI().replayFailedJob(failedJob.getJobDescriptorId(), Collections.singletonMap("throwException", (Serializable) Boolean.FALSE));

            //then
            assertJobWasExecutedWithSucess("ThrowsExceptionJob");
        } finally {
            getCommandAPI().unregister("except");
        }
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

}
