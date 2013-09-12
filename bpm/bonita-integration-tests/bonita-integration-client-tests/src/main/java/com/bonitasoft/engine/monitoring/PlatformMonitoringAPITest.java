package com.bonitasoft.engine.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;

/**
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 */
public class PlatformMonitoringAPITest extends CommonAPISPTest {

    @After
    public void afterTest() throws Exception {
        logout();
    }

    @Before
    public void beforeTest() throws Exception {
        login();
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Memory", "Usage" }, story = "Get current memory usage.")
    @Test
    public void getCurrentMemoryUsage() throws BonitaException {
        assertTrue(getPlatformMonitoringAPI().getCurrentMemoryUsage() > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Memory", "Usage", "Percentage" }, story = "Get memory usage percentage.")
    @Test
    public void getMemoryUsagePercentage() throws BonitaException {
        final float memoryUsagePercentage = getPlatformMonitoringAPI().getMemoryUsagePercentage();
        assertTrue(memoryUsagePercentage >= 0);
        assertTrue(memoryUsagePercentage <= 100);
    }

    // // FIXME: add this test for OS supporting this feature
    // @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "System load average" }, story = "Get system load average.")
    // @Test
    // public void getSystemLoadAverage() throws BonitaException {
    // final double result = getPlatformMonitoringAPI().getSystemLoadAverage();
    //
    // assertTrue(result > 0);
    // }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Up time" }, story = "Get up time.")
    @Test
    public void getUpTime() throws BonitaException {
        assertTrue(getPlatformMonitoringAPI().getUpTime() > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Start time" }, story = "Get start time.")
    @Test
    public void getStartTime() throws BonitaException {
        assertTrue(getPlatformMonitoringAPI().getStartTime() > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Thread", "CPU time" }, story = "Get total threads CPU time.")
    @Test
    public void getTotalThreadsCpuTime() throws BonitaException {
        assertTrue(getPlatformMonitoringAPI().getTotalThreadsCpuTime() > 0);
        final long result = getPlatformMonitoringAPI().getTotalThreadsCpuTime();

        assertTrue(result > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Thread" }, story = "Get thread count.")
    @Test
    public void getThreadCount() throws BonitaException {
        assertTrue(getPlatformMonitoringAPI().getThreadCount() > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Processor" }, story = "Get available processors.")
    @Test
    public void getAvailableProcessors() throws BonitaException {
        assertTrue(getPlatformMonitoringAPI().getAvailableProcessors() > 0);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = "OS architecture", story = "Get OS architecture.")
    @Test
    public void getOSArch() throws BonitaException {
        assertNotNull(getPlatformMonitoringAPI().getOSArch());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "OS name" }, story = "Get OS name.")
    @Test
    public void getOSName() throws BonitaException {
        assertNotNull(getPlatformMonitoringAPI().getOSName());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "OS version" }, story = "Get OS version.")
    @Test
    public void getOSVersion() throws BonitaException {
        assertNotNull(getPlatformMonitoringAPI().getOSVersion());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "JVM name" }, story = "Get JVM name.")
    @Test
    public void getJvmName() throws BonitaException {
        assertNotNull(getPlatformMonitoringAPI().getJvmName());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "JVM vendor" }, story = "Get JVM vendor.")
    @Test
    public void getJvmVendor() throws BonitaException {
        assertNotNull(getPlatformMonitoringAPI().getJvmVendor());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "JVM version" }, story = "Get JVM version.")
    @Test
    public void getJvmVersion() throws BonitaException {
        assertNotNull(getPlatformMonitoringAPI().getJvmVersion());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "JVM", "System properties" }, story = "Get JVM system properties.")
    @Test
    public void getJvmSystemProperties() throws BonitaException {
        final Map<String, String> systemProperties = getPlatformMonitoringAPI().getJvmSystemProperties();
        assertNotNull(systemProperties);
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Scheduler" }, story = "Test if scheduler is started.")
    @Test
    public void isSchedulerStartedTest() throws Exception {
        // TODO how to improve that?
        assertTrue("The scheduler should be started", getPlatformMonitoringAPI().isSchedulerStarted());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Transaction", "Active" }, story = "Get number of active transactions.")
    @Test
    public void getNumberOfActiveTransactions() throws Exception {
        Thread.sleep(500);// wait for potential work to finish
        assertEquals(0, getPlatformMonitoringAPI().getNumberOfActiveTransactions());
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.PROCESS, keywords = { "Process", "CPU time" }, story = "Get process cpu time", jira = "ENGINE-620")
    @Test
    public void getProcessCpuTime() throws Exception {
        if (getPlatformMonitoringAPI().isOptionalMonitoringInformationAvailable()) {
            assertTrue(getPlatformMonitoringAPI().getProcessCpuTime() > 0);
        } else {
            try {
                getPlatformMonitoringAPI().getProcessCpuTime();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Commited", "Virtual memory", "Size" }, story = "Get committed virtual memory size.", jira = "ENGINE-620")
    @Test
    public void getCommittedVirtualMemorySize() throws Exception {
        if (getPlatformMonitoringAPI().isOptionalMonitoringInformationAvailable()) {
            assertTrue(getPlatformMonitoringAPI().getCommittedVirtualMemorySize() > 0);
        } else {
            try {
                getPlatformMonitoringAPI().getCommittedVirtualMemorySize();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Swap space", "Size" }, story = "Get total swap space size.", jira = "ENGINE-620")
    @Test
    public void getTotalSwapSpaceSize() throws Exception {
        if (getPlatformMonitoringAPI().isOptionalMonitoringInformationAvailable()) {
            assertTrue(getPlatformMonitoringAPI().getTotalSwapSpaceSize() >= 0);
        } else {
            try {
                getPlatformMonitoringAPI().getTotalSwapSpaceSize();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Swap space", "Size" }, story = "Get free swap space size.", jira = "ENGINE-620")
    @Test
    public void getFreeSwapSpaceSize() throws Exception {
        if (getPlatformMonitoringAPI().isOptionalMonitoringInformationAvailable()) {
            final long freeSwapSpaceSize = getPlatformMonitoringAPI().getFreeSwapSpaceSize();
            assertTrue("Unexpected free swap memory size: " + freeSwapSpaceSize, freeSwapSpaceSize >= 0);
        } else {
            try {
                getPlatformMonitoringAPI().getFreeSwapSpaceSize();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Physical memory", "Size" }, story = " Get free physical memory size.", jira = "ENGINE-620")
    @Test
    public void getFreePhysicalMemorySize() throws Exception {
        if (getPlatformMonitoringAPI().isOptionalMonitoringInformationAvailable()) {
            assertTrue(getPlatformMonitoringAPI().getFreePhysicalMemorySize() > 0);
        } else {
            try {
                getPlatformMonitoringAPI().getFreePhysicalMemorySize();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Physical memory", "Size" }, story = "Get total physical memory size.", jira = "ENGINE-620")
    @Test
    public void getTotalPhysicalMemorySize() throws Exception {
        if (getPlatformMonitoringAPI().isOptionalMonitoringInformationAvailable()) {
            assertTrue(getPlatformMonitoringAPI().getTotalPhysicalMemorySize() > 0);
        } else {
            try {
                getPlatformMonitoringAPI().getTotalPhysicalMemorySize();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Monitoring information", "Optional" }, story = "Test if optional monitoring information are available.")
    @Test
    public void isOptionalMonitoringInformationAvailable() throws Exception {
        if (getPlatformMonitoringAPI().getJvmVendor().indexOf("Sun") >= 0) {
            assertTrue(getPlatformMonitoringAPI().isOptionalMonitoringInformationAvailable());
        } else {
            assertFalse(getPlatformMonitoringAPI().isOptionalMonitoringInformationAvailable());
        }
    }

    @Cover(classes = PlatformMonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Information" }, story = "Get last gc information.", jira = "ENGINE-620")
    @Test
    public void getLastGcInfo() throws Exception {
        if (getPlatformMonitoringAPI().isOptionalMonitoringInformationAvailable()) {
            final Map<String, GcInfo> lastGcInfos = getPlatformMonitoringAPI().getLastGcInfo();
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
                getPlatformMonitoringAPI().getLastGcInfo();
                fail();
            } catch (final UnavailableInformationException ex) {
                // Do nothing
            }
        }
    }

}
