/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.Map;

import com.bonitasoft.engine.monitoring.GcInfo;
import com.bonitasoft.engine.monitoring.MonitoringException;
import com.bonitasoft.engine.monitoring.UnavailableInformationException;

/**
 * The <code>PlatformMonitoringAPI</code> allows to monitor certains indicator at platform level.
 * Some indicators are based on the JVM running the platform.
 * 
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public interface PlatformMonitoringAPI {

    /**
     * Get the sum of both heap and non-heap memory usage.
     * 
     * @return a quantity number of memory occupied currently
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    long getCurrentMemoryUsage() throws MonitoringException;

    /**
     * Returns the percentage of memory used compare to maximum available memory.
     * This calculation is based on both the heap & non-heap maximum amount of memory that can be used.
     * 
     * @return a percentage of memory occupied compare to maximum available memory
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    float getMemoryUsagePercentage() throws MonitoringException;

    /**
     * Returns the system load average for the last minute.
     * The system load average is the sum of the number of runnable entities queued to the available
     * processors and the number of runnable entities running on the available processors averaged over
     * a period of time. The way in which the load average is calculated is operating system specific
     * but is typically a damped time-dependent average.
     * If the load average is not available, a negative value is returned.
     * 
     * @return a average number of system load for the last minute
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    double getSystemLoadAverage() throws MonitoringException;

    /**
     * Returns the number of milliseconds elapsed since the Java Virtual Machine started.
     * 
     * @return a number of milliseconds elapsed since the Java Virtual Machine started
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    long getUpTime() throws MonitoringException;

    /**
     * Returns a timestamp (in millisecond) which indicates the date when the Java virtual
     * machine started.
     * Usually, a timestamp represents the time elapsed since the 1st of January, 1970.
     * 
     * @return a timestamp (in millisecond) which indicates the date when the Java virtual machine started
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    long getStartTime() throws MonitoringException;

    /**
     * Returns the total CPU time for all live threads in nanoseconds. It sums the CPU time
     * consumed by each live threads.
     * 
     * @return the total CPU time for all live threads in nanoseconds
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    long getTotalThreadsCpuTime() throws MonitoringException;

    /**
     * Returns the current number of live threads including both daemon and non-daemon threads.
     * 
     * @return the current number of live threads including both daemon and non-daemon threads
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    int getThreadCount() throws MonitoringException;

    /**
     * Returns the number of processors available to the Java virtual machine.
     * 
     * @return the number of processors available to the Java virtual machine
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    int getAvailableProcessors() throws MonitoringException;

    /**
     * Returns the operating system architecture.
     * 
     * @return the operating system architecture as a string
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    String getOSArch() throws MonitoringException;

    /**
     * Return the OS name.
     * 
     * @return the OS name
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    String getOSName() throws MonitoringException;

    /**
     * Return the OS version.
     * 
     * @return the OS version
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    String getOSVersion() throws MonitoringException;

    /**
     * Returns the Java virtual machine implementation name.
     * 
     * @return the Java virtual machine implementation name
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    String getJvmName() throws MonitoringException;

    /**
     * Returns the Java virtual machine implementation vendor.
     * 
     * @return the Java virtual machine implementation vendor
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    String getJvmVendor() throws MonitoringException;

    /**
     * Returns the Java virtual machine implementation version.
     * 
     * @return the Java virtual machine implementation version
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    String getJvmVersion() throws MonitoringException;

    /**
     * Returns the Java virtual machine System properties list.
     * 
     * @return a map of the Java virtual machine System properties
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    Map<String, String> getJvmSystemProperties() throws MonitoringException;

    /**
     * Check if the scheduler is started.
     * If it is started, return true. else return false.
     * 
     * @return if the scheduler is started
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    boolean isSchedulerStarted() throws MonitoringException;

    /**
     * Get the number of all active transactions
     * If no active transactions there, return 0
     * 
     * @return a number of active transaction
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    long getNumberOfActiveTransactions() throws MonitoringException;

    /**
     * Returns the CPU time used by the process on which the Java virtual machine is running in nanoseconds. The returned value is of nanoseconds precision but
     * not necessarily nanoseconds accuracy. This method returns -1 if the the platform does not support this operation.
     * 
     * @return the CPU time used by the process in nanoseconds, or -1 if this operation is not supported.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws UnavailableInformationException
     *             if the Process CPU time is not available for the current JVM.
     * @since 6.0
     */
    long getProcessCpuTime() throws MonitoringException, UnavailableInformationException;

    /**
     * Returns the amount of virtual memory that is guaranteed to be available to the running process in bytes, or -1 if this operation is not supported.
     * 
     * @return the amount of virtual memory
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws UnavailableInformationException
     *             if the information is not available for the current JVM
     * @since 6.0
     */
    long getCommittedVirtualMemorySize() throws MonitoringException, UnavailableInformationException;

    /**
     * Get the total amount of swap space in bytes.
     * 
     * @return the total amount of swap space in bytes
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws UnavailableInformationException
     *             if the information is not available for the current JVM
     * @since 6.0
     */
    long getTotalSwapSpaceSize() throws MonitoringException, UnavailableInformationException;

    /**
     * Get the amount of free swap space in bytes.
     * 
     * @return the amount of free swap space in bytes
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws UnavailableInformationException
     *             if the information is not available for the current JVM
     * @since 6.0
     */
    long getFreeSwapSpaceSize() throws MonitoringException, UnavailableInformationException;

    /**
     * Get the amount of free physical memory in bytes.
     * 
     * @return the amount of free physical memory in bytes.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws UnavailableInformationException
     *             if the information is not available for the current JVM
     * @since 6.0
     */
    long getFreePhysicalMemorySize() throws MonitoringException, UnavailableInformationException;

    /**
     * Get the total amount of physical memory in bytes.
     * 
     * @return the total amount of physical memory in bytes
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws UnavailableInformationException
     *             if the information is not available for the current JVM
     * @since 6.0
     */
    long getTotalPhysicalMemorySize() throws MonitoringException, UnavailableInformationException;

    /**
     * Returns true if engine is running on top of a SUN/Oracle JVM
     * 
     * @return true if engine is running on top of a SUN/Oracle JVM, false otherwise
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @since 6.0
     */
    boolean isOptionalMonitoringInformationAvailable() throws MonitoringException;

    /**
     * Returns the last GC info.
     * The key 'GcName' of the outer map is the GarbageCollectorMXBean's instance name.
     * The inner Map<String key, String value> represents the LastGcInfo instance.
     * The possible keys are StartTime, EndTime, Duration, MemoryUsageBeforeGc and MemoryUsageAfterGc.
     * 
     * @return the resulting map
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws UnavailableInformationException
     *             if the information is not available for the current JVM
     * @since 6.0
     */
    Map<String, GcInfo> getLastGcInfo() throws MonitoringException, UnavailableInformationException;

}
