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

import com.bonitasoft.engine.CommonBPMServicesSPTest;
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

public class SServiceMXBeanTest extends CommonBPMServicesSPTest {

    private SchedulerService schedulerService;

    private MBeanServer mbserver = null;

    private ObjectName entityMB;

    private ObjectName serviceMB;

    public SServiceMXBeanTest() {
        schedulerService = getTenantAccessor().getSchedulerService();
    }

    @Before
    public void disableMBeans() throws Exception {
        TestUtil.startScheduler(schedulerService);
        final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
        if (mbservers.size() > 0) {
            mbserver = mbservers.get(0);
        }
        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }
        // Constructs the mbean names
        entityMB = new ObjectName(TenantMonitoringService.ENTITY_MBEAN_PREFIX + getDefaultTenantId());
        serviceMB = new ObjectName(TenantMonitoringService.SERVICE_MBEAN_PREFIX + getDefaultTenantId());

        unregisterMBeans();

    }

    public SServiceMXBean getServiceMXBean() {
        return getTenantAccessor().getTenantMonitoringService().getServiceBean();
    }

    /**
     * Assure that no Bonitasoft MBeans are registered in the MBServer before
     * each test.
     *
     * @throws javax.management.MBeanRegistrationException
     * @throws javax.management.InstanceNotFoundException
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
