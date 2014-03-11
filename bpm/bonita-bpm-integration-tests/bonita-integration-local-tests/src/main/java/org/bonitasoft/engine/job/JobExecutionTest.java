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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobExecutionTest extends CommonAPITest {

    protected User matti;

    @Before
    public void before() throws Exception {
        login();
        matti = createUser("matti", "keltainen");
    }

    @After
    public void after() throws Exception {
        deleteUser(matti);
        logout();
    }

    @Test
    public void getFailedJobs() {
        final List<FailedJob> failedJobs = getProcessAPI().getFailedJobs(0, 100);
        assertEquals(0, failedJobs.size());
    }

    @Test
    public void retrySeveralTimesAJob() throws Exception {
        getCommandAPI().register("except", "Throws Exception when scheduling a job", AddJobCommand.class.getName());
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        try {
            getCommandAPI().execute("except", parameters);
            Thread.sleep(1000);
            List<FailedJob> failedJobs = getProcessAPI().getFailedJobs(0, 100);
            assertEquals(1, failedJobs.size());
            final FailedJob failedJob = failedJobs.get(0);
            getProcessAPI().replayFailedJob(failedJob.getJobDescriptorId(), Collections.<String, Serializable> emptyMap());
            Thread.sleep(1000);
            failedJobs = getProcessAPI().getFailedJobs(0, 100);
            assertEquals(1, failedJobs.size());
            final FailedJob failedJob2 = failedJobs.get(0);
            assertNotEquals(failedJob, failedJob2);
            assertEquals(failedJob.getJobDescriptorId(), failedJob2.getJobDescriptorId());
            assertEquals(failedJob.getJobName(), failedJob2.getJobName());
            assertNotEquals(failedJob.getLastUpdateDate(), failedJob2.getLastUpdateDate());
            assertEquals(0, failedJob.getRetryNumber());
            assertEquals(1, failedJob2.getRetryNumber());
            assertEquals("Throw an exception when 'throwException'=true", failedJob.getDescription());
            getProcessAPI().replayFailedJob(failedJobs.get(0).getJobDescriptorId(), Collections.singletonMap("throwException", (Serializable) Boolean.FALSE));
            Thread.sleep(1000);
            failedJobs = getProcessAPI().getFailedJobs(0, 100);
            assertEquals(0, failedJobs.size());
        } finally {
            getCommandAPI().unregister("except");
        }
    }

}
