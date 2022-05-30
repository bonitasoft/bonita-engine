/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.tenant.TenantServicesManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TriggerTimerEventJobTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private TriggerTimerEventJob triggerTimerEventJob = new TriggerTimerEventJob() {

        @Override
        protected TenantServiceAccessor getTenantServiceAccessor() {
            return tenantServiceAccessor;
        }
    };
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private EventsHandler eventsHandler;
    @Mock
    private JobService jobService;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private EventInstanceService eventInstanceService;
    @Mock
    private TenantServicesManager tenantServicesManager;

    private Long processDefinitionId = 1234L;
    private Long targetSFlowNodeDefinitionId = 32497L;
    private Long flowNodeInstanceId = 43298L;
    private String containerType = "containerType";
    private String eventType = SBPMEventType.INTERMEDIATE_CATCH_EVENT.name();
    private Long subProcessId = null;
    private Long processInstanceId = 646534L;
    private Long rootProcessInstanceId = 3412L;
    private Boolean isInterrupting = false;
    private Long jobDescriptorId = 3209494L;

    @Before
    public void before() throws Exception {
        doReturn(eventsHandler).when(tenantServiceAccessor).getEventsHandler();
        doReturn(jobService).when(tenantServiceAccessor).getJobService();
        doReturn(schedulerService).when(tenantServiceAccessor).getSchedulerService();
        doReturn(eventInstanceService).when(tenantServiceAccessor).getEventInstanceService();
        doReturn(tenantServicesManager).when(tenantServiceAccessor).getTenantServicesManager();
        doReturn(true).when(tenantServicesManager).isStarted();
    }

    private void setAttributes() throws SJobConfigurationException {
        Map<String, Serializable> attributes = new HashMap<>();
        attributes.put("processDefinitionId", processDefinitionId);
        attributes.put("targetSFlowNodeDefinitionId", targetSFlowNodeDefinitionId);
        attributes.put("flowNodeInstanceId", flowNodeInstanceId);
        attributes.put("containerType", containerType);
        attributes.put("eventType", eventType);
        attributes.put("subProcessId", subProcessId);
        attributes.put("processInstanceId", processInstanceId);
        attributes.put("rootProcessInstanceId", rootProcessInstanceId);
        attributes.put("isInterrupting", isInterrupting);
        attributes.put(StatelessJob.JOB_DESCRIPTOR_ID, jobDescriptorId);
        triggerTimerEventJob.setAttributes(attributes);
    }

    @Test(expected = SRetryableException.class)
    public void should_throw_retryableException_when_tenant_is_stopped() throws Exception {
        SJobDescriptor jobDescriptor = SJobDescriptor.builder().id(jobDescriptorId).build();
        doReturn(jobDescriptor).when(jobService).getJobDescriptor(jobDescriptorId);
        setAttributes();
        doReturn(false).when(tenantServicesManager).isStarted();

        triggerTimerEventJob.execute();

    }

    @Test
    public void should_trigger_timer_for_event_sub_process() throws Exception {
        eventType = SBPMEventType.EVENT_SUB_PROCESS.name();
        subProcessId = 31289032198L;
        setAttributes();

        triggerTimerEventJob.execute();

        verify(eventsHandler).triggerCatchEvent(SEventTriggerType.TIMER, processDefinitionId,
                targetSFlowNodeDefinitionId,
                containerType, subProcessId, processInstanceId, rootProcessInstanceId, isInterrupting);
    }

    @Test
    public void should_trigger_timer_for_start_process() throws Exception {
        eventType = SBPMEventType.START_EVENT.name();
        flowNodeInstanceId = null;
        setAttributes();

        triggerTimerEventJob.execute();

        verify(eventsHandler).triggerCatchEvent(eventType, processDefinitionId, targetSFlowNodeDefinitionId,
                null, containerType);
    }

    @Test
    public void should_trigger_timer_for_catch_event() throws Exception {
        setAttributes();

        triggerTimerEventJob.execute();

        verify(eventsHandler).triggerCatchEvent(eventType, processDefinitionId, targetSFlowNodeDefinitionId,
                flowNodeInstanceId, containerType);
    }

    @Test
    public void should_delete_EventTriggerInstance() throws Exception {
        setAttributes();
        STimerEventTriggerInstance timerEventTriggerInstance = new STimerEventTriggerInstance();
        timerEventTriggerInstance.setId(32143L);
        doReturn(Optional.of(timerEventTriggerInstance)).when(eventInstanceService)
                .getTimerEventTriggerInstanceOfFlowNode(flowNodeInstanceId);

        triggerTimerEventJob.execute();

        verify(eventInstanceService).deleteEventTriggerInstance(timerEventTriggerInstance);
    }

}
