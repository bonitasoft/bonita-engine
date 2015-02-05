/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.mbean;

import java.util.Map;

/**
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 */
public interface SJvmMXBean extends BonitaMXBean {

    /**
     * Returns the sum of both heap and non-heap memory usage.
     */
    long getMemoryUsage();

    /**
     * Returns the percentage of memory used compare to maximum available memory.
     * This calculation is based on both the heap & non-heap maximum amount of memory that can be used.
     * 
     * @throws MonitoringException
     */
    float getMemoryUsagePercentage();

    /**
     * Returns the system load average for the last minute.
     * The system load average is the sum of the number of runnable entities queued to the available
     * processors and the number of runnable entities running on the available processors averaged over
     * a period of time. The way in which the load average is calculated is operating system specific
     * but is typically a damped time-dependent average.
     * If the load average is not available, a negative value is returned.
     */
    double getSystemLoadAverage();

    /**
     * Returns the number of milliseconds elapsed since the Java Virtual Machine started.
     */
    long getUpTime();

    /**
     * Returns a timestamp (in millisecond) which indicates the date when the Java virtual
     * machine started.
     * Usually, a timestamp represents the time elapsed since the 1st of January, 1970.
     */
    long getStartTime();

    /**
     * Returns the total CPU time for all live threads in nanoseconds. It sums the CPU time
     * consumed by each live threads.
     */
    long getTotalThreadsCpuTime();

    /**
     * Returns the current number of live threads including both daemon and non-daemon threads.
     */
    int getThreadCount();

    /**
     * Returns the operating system architecture
     */
    String getOSArch();

    /**
     * Returns the number of processors available to the Java virtual machine.
     */
    int getAvailableProcessors();

    /**
     * Return the OS name
     */
    String getOSName();

    /**
     * Return the OS version
     */
    String getOSVersion();

    /**
     * Returns the Java virtual machine implementation name
     */
    String getJvmName();

    /**
     * Returns the Java virtual machine implementation vendor
     */
    String getJvmVendor();

    /**
     * Returns the Java virtual machine implementation version
     */
    String getJvmVersion();

    /**
     * Returns the Java virtual machine System properties list
     */
    Map<String, String> getJvmSystemProperties();

}
