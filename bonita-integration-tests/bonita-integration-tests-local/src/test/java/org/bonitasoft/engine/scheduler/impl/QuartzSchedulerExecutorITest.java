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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.job.ReleaseWaitersJob;
import org.bonitasoft.engine.scheduler.job.VariableStorage;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneExecutionTrigger;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTriggerForTest;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QuartzSchedulerExecutorITest extends CommonBPMServicesTest {

    private final SchedulerService schedulerService;

    private final VariableStorage storage = VariableStorage.getInstance();

    public QuartzSchedulerExecutorITest() {
        schedulerService = getTenantAccessor().getSchedulerService();
    }

    @Before
    public void before() throws Exception {
        TestUtil.stopScheduler(schedulerService, getTransactionService());
        if (!schedulerService.isStarted()) {
            schedulerService.initializeScheduler();
            schedulerService.start();
        }
    }

    @After
    public void after() throws Exception {
        storage.clear();
    }

    @Test
    public void canRestartTheSchedulerAfterShutdown() throws Exception {
        schedulerService.stop();
        assertTrue(schedulerService.isStopped());
        schedulerService.initializeScheduler();
        schedulerService.start();
        assertTrue(schedulerService.isStarted());
    }

    @Test
    public void doNotExecuteAFutureJob() throws Exception {
        final Date future = new Date(System.currentTimeMillis() + 10000000);
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "testDoNotExecuteAFutureJob").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", variableName).done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger("events", future, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        Thread.sleep(200);
        assertNull(storage.getVariableValue(variableName));
    }

    @Test
    public void doNotThrowAnExceptionWhenDeletingAnUnknownJob() throws Exception {
        getTransactionService().begin();
        final boolean deleted = schedulerService.delete("MyJob");
        getTransactionService().complete();
        assertFalse(deleted);
    }

    /*
     * We must ensure that:
     * * pause only jobs of the current tenant
     * * trigger new job are not executed
     * * resume the jobs resume it really
     * *
     */
    @Test
    public void pause_and_resume_jobs_of_a_tenant() throws Exception {
        long tenantForJobTest1 = createTenant("tenantForJobTest1");
        long tenantForJobTest2 = createTenant("tenantForJobTest2");

        changeTenant(tenantForJobTest1);
        final String jobName = "ReleaseWaitersJob";
        Date now = new Date();
        SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(ReleaseWaitersJob.class.getName(), jobName + "1").done();
        List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", jobName).done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobKey", "1").done());
        Trigger trigger = new UnixCronTriggerForTest("events", now, 10, "0/1 * * * * ?");

        // trigger it
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        ReleaseWaitersJob.waitForJobToExecuteOnce();

        // pause
        getTransactionService().begin();
        schedulerService.pauseJobs(sessionAccessor.getTenantId());
        getTransactionService().complete();
        Thread.sleep(100);
        ReleaseWaitersJob.checkNotExecutedDuring(1500);

        // trigger the job in an other tenant
        changeTenant(tenantForJobTest2);
        now = new Date(System.currentTimeMillis() + 100);
        jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class).createNewInstance(ReleaseWaitersJob.class.getName(), jobName + "2").done();
        parameters = new ArrayList<>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName3", jobName).done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobKey", "3").done());
        trigger = new OneShotTrigger("events3", now, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        ReleaseWaitersJob.waitForJobToExecuteOnce();
    }

}
