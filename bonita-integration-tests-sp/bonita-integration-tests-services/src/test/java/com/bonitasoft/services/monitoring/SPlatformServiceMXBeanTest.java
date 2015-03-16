/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.Test;

import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;
import com.bonitasoft.engine.monitoring.mbean.impl.SPlatformServiceMXBeanImpl;
import com.bonitasoft.services.CommonServiceSPTest;

public class SPlatformServiceMXBeanTest extends CommonServiceSPTest {

    protected static MBeanServer mbserver = null;

    private final ObjectName serviceMB;

    private static SchedulerService schedulerService;

    private static PlatformMonitoringService monitoringService;

    static {
        schedulerService = getServicesBuilder().buildSchedulerService();
        monitoringService = getServicesBuilder().buildPlatformMonitoringService();
    }

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
    public void isSchedulerStartedTest() throws Exception {

        final SPlatformServiceMXBean svcMB = getPlatformServiceMXBean();
        svcMB.start();

        assertFalse((Boolean) mbserver.getAttribute(serviceMB, "SchedulerStarted"));

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
