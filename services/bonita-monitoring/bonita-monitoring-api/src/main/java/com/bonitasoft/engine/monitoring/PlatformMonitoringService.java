/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring;

import java.util.Map;

import com.bonitasoft.engine.monitoring.mbean.SJvmMXBean;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public interface PlatformMonitoringService extends MonitoringService {

    String JVM_MBEAN_NAME = "Bonitasoft:name=JVM,type=JVMMBean";

    String SERVICE_MBEAN_NAME = "Bonitasoft:name=Service,type=ServiceMBean";

    SJvmMXBean getJvmMBean();

    /**
     * Get true if the scheduler service is started, false if it is stopped.
     *
     * @return true if the scheduler service is started, false if it is stopped, else return false.
     * @since 6.0
     */
    boolean isSchedulerStarted() throws SBonitaException;

    /**
     * Get the current number of active transactions at platform level.
     *
     * @return the current number of active transactions at platform level.
     * @since 6.0
     */
    long getNumberOfActiveTransactions();

    /**
     * Get the sum of both heap and non-heap memory usage.
     *
     * @return the sum of both heap and non-heap memory usage.
     * @since 6.0
     */
    long getCurrentMemoryUsage();

    /**
     * Get the percentage of memory used compare to maximum available memory.
     * This calculation is based on both the heap & non-heap maximum amount of memory that can be used.
     *
     * @throws SMonitoringException
     * @since 6.0
     */
    float getMemoryUsagePercentage();

    /**
     * Get the system load average for the last minute.
     * The system load average is the sum of the number of runnable entities queued to the available
     * processors and the number of runnable entities running on the available processors averaged over
     * a period of time. The way in which the load average is calculated is operating system specific
     * but is typically a damped time-dependent average.
     * If the load average is not available, a negative value is returned.
     *
     * @since 6.0
     */
    double getSystemLoadAverage();

    /**
     * Get the number of milliseconds elapsed since the Java Virtual Machine started.
     *
     * @return the number of milliseconds elapsed since the Java Virtual Machine started.
     * @since 6.0
     */
    long getUpTime();

    /**
     * Get a timestamp (in millisecond) which indicates the date when the Java virtual
     * machine started.
     * Usually, a timestamp represents the time elapsed since the 1st of January, 1970.
     *
     * @return a long of start time.
     * @since 6.0
     */
    long getStartTime();

    /**
     * Get the total CPU time for all live threads in nanoseconds. It sums the CPU time
     * consumed by each live threads.
     *
     * @since 6.0
     */
    long getTotalThreadsCpuTime();

    /**
     * Get the current number of live threads including both daemon and non-daemon threads.
     *
     * @return the current number of live threads including both daemon and non-daemon threads.
     * @since 6.0
     */
    int getThreadCount();

    /**
     * Get the number of processors available to the Java virtual machine.
     *
     * @return the number of processors available to the Java virtual machine.
     * @since 6.0
     */
    int getAvailableProcessors();

    /**
     * Get the operating system architecture
     *
     * @return the operating system architecture
     * @since 6.0
     */
    String getOSArch();

    /**
     * Get the OS name
     *
     * @return the OS name
     * @since 6.0
     */
    String getOSName();

    /**
     * Get the OS version
     *
     * @return the OS version
     * @since 6.0
     */
    String getOSVersion();

    /**
     * Get the Java virtual machine implementation name
     *
     * @return the Java virtual machine implementation name
     * @since 6.0
     */
    String getJvmName();

    /**
     * Get the Java virtual machine implementation vendor
     *
     * @return the Java virtual machine implementation vendor
     * @since 6.0
     */
    String getJvmVendor();

    /**
     * Get the Java virtual machine implementation version
     *
     * @return the Java virtual machine implementation version
     * @since 6.0
     */
    String getJvmVersion();

    /**
     * Get the Java virtual machine System properties list
     *
     * @return the Java virtual machine System properties list
     * @since 6.0
     */
    Map<String, String> getJvmSystemProperties();

    /**
     * Get the CPU time used by the process in nanoseconds, or -1 if this operation is not supported.
     *
     * @return the CPU time used by the process in nanoseconds.
     * @since 6.0
     */
    long getProcessCpuTime();

    /**
     * Get the amount of virtual memory that is guaranteed to be available to the running process in bytes, or -1 if this operation is not supported.
     *
     * @return the amount of virtual memory that is guaranteed to be available to the running process in bytes, or -1 if this operation is not supported.
     * @since 6.0
     */
    long getCommittedVirtualMemorySize();

    /**
     * Get the total amount of swap space in bytes.
     *
     * @return the total amount of swap space in bytes.
     * @since 6.0
     */
    long getTotalSwapSpaceSize();

    /**
     * Get the amount of free swap space in bytes.
     *
     * @return the amount of free swap space in bytes.
     * @since 6.0
     */
    long getFreeSwapSpaceSize();

    /**
     * Get the amount of free physical memory in bytes.
     *
     * @return the amount of free physical memory in bytes.
     * @since 6.0
     */
    long getFreePhysicalMemorySize();

    /**
     * Get the total amount of physical memory in bytes.
     *
     * @return the total amount of physical memory in bytes.
     * @since 6.0
     */
    long getTotalPhysicalMemorySize();

    /**
     * Get true if engine is running on top of a SUN/Oracle JVM.
     *
     * @return true if engine is running on top of a SUN/Oracle JVM, else return false.
     * @since 6.0
     */
    boolean isOptionalMonitoringInformationAvailable();

    /**
     * Get a map of last garbage collector MXBeans.
     *
     * @return a map of last garbage collector MXBeans.
     * @since 6.0
     */
    Map<String, SGcInfo> getLastGcInfo();


    /**
     * Get the current number of executing jobs.
     *
     * @return the current number of executing jobs.
     * @since 6.0
     */
    long getNumberOfExecutingJobs();

}
