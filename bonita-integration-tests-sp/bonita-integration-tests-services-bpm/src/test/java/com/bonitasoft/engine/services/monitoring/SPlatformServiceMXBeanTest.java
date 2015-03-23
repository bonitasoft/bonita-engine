/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.services.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.bonitasoft.engine.CommonBPMServicesSPTest;
import com.bonitasoft.engine.monitoring.mbean.SServiceMXBean;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.Test;

import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;
import com.bonitasoft.engine.monitoring.mbean.impl.SPlatformServiceMXBeanImpl;

public class SPlatformServiceMXBeanTest extends CommonBPMServicesSPTest {

    protected static MBeanServer mbserver = null;

    private final ObjectName serviceMB;

    private SchedulerService schedulerService;

    private PlatformMonitoringService monitoringService;

    public void startScheduler() throws Exception {
        TestUtil.startScheduler(schedulerService);
    }

    public void stopScheduler() throws Exception {
        TestUtil.stopScheduler(schedulerService, getTransactionService());
    }

    private SPlatformServiceMXBean getPlatformServiceMXBean() throws Exception {
        return new SPlatformServiceMXBeanImpl(monitoringService);
    }

    public SPlatformServiceMXBeanTest() throws Exception {
        schedulerService = getTenantAccessor().getSchedulerService();
        monitoringService = getPlatformAccessor().getPlatformMonitoringService();
        final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
        if (mbservers.size() > 0) {
            mbserver = mbservers.get(0);
        }
        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }

        serviceMB = new ObjectName(PlatformMonitoringService.SERVICE_MBEAN_NAME);
    }

    @Test
    public void getExecutingJobsTest() throws Exception {
        final SPlatformServiceMXBean svcMB = getPlatformServiceMXBean();
        // stop and start the Scheduler service to clean the previous job list

        svcMB.start();
        // save the current number of executing jobs
        // final int startNbOfExecutingJobs = svcMB.getExecutingJobsNb();
        final String numberOfExecutingJobs = "NumberOfExecutingJobs";
        final long startNbOfExecutingJobs = (Long) mbserver.getAttribute(serviceMB, numberOfExecutingJobs);

        getTransactionService().begin();
        // create an action that will schedule a job
        final VariableStorageForMonitoring storage = VariableStorageForMonitoring.getInstance();

        final String theResponse = "theUltimateQuestionOfLifeTheUniverseAndEverything";
        storage.setVariable(theResponse, 42);

        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(IncrementAVariable.class.getName(), "IncrementAVariable").setDescription("increment a variable").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", theResponse).done());
        final Trigger trigger = new OneShotTrigger("events", now, 10);
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();

        // check the number of executing job has incremented
        waitForJobExecuting(numberOfExecutingJobs, startNbOfExecutingJobs + 1);

        // set the storage variable to 1 to finish the Job execution
        storage.setVariable(theResponse, 1);

        // wait while the job finish its execution
        // check the number of executing jobs is 0
        waitForJobExecuting(numberOfExecutingJobs, startNbOfExecutingJobs);

        svcMB.stop();
    }

    private void waitForJobExecuting(final String numberOfExecutingJobs, final long startNbOfExecutingJobs) throws InterruptedException,
            AttributeNotFoundException,
            InstanceNotFoundException, MBeanException, ReflectionException {
        final WaitFor waitForJobExecuting = new WaitFor(50, 10000) {

            @Override
            boolean check() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
                System.err.println("mbserver.getAttribute(serviceMB, numberOfExecutingJobs)=" + mbserver.getAttribute(serviceMB, numberOfExecutingJobs));
                System.err.println("startNbOfExecutingJobs=" + startNbOfExecutingJobs);
                return (Long) mbserver.getAttribute(serviceMB, numberOfExecutingJobs) == startNbOfExecutingJobs;
            }
        };
        assertTrue(waitForJobExecuting.waitFor());
    }

    @Test
    public void isSchedulerStartedTest() throws Exception {

        final SPlatformServiceMXBean svcMB = getPlatformServiceMXBean();
        svcMB.start();
        startScheduler();

        final WaitFor waitForSchedulerStart = new WaitFor(50, 5000) {

            @Override
            boolean check() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
                return (Boolean) mbserver.getAttribute(serviceMB, "SchedulerStarted");
            }
        };

        assertTrue(waitForSchedulerStart.check());

        stopScheduler();

        final WaitFor waitForSchedulerStop = new WaitFor(50, 5000) {

            @Override
            boolean check() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
                return (Boolean) mbserver.getAttribute(serviceMB, "SchedulerStarted");
            }
        };

        assertFalse(waitForSchedulerStop.check());

        svcMB.stop();
    }

    @Test
    public void getActiveTransactionTest() throws Exception {

        // start the ServiceMXBean
        final SPlatformServiceMXBean svcMB = getPlatformServiceMXBean();

        svcMB.start();

        final String numberOfActiveTransactions = "NumberOfActiveTransactions";
        assertEquals(0L, mbserver.getAttribute(serviceMB, numberOfActiveTransactions));

        // create a transaction
        getTransactionService().begin();

        // check the transaction has been successfully counted
        assertEquals(1L, mbserver.getAttribute(serviceMB, numberOfActiveTransactions));
        // close the transaction
        getTransactionService().complete();

        assertEquals(0L, mbserver.getAttribute(serviceMB, numberOfActiveTransactions));
        svcMB.stop();
    }

}
