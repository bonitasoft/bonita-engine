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
package org.bonitasoft.engine.execution.event;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SBoundaryEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCatchErrorEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SStandardLoopCharacteristicsImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SUserTaskDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEndEventInstance;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.ProcessInstanceInterruptor;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ErrorEventHandlerStrategyTest {

    @Mock
    private EventInstanceService eventInstanceService;

    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;

    @Mock
    private ContainerRegistry containerRegistry;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private EventsHandler eventsHandler;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private ProcessInstanceInterruptor processInstanceInterruptor;

    private ErrorEventHandlerStrategy spyStrategy;

    @Before
    public void setUp() {
        spyStrategy = spy(new ErrorEventHandlerStrategy(eventInstanceService, processInstanceService,
                flowNodeInstanceService,
                processDefinitionService, eventsHandler, processInstanceInterruptor));
    }

    @Test
    public void getWaitingErrorEventFromBoundary_should_find_matching_event_in_multiple_instance_activity()
            throws Exception {
        final long processDefinitionId = 7845475L;
        final long flowNodeDefinitionId = 357L;
        final long rootContainerId = 951L;
        final long parentContainerId = 7887L;
        final long logicalGroup2 = 78454646L;
        final long parentActivityInstanceId = 7897L;
        final String errorCode = "mistake";
        final SProcessDefinition definition = buildProcessDefinitionWithAMultipleInstanceUserTask(flowNodeDefinitionId,
                errorCode);
        final SMultiInstanceActivityInstance multiInstanceInstance = buildMultiInstanceActivity(processDefinitionId,
                flowNodeDefinitionId, rootContainerId,
                parentContainerId);
        final SEndEventInstance eventInstance = buildEventInstance(processDefinitionId, flowNodeDefinitionId,
                rootContainerId, parentContainerId,
                logicalGroup2);
        final SUserTaskInstance instance = buildSUserTaskInstance(processDefinitionId, flowNodeDefinitionId,
                rootContainerId, parentContainerId,
                logicalGroup2, parentActivityInstanceId);
        when(processDefinitionService.getProcessDefinition(processDefinitionId)).thenReturn(definition);
        when(flowNodeInstanceService.getFlowNodeInstance(parentActivityInstanceId)).thenReturn(multiInstanceInstance);

        spyStrategy.getWaitingErrorEventFromBoundary(eventInstance, errorCode, instance);

        verify(spyStrategy).getWaitingErrorEventFromBoundary(eq(errorCode), eq(multiInstanceInstance), anyList());
    }

    private SMultiInstanceActivityInstance buildMultiInstanceActivity(final long processDefinitionId,
            final long flowNodeDefinitionId,
            final long rootContainerId,
            final long parentContainerId) {
        final SMultiInstanceActivityInstance multiInstanceInstance = new SMultiInstanceActivityInstance("MIActivity",
                flowNodeDefinitionId,
                rootContainerId, parentContainerId, processDefinitionId, rootContainerId, false);
        return multiInstanceInstance;
    }

    private SUserTaskInstance buildSUserTaskInstance(final long processDefinitionId, final long flowNodeDefinitionId,
            final long rootContainerId,
            final long parentContainerId, final long logicalGroup2, final long parentActivityInstanceId) {
        final SUserTaskInstance instance = new SUserTaskInstance("usertTask", flowNodeDefinitionId, rootContainerId,
                parentContainerId, 457L,
                STaskPriority.NORMAL, processDefinitionId, logicalGroup2);
        instance.setLogicalGroup(2, parentActivityInstanceId);
        return instance;
    }

    private SEndEventInstance buildEventInstance(final long processDefinitionId, final long flowNodeDefinitionId,
            final long rootContainerId,
            final long parentContainerId, final long logicalGroup2) {
        final SEndEventInstance eventInstance = new SEndEventInstance("ErrorEvent", flowNodeDefinitionId,
                rootContainerId, parentContainerId,
                processDefinitionId, logicalGroup2);
        eventInstance.setLogicalGroup(2, 9L);
        return eventInstance;
    }

    private SProcessDefinition buildProcessDefinitionWithAMultipleInstanceUserTask(final long flowNodeDefinitionId,
            final String errorCode) {
        final SProcessDefinitionImpl definition = new SProcessDefinitionImpl("test", "2.0");
        final SStandardLoopCharacteristicsImpl loopCharacteristics = new SStandardLoopCharacteristicsImpl(null, true);
        final SUserTaskDefinitionImpl taskDefinition = new SUserTaskDefinitionImpl(flowNodeDefinitionId, "usertTask",
                "employee");
        taskDefinition.setLoopCharacteristics(loopCharacteristics);
        final SBoundaryEventDefinitionImpl boundaryEventDefinition = new SBoundaryEventDefinitionImpl(786768768L,
                "error");
        final SCatchErrorEventTriggerDefinitionImpl errorEventTrigger = new SCatchErrorEventTriggerDefinitionImpl(
                errorCode);
        boundaryEventDefinition.addErrorEventTrigger(errorEventTrigger);
        taskDefinition.addBoundaryEventDefinition(boundaryEventDefinition);
        final SFlowElementContainerDefinitionImpl processContainer = new SFlowElementContainerDefinitionImpl();
        processContainer.addActivity(taskDefinition);
        definition.setProcessContainer(processContainer);
        return definition;
    }

}
