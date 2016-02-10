/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.scheduler;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.impl.SchedulerServiceImpl;
import org.bonitasoft.engine.scheduler.trigger.Trigger;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface SchedulerExecutor {

    boolean isStarted() throws SSchedulerException;

    boolean isShutdown() throws SSchedulerException;

    /**
     * Note that once a scheduler is shutdown, it cannot be restarted without being re-instantiated.
     *
     * @throws SSchedulerException
     * @see {@link #initializeScheduler()}
     * @since 6.4.0
     */
    void start() throws SSchedulerException;

    /**
     * Note that once a scheduler is shutdown, it cannot be restarted without being re-instantiated.
     *
     * @throws SSchedulerException
     * @see {@link #initializeScheduler()}
     * @since 6.4.0
     */
    void shutdown() throws SSchedulerException;

    void rescheduleErroneousTriggers() throws SSchedulerException;

    boolean delete(String jobName, String groupName) throws SSchedulerException;

    void deleteJobs(String groupName) throws SSchedulerException;

    List<String> getJobs(String groupName) throws SSchedulerException;

    void setBOSSchedulerService(SchedulerServiceImpl schedulerService);

    List<String> getAllJobs() throws SSchedulerException;

    void executeNow(long jobId, String groupName, String jobName, boolean disallowConcurrentExecution) throws SSchedulerException;

    void schedule(long jobId, String groupName, String jobName, Trigger trigger, boolean disallowConcurrentExecution) throws SSchedulerException;

    boolean isStillScheduled(String groupName, String jobName) throws SSchedulerException;

    void executeAgain(long jobId, String groupName, String jobName, boolean disallowConcurrentExecution) throws SSchedulerException;

    void pauseJobs(String groupName) throws SSchedulerException;

    void resumeJobs(String groupName) throws SSchedulerException;

    /**
     * Remove (delete) the <code>{@link org.quartz.Trigger}</code> with the given key, and store the new given one - which must be associated
     * with the same job (the new trigger must have the job name & group specified)
     * - however, the new trigger need not have the same name as the old trigger.
     *
     * @param triggerName
     *        The name of the trigger to replace
     * @param groupName
     *        The group name of the trigger to replace
     * @param triggerStartTime
     *        The start date of the new trigger
     * @return <code>null</code> if a <code>Trigger</code> with the given
     *         name & group was not found and removed from the store (and the
     *         new trigger is therefore not stored), otherwise
     *         the first fire time of the newly scheduled trigger is returned.
     * @throws SSchedulerException
     * @since 6.4.0
     */
    Date rescheduleJob(String triggerName, String groupName, Date triggerStartTime) throws SSchedulerException;

    /**
     * Add the given <code>{@link AbstractBonitaTenantJobListener}s</code> to the <code>Scheduler</code>, and register it to receive events for Jobs that are
     * matched by the group name.
     *
     * @param jobListeners
     *        The job listeners to add to the scheduler
     * @param groupName
     *        The group name to filter
     * @throws SSchedulerException
     * @since 6.4.0
     */
    void addJobListener(List<AbstractBonitaTenantJobListener> jobListeners, String groupName) throws SSchedulerException;

    /**
     * Add the given <code>{@link AbstractBonitaPlatformJobListener}s</code> to the <code>Scheduler</code>, and register it to receive events for all Jobs.
     *
     * @param jobListeners
     *        The job listeners to add to the scheduler
     * @throws SSchedulerException
     * @since 6.4.0
     */
    void addJobListener(List<AbstractBonitaPlatformJobListener> jobListeners) throws SSchedulerException;

    /**
     * Initialize the scheduler if this method has not be previously called (after shutdown); otherwise, do nothing.
     *
     * @throws SSchedulerException
     * @since 6.4.0
     */
    void initializeScheduler() throws SSchedulerException;

    /**
     * Check if a job exists.
     *
     * @param jobName
     *        The name of the job
     * @param groupName
     *        The group of the job
     * @return True if the job exists, else False.
     * @throws SSchedulerException
     * @since 6.4.0
     */
    boolean isExistingJob(String jobName, String groupName) throws SSchedulerException;

}
