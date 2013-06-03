/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.scheduler.impl.SchedulerImpl;

/**
 * @author Matthieu Chaffotte
 */
public interface SchedulerExecutor {

    void schedule(final JobIdentifier job, final Trigger trigger) throws SSchedulerException;

    void executeNow(JobIdentifier jobIdentifier) throws SSchedulerException;

    boolean isStarted() throws SSchedulerException;

    boolean isShutdown() throws SSchedulerException;

    void start() throws SSchedulerException;

    void shutdown() throws SSchedulerException;

    void reschedule(final String triggerName, final Trigger newTrigger) throws SSchedulerException;

    void resume(final String jobName) throws SSchedulerException;

    void resumeJobs() throws SSchedulerException;

    void pause(final String jobName) throws SSchedulerException;

    void pauseJobs() throws SSchedulerException;

    boolean delete(final String jobName) throws SSchedulerException;

    void deleteJobs() throws SSchedulerException;

    List<String> getJobs() throws SSchedulerException;

    void setBOSSchedulerService(SchedulerImpl schedulerService);

    List<String> getAllJobs() throws SSchedulerException;

}
