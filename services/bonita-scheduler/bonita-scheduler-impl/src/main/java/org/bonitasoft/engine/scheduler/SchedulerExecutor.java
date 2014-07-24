/**
 * Copyright (C) 2011-2014 BonitaSoft S.A.
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

    void start() throws SSchedulerException;

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

}
