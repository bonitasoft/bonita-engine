package org.bonitasoft.engine.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.WaitUntil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// because of waituntil but its the only classe where we use failed jobs... so i don't want to add a handler and so on only for jobs
@SuppressWarnings("deprecation")
public class JobExecutionTest extends CommonAPITest {

    private static final int ENOUTH_TIME_TO_GET_THE_JOB_DONE = 1000;

    protected User matti;

    @After
    public void after() throws Exception {
        deleteUser(matti);
        logoutOnTenant();
    }

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        matti = createUser("matti", "keltainen");
    }

    @Test
    public void getFailedJobs() {
        final List<FailedJob> failedJobs = getProcessAPI().getFailedJobs(0, 100);
        assertEquals(0, failedJobs.size());
    }

    // @Test: ignored before we find why it fails.? see https://bonitasoft.atlassian.net/browse/BS-9402 for the stack trace to anaylyse.
    public void retryAJob() throws Exception {
        getCommandAPI().register("except", "Throws Exception when scheduling a job", AddJobCommand.class.getName());
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        try {
            getCommandAPI().execute("except", parameters);
            List<FailedJob> failedJobs = waitForFailedJobs(1);
            final FailedJob failedJob = failedJobs.get(0);
            Thread.sleep(10);
            getProcessAPI().replayFailedJob(failedJob.getJobDescriptorId(), Collections.<String, Serializable> emptyMap());
            failedJobs = waitForFailedJobs(1);
            assertEquals(1, failedJobs.size());
            final FailedJob failedJob2 = failedJobs.get(0);
            assertNotEquals(failedJob, failedJob2);
            assertEquals(failedJob.getJobDescriptorId(), failedJob2.getJobDescriptorId());
            assertEquals(failedJob.getJobName(), failedJob2.getJobName());
            assertEquals(0, failedJob.getRetryNumber());
            assertEquals(1, failedJob2.getRetryNumber());
            assertNotEquals(failedJob.getLastUpdateDate(), failedJob2.getLastUpdateDate());
            assertEquals("Throw an exception when 'throwException'=true", failedJob.getDescription());
            getProcessAPI().replayFailedJob(failedJobs.get(0).getJobDescriptorId(), Collections.singletonMap("throwException", (Serializable) Boolean.FALSE));
            Thread.sleep(ENOUTH_TIME_TO_GET_THE_JOB_DONE);
            failedJobs = getProcessAPI().getFailedJobs(0, 100);
            assertEquals(0, failedJobs.size());
        } finally {
            getCommandAPI().unregister("except");
        }
    }

    private List<FailedJob> waitForFailedJobs(final int numberOfFailedJobs) throws Exception {
        new WaitUntil(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT) {

            @Override
            protected boolean check() {
                return getProcessAPI().getFailedJobs(0, 100).size() == numberOfFailedJobs;
            }
        }.waitUntil();
        final List<FailedJob> failedJobs = getProcessAPI().getFailedJobs(0, 100);
        assertEquals(numberOfFailedJobs, failedJobs.size());
        return failedJobs;
    }

}
