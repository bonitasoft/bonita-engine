/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.restart.TenantRestartHandler;
import org.bonitasoft.engine.scheduler.JobRegister;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class NodeConfigurationImpl implements NodeConfiguration {

    private boolean shouldStartScheduler = true;;

    private boolean shouldRestartElements = true;

    private List<RestartHandler> restartHandlers;

    private String eventHandlingJobCron = "*/5 * * * * ?";

    private String cleanInvalidSessionsJobCron = "0 0 */2 * * ?";

    private boolean shouldStartEventHandlingJob = true;

    private List<TenantRestartHandler> tenantRestartHandlers;

    private List<JobRegister> jobsToRegister;

    @Override
    public boolean shouldStartScheduler() {
        return shouldStartScheduler;
    }

    @Override
    public boolean shouldResumeElements() {
        return shouldRestartElements;
    }

    @Override
    public List<RestartHandler> getRestartHandlers() {
        final List<RestartHandler> emptyList = Collections.emptyList();
        return restartHandlers == null ? emptyList : restartHandlers;
    }

    public void setRestartHandlers(final List<RestartHandler> restartHandlers) {
        this.restartHandlers = restartHandlers;
    }

    @Override
    public List<TenantRestartHandler> getTenantRestartHandlers() {
        final List<TenantRestartHandler> emptyList = Collections.emptyList();
        return tenantRestartHandlers == null ? emptyList : tenantRestartHandlers;
    }

    public void setTenantRestartHandlers(final List<TenantRestartHandler> tenantRestartHandlers) {
        this.tenantRestartHandlers = tenantRestartHandlers;
    }

    @Override
    public String getEventHandlingJobCron() {
        return eventHandlingJobCron;
    }

    @Override
    public boolean shouldStartEventHandlingJob() {
        return shouldStartEventHandlingJob;
    }

    public boolean getShouldStartScheduler() {
        return shouldStartScheduler;
    }

    public boolean getShouldRestartElements() {
        return shouldRestartElements;
    }

    public boolean getShouldStartEventHandlingJob() {
        return shouldStartEventHandlingJob;
    }

    public void setShouldStartScheduler(final boolean shouldStartScheduler) {
        this.shouldStartScheduler = shouldStartScheduler;
    }

    public void setShouldRestartElements(final boolean shouldRestartElements) {
        this.shouldRestartElements = shouldRestartElements;
    }

    public void setEventHandlingJobCron(final String eventHandlingJobCron) {
        this.eventHandlingJobCron = eventHandlingJobCron;
    }

    public void setShouldStartEventHandlingJob(final boolean shouldStartEventHandlingJob) {
        this.shouldStartEventHandlingJob = shouldStartEventHandlingJob;
    }

    public void setJobsToRegister(final List<JobRegister> jobsToRegister) {
        this.jobsToRegister = jobsToRegister;
    }

    @Override
    public List<JobRegister> getJobsToRegister() {
        if (jobsToRegister == null) {
            return Collections.emptyList();
        } else {
            return jobsToRegister;
        }
    }

    @Override
    public String getCleanInvalidSessionsJobCron() {
        return cleanInvalidSessionsJobCron;
    }

    public void setCleanInvalidSessionsJobCron(final String cleanInvalidSessionsJobCron) {
        this.cleanInvalidSessionsJobCron = cleanInvalidSessionsJobCron;
    }

}
