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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.bonitasoft.engine.persistence.PersistentObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest(BuilderFactory.class)
public class BPMInstancesCreatorTest {

    @Mock
    private TransactionExecutor transactionExecutor;

    @Mock
    private ConnectorInstanceService connectorInstanceService;

    @Spy
    @InjectMocks
    private BPMInstancesCreator bpmInstancesCreator;


    @Before
    public void setUp() {

    }

    @Test
    public void testExecutionOrder() throws Exception {
        final PersistentObject container = mock(PersistentObject.class);
        final List<SConnectorDefinition> connectors = getConnectorList();

        bpmInstancesCreator.createConnectorInstances(container, connectors, SConnectorInstance.FLOWNODE_TYPE);

        verify(bpmInstancesCreator).createConnectorInstanceObject(any(PersistentObject.class), anyString(), any(SConnectorDefinition.class),eq(0));
        verify(bpmInstancesCreator).createConnectorInstanceObject(any(PersistentObject.class), anyString(), any(SConnectorDefinition.class),eq(1));
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
        final List<SConnectorDefinition> connectors = new ArrayList<SConnectorDefinition>(2);
        connectors.add(connector1);
        connectors.add(connector2);
        return connectors;
    }
}
