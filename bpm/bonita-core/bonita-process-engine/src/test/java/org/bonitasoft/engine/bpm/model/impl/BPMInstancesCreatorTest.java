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
package org.bonitasoft.engine.bpm.model.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.core.process.definition.model.SFlowNodeType.USER_TASK;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.operation.model.impl.SOperationImpl;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.definition.model.SHumanTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SGatewayDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.state.InitializingActivityStateImpl;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMInstancesCreatorTest {

    @Mock
    private ConnectorInstanceService connectorInstanceService;

    @Mock
    private GatewayInstanceService gatewayInstanceService;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private FlowNodeStateManager flowNodeStateManager;

    @Mock
    ActorMappingService actorMappingService;

    @Spy
    @InjectMocks
    private BPMInstancesCreator bpmInstancesCreator;

    @Mock
    private SHumanTaskDefinition sHumanTaskDefinition;

    @Mock
    private SActor actor;

    @Before
    public void setupClass() {
        bpmInstancesCreator.setStateManager(flowNodeStateManager);
    }

    @Test
    public void testExecutionOrder() throws Exception {
        final PersistentObject container = mock(PersistentObject.class);
        final List<SConnectorDefinition> connectors = getConnectorList();

        bpmInstancesCreator.createConnectorInstances(container, connectors, SConnectorInstance.FLOWNODE_TYPE);

        verify(bpmInstancesCreator).createConnectorInstanceObject(any(PersistentObject.class), anyString(), any(SConnectorDefinition.class), eq(0));
        verify(bpmInstancesCreator).createConnectorInstanceObject(any(PersistentObject.class), anyString(), any(SConnectorDefinition.class), eq(1));
    }

    @Test
    public void should_getOperationToSetData_return_the_operation_for_the_data() {
        // given
        SLeftOperandImpl leftOp1 = new SLeftOperandImpl();
        leftOp1.setName("Plop1");
        leftOp1.setType(SLeftOperand.TYPE_DATA);
        SOperationImpl op1 = new SOperationImpl();
        op1.setLeftOperand(leftOp1);
        op1.setType(SOperatorType.ASSIGNMENT);
        SLeftOperandImpl leftOp2 = new SLeftOperandImpl();
        leftOp2.setName("Plop2");
        leftOp2.setType(SLeftOperand.TYPE_DATA);
        SOperationImpl op2 = new SOperationImpl();
        op2.setType(SOperatorType.ASSIGNMENT);
        op2.setLeftOperand(leftOp2);
        // when
        SOperation operationToSetData = bpmInstancesCreator.getOperationToSetData("Plop2", Arrays.<SOperation> asList(op1, op2));

        // then
        assertThat(operationToSetData).isEqualTo(op2);
    }

    private List<SConnectorDefinition> getConnectorList() {
        final SConnectorDefinition connector1 = mock(SConnectorDefinition.class);
        final SConnectorDefinition connector2 = mock(SConnectorDefinition.class);
        final List<SConnectorDefinition> connectors = new ArrayList<>(2);
        connectors.add(connector1);
        connectors.add(connector2);
        return connectors;
    }

    @Test
    public void createManualTaskInstanceShouldSetReachedStateDateAndLastUpdateDate() throws Exception {
        doReturn(mock(SManualTaskInstance.class)).when(activityInstanceService).getFlowNodeInstance(anyLong());
        doReturn(new InitializingActivityStateImpl(null)).when(flowNodeStateManager).getFirstState(SFlowNodeType.MANUAL_TASK);

        final SManualTaskInstance manualTaskInstance = bpmInstancesCreator.createManualTaskInstance(2345L, "task", 964854854L, "disp", 78L, "desc",
                987968744446L, STaskPriority.HIGHEST);

        assertThat(manualTaskInstance.getReachedStateDate()).isNotEqualTo(0L);
        assertThat(manualTaskInstance.getLastUpdateDate()).isNotEqualTo(0L);
    }

    @Test
    public void createFlownodeInstanceShouldSetReachedStateDateAndLastUpdateDate() throws Exception {
        doReturn(new InitializingActivityStateImpl(null)).when(flowNodeStateManager).getFirstState(SFlowNodeType.GATEWAY);

        final SGatewayInstance gatewayInstance = (SGatewayInstance) bpmInstancesCreator.createFlowNodeInstance(2345L, 999L, 888L,
                SFlowElementsContainerType.FLOWNODE, new SGatewayDefinitionImpl(222L, "myGate", SGatewayType.EXCLUSIVE), 964854854L, 78L, true, 0,
                SStateCategory.NORMAL, 987968744446L);

        assertThat(gatewayInstance.getReachedStateDate()).isNotEqualTo(0L);
        assertThat(gatewayInstance.getLastUpdateDate()).isNotEqualTo(0L);
    }

    @Test
    public void create_user_task_should_not_evaluate_setExpectedEndDate_expression() throws Exception {
        //given
        doReturn(new InitializingActivityStateImpl(null)).when(flowNodeStateManager).getFirstState(SFlowNodeType.USER_TASK);
        doReturn(USER_TASK).when(sHumanTaskDefinition).getType();
        doReturn("actor").when(sHumanTaskDefinition).getActorName();
        doReturn(actor).when(actorMappingService).getActor(anyString(), anyLong());

        ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        expressionBuilder.createConstantLongExpression(3600L);

        //when
        final SHumanTaskInstance sHumanTaskInstance = (SHumanTaskInstance) bpmInstancesCreator.createFlowNodeInstance(2345L, 456L, 987L,
                SFlowElementsContainerType.FLOWNODE,
                sHumanTaskDefinition, 964854854L, 547, false, 0, SStateCategory.NORMAL, 0);

        //then
        assertThat(sHumanTaskInstance.getExpectedEndDate()).as("should not compute end date on creation").isNull();

    }
}
