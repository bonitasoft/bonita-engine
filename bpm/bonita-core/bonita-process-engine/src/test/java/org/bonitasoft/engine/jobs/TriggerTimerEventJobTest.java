package org.bonitasoft.engine.jobs;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TriggerTimerEventJobTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private TriggerTimerEventJob triggerTimerEventJob = new TriggerTimerEventJob(){
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
    private WorkService workService;
    @Mock
    private JobService jobService;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private TechnicalLoggerService loggerService;
    @Mock
    private EventInstanceService eventInstanceService;
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
        doReturn(workService).when(tenantServiceAccessor).getWorkService();
        doReturn(jobService).when(tenantServiceAccessor).getJobService();
        doReturn(schedulerService).when(tenantServiceAccessor).getSchedulerService();
        doReturn(loggerService).when(tenantServiceAccessor).getTechnicalLoggerService();
        doReturn(eventInstanceService).when(tenantServiceAccessor).getEventInstanceService();
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

    @Test
    public void should_retrigger_timer_if_work_service_is_stopped() throws Exception {
        SJobDescriptor jobDescriptor = SJobDescriptor.builder().id(jobDescriptorId).build();
        doReturn(jobDescriptor).when(jobService).getJobDescriptor(jobDescriptorId);
        setAttributes();
        doReturn(true).when(workService).isStopped();

        triggerTimerEventJob.execute();

        verify(schedulerService).executeAgain(jobDescriptorId);
    }

    @Test
    public void should_trigger_timer_for_event_sub_process() throws Exception {
        eventType = SBPMEventType.EVENT_SUB_PROCESS.name();
        subProcessId = 31289032198L;
        setAttributes();

        triggerTimerEventJob.execute();

        verify(eventsHandler).triggerCatchEvent(SEventTriggerType.TIMER, processDefinitionId, targetSFlowNodeDefinitionId,
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
        doReturn(Optional.of(timerEventTriggerInstance)).when(eventInstanceService).getTimerEventTriggerInstanceOfFlowNode(flowNodeInstanceId);

        triggerTimerEventJob.execute();

        verify(eventInstanceService).deleteEventTriggerInstance(timerEventTriggerInstance);
    }

}