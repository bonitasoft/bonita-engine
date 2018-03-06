/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.execution.state;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristicsImpl;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SMultiInstanceLoopCharacteristicsImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SMultiInstanceActivityInstanceImpl;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class InitializingMultiInstanceActivityStateImplTest {

    private static final long MULTI_INSTANCE_ACTIVITY_INSTANCE_ID = 789432L;
    private static final long FLOW_NODE_DEFINITION_ID = 765311123L;
    @Mock
    private SProcessDefinition processDefinition;
    @Mock
    private SActivityDefinition activityDefinition;
    private SMultiInstanceActivityInstanceImpl flowNodeInstance = new SMultiInstanceActivityInstanceImpl();
    @Mock
    ExpressionResolverService expressionResolverService;
    @Mock
    ActivityInstanceService activityInstanceService;
    @Mock
    StateBehaviors stateBehaviors;
    @InjectMocks
    private InitializingMultiInstanceActivityStateImpl state;

    @Before
    public void before() throws Exception {
        flowNodeInstance.setId(MULTI_INSTANCE_ACTIVITY_INSTANCE_ID);
        flowNodeInstance.setFlowNodeDefinitionId(FLOW_NODE_DEFINITION_ID);
        SFlowElementContainerDefinition flowElementContainerDefinition = mock(SFlowElementContainerDefinition.class);
        doReturn(flowElementContainerDefinition).when(processDefinition).getProcessContainer();
        doReturn(activityDefinition).when(flowElementContainerDefinition).getFlowNode(FLOW_NODE_DEFINITION_ID);
    }

    @Test
    public void should_not_create_instances_with_empty_list() throws Exception {
        //given
        SMultiInstanceLoopCharacteristicsImpl miLoop = new SMultiInstanceLoopCharacteristicsImpl(new MultiInstanceLoopCharacteristicsImpl(false, "data"));
        doReturn(miLoop).when(activityDefinition)
                .getLoopCharacteristics();
        doReturn(0).when(stateBehaviors).getNumberOfInstancesToCreateFromInputRef(processDefinition, flowNodeInstance, miLoop,
                -1);
        //when
        state.execute(processDefinition, flowNodeInstance);
        //then
        verify(stateBehaviors, never()).createInnerInstances(anyLong(), any(SActivityDefinition.class), any(SMultiInstanceActivityInstance.class), anyInt());
    }

    @Test
    public void should_create_instances_when_list_not_empty() throws Exception {
        //given
        SMultiInstanceLoopCharacteristicsImpl miLoop = new SMultiInstanceLoopCharacteristicsImpl(new MultiInstanceLoopCharacteristicsImpl(false, "data"));
        doReturn(miLoop).when(activityDefinition)
                .getLoopCharacteristics();
        doReturn(1).when(stateBehaviors).getNumberOfInstancesToCreateFromInputRef(processDefinition, flowNodeInstance, miLoop,
                -1);
        //when
        state.execute(processDefinition, flowNodeInstance);
        //then
        verify(stateBehaviors).createInnerInstances(anyLong(), any(SActivityDefinition.class), any(SMultiInstanceActivityInstance.class), anyInt());
    }

}
