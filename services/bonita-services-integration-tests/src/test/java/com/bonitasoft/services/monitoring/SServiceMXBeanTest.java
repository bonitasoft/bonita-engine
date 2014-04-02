/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.SServiceMXBean;
import com.bonitasoft.engine.monitoring.mbean.impl.SServiceMXBeanImpl;
import com.bonitasoft.services.CommonServiceSPTest;

public class SServiceMXBeanTest extends CommonServiceSPTest {

    private static SchedulerService schedulerService;

    private static TenantMonitoringService monitoringService;

    private static long sessionId;

    private static long tenantId;

    private MBeanServer mbserver = null;

    private ObjectName entityMB;

    private ObjectName serviceMB;

    static {
        schedulerService = getServicesBuilder().buildSchedulerService();
        monitoringService = getServicesBuilder().buildTenantMonitoringService();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        sessionId = getSessionAccessor().getSessionId();
        tenantId = getSessionAccessor().getTenantId();
    }

    @Before
    public void disableMBeans() throws Exception {
        TestUtil.startScheduler(schedulerService);
        getSessionAccessor().setSessionInfo(sessionId, tenantId);
        final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
        if (mbservers.size() > 0) {
            mbserver = mbservers.get(0);
        }
        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }
        // Constructs the mbean names
        entityMB = new ObjectName(TenantMonitoringService.ENTITY_MBEAN_PREFIX + tenantId);
        serviceMB = new ObjectName(TenantMonitoringService.SERVICE_MBEAN_PREFIX + tenantId);

        unregisterMBeans();

    }

    @Override
    @After
    public void tearDown() throws Exception {
        getSessionAccessor().setSessionInfo(sessionId, tenantId);
        TestUtil.closeTransactionIfOpen(getTransactionService());
        TestUtil.stopScheduler(schedulerService, getTransactionService());
    }

    public SServiceMXBean getServiceMXBean() {
        return new SServiceMXBeanImpl(getTransactionService(), monitoringService, getSessionAccessor(), getSessionService());
    }

    /**
     * Assure that no Bonitasoft MBeans are registered in the MBServer before
     * each test.
     * 
     * @throws MBeanRegistrationException
     * @throws InstanceNotFoundException
     */
    public void unregisterMBeans() throws MBeanRegistrationException, InstanceNotFoundException {
        if (mbserver.isRegistered(entityMB)) {
            mbserver.unregisterMBean(entityMB);
        }
        if (mbserver.isRegistered(serviceMB)) {
            mbserver.unregisterMBean(serviceMB);
        }
    }

    @Test
    public void getActiveTransactionTest() throws Exception {

        // start the ServiceMXBean
        final SServiceMXBean svcMB = getServiceMXBean();
        svcMB.start();

        final String numberOfActiveTransactions = "NumberOfActiveTransactions";
        assertEquals(0L, mbserver.getAttribute(serviceMB, numberOfActiveTransactions));

        // create a transaction in new thread to avoid an exception
        final Thread createTransactionThread = new Thread(new CreateTransactionThread(getTransactionService()));
        createTransactionThread.start();
        Thread.sleep(500);// wait thread start transaction

        // check the transaction has been successfully counted
        assertEquals(1L, mbserver.getAttribute(serviceMB, numberOfActiveTransactions));

        final WaitFor waitForNoActiveTransactions = new WaitFor(50, 10000) {

            @Override
            boolean check() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
                return (Long) mbserver.getAttribute(serviceMB, numberOfActiveTransactions) == 0l;
            }
        };
        // check the number of executing job has incremented
        assertTrue(waitForNoActiveTransactions.waitFor());
        svcMB.stop();
    }

    @Test
    public void getExecutingJobsTest() throws Exception {
        final SServiceMXBean svcMB = getServiceMXBean();
        // stop and start the Scheduler service to clean the previous job list

        svcMB.start();
        // save the current number of executing jobs
        // final int startNbOfExecutingJobs = svcMB.getExecutingJobsNb();
        final String numberOfExecutingJobs = "NumberOfExecutingJobs";
        final long startNbOfExecutingJobs = (Long) mbserver.getAttribute(serviceMB, numberOfExecutingJobs);

        getTransactionService().begin();
        getSessionAccessor().setSessionInfo(sessionId, tenantId);
        // create an action that will schedule a job
        final VariableStorageForMonitoring storage = VariableStorageForMonitoring.getInstance();

        final String theResponse = "theUltimateQuestionOfLifeTheUniverseAndEverything";
        storage.setVariable(theResponse, 42);

        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("com.bonitasoft.services.monitoring.IncrementAVariable", "IncrementAVariable").setDescription("increment a variable").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", theResponse).done());
        final Trigger trigger = new OneShotTrigger("events", now, 10);
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        final WaitFor waitForJobExecuting = new WaitFor(50, 6000) {

            @Override
            boolean check() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
                // return svcMB.getExecutingJobsNb() == (startNbOfExecutingJobs
                // + 1);
                return (Long) mbserver.getAttribute(serviceMB, numberOfExecutingJobs) == startNbOfExecutingJobs + 1;
            }
        };

        // check the number of executing job has incremented
        assertTrue(waitForJobExecuting.waitFor());

        // set the storage variable to 1 to finish the Job execution
        storage.setVariable(theResponse, 1);
        // wait while the job finish its execution
        final WaitFor waitforJobCompleted = new WaitFor(50, 5000) {

            @Override
            boolean check() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
                // return (svcMB.getExecutingJobsNb() ==
                // startNbOfExecutingJobs);
                return (Long) mbserver.getAttribute(serviceMB, "NumberOfExecutingJobs") == startNbOfExecutingJobs;
            }

        };

        // check the number of executing jobs is 0
        assertTrue(waitforJobCompleted.waitFor());
        svcMB.stop();
    }

    private class CreateTransactionThread implements Runnable {

        private final TransactionService txService;

        public CreateTransactionThread(final TransactionService txService) {
            this.txService = txService;
        }

        @Override
        public void run() {
            try {
                txService.begin();
                Thread.sleep(4000);
                txService.complete();
            } catch (final Exception e) {
                e.printStackTrace();
            }

        }

    }

}
