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
package org.bonitasoft.engine.jobs;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class TriggerTimerEventJob extends InternalJob {

    private static final long serialVersionUID = 8727861254645155327L;

    private transient EventsHandler eventsHandler;

    private Long processDefinitionId;

    private long targetSFlowNodeDefinitionId;

    private Long flowNodeInstanceId;

    private Long parentProcessInstanceId;

    private Long rootProcessInstanceId;

    private String containerType;

    private String eventType;

    private Boolean isInterrupting;

    private Long subProcessId;

    private transient WorkService workService;
    private transient JobService jobService;
    private transient SchedulerService schedulerService;
    private transient TechnicalLoggerService loggerService;
    private Long jobDescriptorId;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void execute() throws SJobExecutionException {
        try {
            if (workService.isStopped()) {
                loggerService.log(getClass(), TechnicalLogSeverity.INFO,
                        "Rescheduling Timer job " + jobDescriptorId + " because the work service was shutdown. the timer is in process definition "
                                + processDefinitionId + " on definition element " + targetSFlowNodeDefinitionId);
                try {
                    final SJobDescriptor sJobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
                    schedulerService.executeAgain(sJobDescriptor.getId());
                } catch (final SBonitaException sbe) {
                    if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                        loggerService
                                .log(getClass(), TechnicalLogSeverity.WARNING, "Unable to reschedule the job: " + jobDescriptorId, sbe);
                    }
                }
                return;
            }
            if (subProcessId == null) {
                eventsHandler.triggerCatchEvent(eventType, processDefinitionId, targetSFlowNodeDefinitionId, flowNodeInstanceId, containerType);
            } else {
                eventsHandler.triggerCatchEvent(SEventTriggerType.TIMER, processDefinitionId, targetSFlowNodeDefinitionId, containerType, subProcessId,
                        parentProcessInstanceId, rootProcessInstanceId, isInterrupting);
            }
        } catch (final SBonitaException e) {
            throw new SJobExecutionException(e);
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        processDefinitionId = (Long) attributes.get("processDefinitionId");
        targetSFlowNodeDefinitionId = (Long) attributes.get("targetSFlowNodeDefinitionId");
        flowNodeInstanceId = (Long) attributes.get("flowNodeInstanceId");
        containerType = (String) attributes.get("containerType");
        eventType = (String) attributes.get("eventType");
        subProcessId = (Long) attributes.get("subProcessId");
        parentProcessInstanceId = (Long) attributes.get("processInstanceId");
        rootProcessInstanceId = (Long) attributes.get("rootProcessInstanceId");
        isInterrupting = (Boolean) attributes.get("isInterrupting");
        final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor();
        eventsHandler = tenantServiceAccessor.getEventsHandler();
        workService = tenantServiceAccessor.getWorkService();
        jobService = tenantServiceAccessor.getJobService();
        schedulerService = tenantServiceAccessor.getSchedulerService();
        loggerService = tenantServiceAccessor.getTechnicalLoggerService();
        jobDescriptorId = (Long) attributes.get(JOB_DESCRIPTOR_ID);
    }
}
