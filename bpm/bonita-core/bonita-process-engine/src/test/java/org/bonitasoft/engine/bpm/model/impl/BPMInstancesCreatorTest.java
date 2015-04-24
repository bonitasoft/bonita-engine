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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.operation.model.impl.SOperationImpl;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BuilderFactory.class)
public class BPMInstancesCreatorTest {

    @Mock
    private TransactionExecutor transactionExecutor;

    @Mock
    private ConnectorInstanceService connectorInstanceService;

    @Mock
    private SConnectorInstanceBuilderFactory connectorBuilderFact;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(BuilderFactory.class);

        connectorBuilderFact = mock(SConnectorInstanceBuilderFactory.class);
        Mockito.when(BuilderFactory.get(SConnectorInstanceBuilderFactory.class)).thenReturn(connectorBuilderFact);
    }

    @Test
    public void testExecutionOrder() throws Exception {
        final BPMInstancesCreator bpmInstancesCreator = new BPMInstancesCreator(null, null, null, null, connectorInstanceService, null,
                null, null, null, null, null);
        final SConnectorInstance connectorInstance = mock(SConnectorInstance.class);
        final SConnectorInstanceBuilder connectorBuilder = mock(SConnectorInstanceBuilder.class);
        when(connectorBuilderFact.createNewInstance(anyString(), anyLong(), anyString(), anyString(), anyString(), any(ConnectorEvent.class), anyInt()))
                .thenReturn(connectorBuilder);
        when(connectorBuilder.done()).thenReturn(connectorInstance);

        final PersistentObject container = mock(PersistentObject.class);
        final List<SConnectorDefinition> connectors = getConnectorList();

        bpmInstancesCreator.createConnectorInstances(container, connectors, SConnectorInstance.FLOWNODE_TYPE);
        verify(connectorBuilderFact, times(1)).createNewInstance(anyString(), anyLong(), anyString(), anyString(), anyString(), any(ConnectorEvent.class),
                eq(0));
        verify(connectorBuilderFact, times(1)).createNewInstance(anyString(), anyLong(), anyString(), anyString(), anyString(), any(ConnectorEvent.class),
                eq(1));
        ignoreStubs(transactionExecutor);
        ignoreStubs(connectorInstanceService);
    }

    @Test
    public void should_getOperationToSetData_return_the_operation_for_the_data() {
        // given
        final BPMInstancesCreator bpmInstancesCreator = new BPMInstancesCreator(null, null, null, null, null, null,
                null, null, null, null, null);
        SLeftOperandImpl leftOp1 = new SLeftOperandImpl();
        leftOp1.setName(new String("Plop1"));
        leftOp1.setType(new String(SLeftOperand.TYPE_DATA));
        SOperationImpl op1 = new SOperationImpl();
        op1.setLeftOperand(leftOp1);
        op1.setType(SOperatorType.ASSIGNMENT);
        SLeftOperandImpl leftOp2 = new SLeftOperandImpl();
        leftOp2.setName(new String("Plop2"));
        leftOp2.setType(new String(SLeftOperand.TYPE_DATA));
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
        final List<SConnectorDefinition> connectors = new ArrayList<SConnectorDefinition>(2);
        connectors.add(connector1);
        connectors.add(connector2);
        return connectors;
    }
}
