package com.bonitasoft.engine.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.UnavailableInformationException;
import org.bonitasoft.engine.management.GcInfo;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;

/**
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 */
public class PlatformMonitoringAPITest extends CommonAPITest {

    private static PlatformMonitoringAPI monitoringAPI;

    private static PlatformSession session;

    public PlatformMonitoringAPITest() throws BonitaException {
        session = APITestUtil.loginPlatform();
        monitoringAPI = PlatformAPIAccessor.getPlatformMonitoringAPI(session);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Memory", "Usage" }, story = "Get current memory usage.")
    @Test
    public void testGetCurrentMemoryUsage() throws BonitaException {
        assertTrue(monitoringAPI.getCurrentMemoryUsage() > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Memory", "Usage", "Percentage" }, story = "Get memory usage percentage.")
    @Test
    public void testGetMemoryUsagePercentage() throws BonitaException {
        final float memoryUsagePercentage = monitoringAPI.getMemoryUsagePercentage();
        assertTrue(memoryUsagePercentage >= 0);
        assertTrue(memoryUsagePercentage <= 100);
    }

    // // FIXME: add this test for OS supporting this feature
    // @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "System load average" }, story = "Get system load average.")
    // @Test
    // public void testGetSystemLoadAverage() throws BonitaException {
    // final double result = monitoringAPI.getSystemLoadAverage();
    //
    // assertTrue(result > 0);
    // }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Up time" }, story = "Get up time.")
    @Test
    public void testGetUpTime() throws BonitaException {
        assertTrue(monitoringAPI.getUpTime() > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Start time" }, story = "Get start time.")
    @Test
    public void testGetStartTime() throws BonitaException {
        assertTrue(monitoringAPI.getStartTime() > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Thread", "CPU time" }, story = "Get total threads CPU time.")
    @Test
    public void testGetTotalThreadsCpuTime() throws BonitaException {
        assertTrue(monitoringAPI.getTotalThreadsCpuTime() > 0);
        final long result = monitoringAPI.getTotalThreadsCpuTime();

        assertTrue(result > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Thread" }, story = "Get thread count.")
    @Test
    public void testGetThreadCount() throws BonitaException {
        assertTrue(monitoringAPI.getThreadCount() > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Processor" }, story = "Get available processors.")
    @Test
    public void testGetAvailableProcessors() throws BonitaException {
        assertTrue(monitoringAPI.getAvailableProcessors() > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = "OS architecture", story = "Get OS architecture.")
    @Test
    public void testGetOSArch() throws BonitaException {
        assertNotNull(monitoringAPI.getOSArch());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "OS name" }, story = "Get OS name.")
    @Test
    public void testGetOSName() throws BonitaException {
        assertNotNull(monitoringAPI.getOSName());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "OS version" }, story = "Get OS version.")
    @Test
    public void testGetOSVersion() throws BonitaException {
        assertNotNull(monitoringAPI.getOSVersion());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "JVM name" }, story = "Get JVM name.")
    @Test
    public void testGetJvmName() throws BonitaException {
        assertNotNull(monitoringAPI.getJvmName());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "JVM vendor" }, story = "Get JVM vendor.")
    @Test
    public void testGetJvmVendor() throws BonitaException {
        assertNotNull(monitoringAPI.getJvmVendor());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "JVM version" }, story = "Get JVM version.")
    @Test
    public void testGetJvmVersion() throws BonitaException {
        assertNotNull(monitoringAPI.getJvmVersion());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "JVM", "System properties" }, story = "Get JVM system properties.")
    @Test
    public void testGetJvmSystemProperties() throws BonitaException {
        final Map<String, String> systemProperties = monitoringAPI.getJvmSystemProperties();
        assertNotNull(systemProperties);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Scheduler" }, story = "Test if scheduler is started.")
    @Test
    public void isSchedulerStartedTest() throws Exception {
        // TODO how to improve that?
        assertTrue(monitoringAPI.isSchedulerStarted());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Transaction", "Active" }, story = "Get number of active transactions.")
    @Test
    public void testGetNumberOfActiveTransactions() throws Exception {
        Thread.sleep(500);// wait for potentiel work to finish
        assertEquals(0, monitoringAPI.getNumberOfActiveTransactions());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.PROCESS, keywords = { "Process", "CPU time" }, story = "Get process cpu time", jira = "ENGINE-620")
    @Test
    public void testGetProcessCpuTime() throws Exception {
        if (monitoringAPI.isOptionalMonitoringInformationAvailable()) {
            assertTrue(monitoringAPI.getProcessCpuTime() > 0);
        } else {
            try {
                monitoringAPI.getProcessCpuTime();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Commited", "Virtual memory", "Size" }, story = "Get committed virtual memory size.", jira = "ENGINE-620")
    @Test
    public void testGetCommittedVirtualMemorySize() throws Exception {
        if (monitoringAPI.isOptionalMonitoringInformationAvailable()) {
            assertTrue(monitoringAPI.getCommittedVirtualMemorySize() > 0);
        } else {
            try {
                monitoringAPI.getCommittedVirtualMemorySize();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Swap space", "Size" }, story = "Get total swap space size.", jira = "ENGINE-620")
    @Test
    public void testGetTotalSwapSpaceSize() throws Exception {
        if (monitoringAPI.isOptionalMonitoringInformationAvailable()) {
            assertTrue(monitoringAPI.getTotalSwapSpaceSize() > 0);
        } else {
            try {
                monitoringAPI.getTotalSwapSpaceSize();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Swap space", "Size" }, story = "Get free swap space size.", jira = "ENGINE-620")
    @Test
    public void testGetFreeSwapSpaceSize() throws Exception {
        if (monitoringAPI.isOptionalMonitoringInformationAvailable()) {
            final long freeSwapSpaceSize = monitoringAPI.getFreeSwapSpaceSize();
            assertTrue("Unexpected free swap memory size: " + freeSwapSpaceSize, freeSwapSpaceSize >= 0);
        } else {
            try {
                monitoringAPI.getFreeSwapSpaceSize();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Physical memory", "Size" }, story = " Get free physical memory size.", jira = "ENGINE-620")
    @Test
    public void testGetFreePhysicalMemorySize() throws Exception {
        if (monitoringAPI.isOptionalMonitoringInformationAvailable()) {
            assertTrue(monitoringAPI.getFreePhysicalMemorySize() > 0);
        } else {
            try {
                monitoringAPI.getFreePhysicalMemorySize();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Physical memory", "Size" }, story = "Get total physical memory size.", jira = "ENGINE-620")
    @Test
    public void testGetTotalPhysicalMemorySize() throws Exception {
        if (monitoringAPI.isOptionalMonitoringInformationAvailable()) {
            assertTrue(monitoringAPI.getTotalPhysicalMemorySize() > 0);
        } else {
            try {
                monitoringAPI.getTotalPhysicalMemorySize();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Monitoring information", "Optional" }, story = "Test if optional monitoring information are available.")
    @Test
    public void testIsOptionalMonitoringInformationAvailable() throws Exception {
        if (monitoringAPI.getJvmVendor().indexOf("Sun") >= 0) {
            assertTrue(monitoringAPI.isOptionalMonitoringInformationAvailable());
        } else {
            assertFalse(monitoringAPI.isOptionalMonitoringInformationAvailable());
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Information" }, story = "Get last gc information.", jira = "ENGINE-620")
    @Test
    public void testGetLastGcInfo() throws Exception {
        if (monitoringAPI.isOptionalMonitoringInformationAvailable()) {
            final Map<String, GcInfo> lastGcInfos = monitoringAPI.getLastGcInfo();
            assertNotNull(lastGcInfos);
            assertTrue(lastGcInfos.size() > 0);
            for (final Entry<String, GcInfo> lastGcInfo : lastGcInfos.entrySet()) {
                final GcInfo gcInfo = lastGcInfo.getValue();
                assertTrue(gcInfo.getStartTime() >= 0);
                assertTrue(gcInfo.getEndTime() >= 0);
                assertTrue(gcInfo.getDuration() >= 0);
                assertNotNull(gcInfo.getMemoryUsageBeforeGc());
                assertNotNull(gcInfo.getMemoryUsageAfterGc());
            }
        } else {
            try {
                monitoringAPI.getLastGcInfo();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

}
