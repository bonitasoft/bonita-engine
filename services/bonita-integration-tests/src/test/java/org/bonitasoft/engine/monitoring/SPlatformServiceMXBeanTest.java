package org.bonitasoft.engine.monitoring;

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

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;
import org.bonitasoft.engine.monitoring.mbean.impl.SPlatformServiceMXBeanImpl;
import org.bonitasoft.engine.scheduler.SSchedulerException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.Test;

public class SPlatformServiceMXBeanTest extends CommonServiceTest {

    protected static MBeanServer mbserver = null;

    private final ObjectName serviceMB;

    private static SchedulerService schedulerService;

    private static PlatformMonitoringService monitoringService;

    static {
        schedulerService = getServicesBuilder().buildSchedulerService();
        monitoringService = getServicesBuilder().buildPlatformMonitoringService();
    }

    public void startScheduler() throws SSchedulerException, FireEventException {
        TestUtil.startScheduler(schedulerService);
    }

    public void stopScheduler() throws SSchedulerException, FireEventException {
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
