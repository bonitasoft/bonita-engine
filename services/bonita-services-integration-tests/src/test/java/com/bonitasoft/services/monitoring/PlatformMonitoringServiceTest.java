package com.bonitasoft.services.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.HandlerUnregistrationException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SSchedulerException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.transaction.SBadTransactionStateException;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import com.bonitasoft.engine.monitoring.mbean.MBeanStopException;
import com.bonitasoft.services.CommonServiceSPTest;

public class PlatformMonitoringServiceTest extends CommonServiceSPTest {

    private static PlatformMonitoringService monitoringService;

    private static TechnicalLoggerService technicalLoggerService;

    private static SchedulerService schedulerService;

    static {
        monitoringService = getServicesBuilder().buildPlatformMonitoringService();
        technicalLoggerService = getServicesBuilder().buildTechnicalLoggerService();
        schedulerService = getServicesBuilder().buildSchedulerService();
    }

    protected static MBeanServer mbserver = null;

    protected static ObjectName serviceMB;

    protected static ObjectName jvmMB;

    @Before
    public void setup() throws Exception {

        final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
        if (mbservers.size() > 0) {
            mbserver = mbservers.get(0);
        }
        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }

        // Constructs the mbean names
        serviceMB = new ObjectName(PlatformMonitoringService.SERVICE_MBEAN_NAME);
        jvmMB = new ObjectName(PlatformMonitoringService.JVM_MBEAN_NAME);

        unregisterMBeans();
    }

    /**
     * Assure that no Bonitasoft MBeans are registered in the MBServer before each test.
     * 
     * @throws MBeanRegistrationException
     * @throws InstanceNotFoundException
     */

    public void unregisterMBeans() throws MBeanRegistrationException, InstanceNotFoundException {
        if (mbserver.isRegistered(serviceMB)) {
            mbserver.unregisterMBean(serviceMB);
        }
        if (mbserver.isRegistered(jvmMB)) {
            mbserver.unregisterMBean(jvmMB);
        }
    }

    @Test
    public void startMbeanAccessibility() throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanStartException {
        assertFalse(mbserver.isRegistered(serviceMB));
        assertFalse(mbserver.isRegistered(jvmMB));

        monitoringService.registerMBeans();

        assertTrue(mbserver.isRegistered(serviceMB));
        assertTrue(mbserver.isRegistered(jvmMB));
    }

    @Test
    public void stopMbeanAccessibility() throws MBeanStartException, MBeanStopException {

        monitoringService.registerMBeans();
        assertTrue(mbserver.isRegistered(serviceMB));
        assertTrue(mbserver.isRegistered(jvmMB));

        monitoringService.unregisterMbeans();
        assertFalse(mbserver.isRegistered(serviceMB));
        assertFalse(mbserver.isRegistered(jvmMB));
    }

    @Test
    public void testGetCurrentMemoryUsage() throws SBonitaException {
        assertTrue(monitoringService.getCurrentMemoryUsage() > 0);
    }

    @Test
    public void testGetMemoryUsagePercentage() throws SBonitaException {
        assertTrue(monitoringService.getMemoryUsagePercentage() >= 0);
        assertTrue(monitoringService.getMemoryUsagePercentage() <= 100);
    }

    @Test
    public void testGetSystemLoadAverage() throws SBonitaException {
        final double result = monitoringService.getSystemLoadAverage();

        if (result < 0) {
            technicalLoggerService.log(this.getClass(), TechnicalLogSeverity.WARNING, "the getSystemLoadAverage method is not available");
        }

        assertTrue(monitoringService.getSystemLoadAverage() != 0);
    }

    @Test
    public void testGetUpTime() throws SBonitaException {
        assertTrue(monitoringService.getUpTime() > 0);
    }

    @Test
    public void testGetStartTime() throws SBonitaException {
        assertTrue(monitoringService.getStartTime() <= System.currentTimeMillis());
    }

    @Test
    public void testGetTotalThreadsCpuTime() throws SBonitaException {
        assertTrue(monitoringService.getTotalThreadsCpuTime() > 0);
        final long result = monitoringService.getTotalThreadsCpuTime();

        if (result < 0) {
            technicalLoggerService.log(this.getClass(), TechnicalLogSeverity.WARNING, "the getTotalThreadsCpuTime method is not available");
        }
        assertTrue(monitoringService.getTotalThreadsCpuTime() != 0);
    }

    @Test
    public void testGetThreadCount() throws SBonitaException {
        assertTrue(monitoringService.getThreadCount() > 0);
    }

    @Test
    public void testGetAvailableProcessors() throws SBonitaException {
        assertTrue(monitoringService.getAvailableProcessors() > 0);
    }

    @Test
    public void testGetOSArch() throws SBonitaException {
        assertNotNull(monitoringService.getOSArch());
    }

    @Test
    public void testGetOSName() throws SBonitaException {
        assertNotNull(monitoringService.getOSName());
    }

    @Test
    public void testGetOSVersion() throws SBonitaException {
        assertNotNull(monitoringService.getOSVersion());
    }

    @Test
    public void testGetJvmName() throws SBonitaException {
        assertNotNull(monitoringService.getJvmName());
    }

    @Test
    public void testGetJvmVendor() throws SBonitaException {
        assertNotNull(monitoringService.getJvmVendor());
    }

    @Test
    public void testGetJvmVersion() throws SBonitaException {
        assertNotNull(monitoringService.getJvmVersion());
    }

    @Test
    public void testGetJvmSystemProperties() throws SBonitaException {
        final Map<String, String> systemProperties = monitoringService.getJvmSystemProperties();
        assertNotNull(systemProperties);
    }

    @Test
    public void isSchedulerStartedTest() throws HandlerUnregistrationException, MBeanStopException, STransactionCreationException, SIdentityException,
            HandlerRegistrationException, SBadTransactionStateException, FireEventException, STransactionCommitException, MBeanStartException,
            SSchedulerException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, STransactionRollbackException {
        getTransactionService().begin();
        assertFalse(monitoringService.isSchedulerStarted());

        schedulerService.start();
        assertTrue(monitoringService.isSchedulerStarted());
        schedulerService.shutdown();

        assertFalse(monitoringService.isSchedulerStarted());
        getTransactionService().complete();
    }

    @Test
    public void testGetNumberOfActiveTransactions() throws Exception {

        assertEquals(0, monitoringService.getNumberOfActiveTransactions());

        // create a transaction
        getTransactionService().begin();
        // check the transaction has been successfully counted
        assertEquals(1, monitoringService.getNumberOfActiveTransactions());
        // close the transaction
        getTransactionService().complete();

        assertEquals(0, monitoringService.getNumberOfActiveTransactions());
    }

}
