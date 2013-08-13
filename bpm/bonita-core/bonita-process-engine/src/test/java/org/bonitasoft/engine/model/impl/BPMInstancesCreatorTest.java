/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.model.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Elias Ricken de Medeiros
 */
public class BPMInstancesCreatorTest {

    @Mock
    private TransactionExecutor transactionExecutor;

    @Mock
    private BPMInstanceBuilders instanceBuilders;

    @Mock
    private ConnectorInstanceService connectorInstanceService;

    @Mock
    private SConnectorInstanceBuilder connectorBuilder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecutionOrder() throws Exception {
        final BPMInstancesCreator bpmInstancesCreator = new BPMInstancesCreator(null, instanceBuilders, null, null, null, connectorInstanceService, null, null,
                null, null);
        final SConnectorInstance connectorInstance = mock(SConnectorInstance.class);
        when(instanceBuilders.getSConnectorInstanceBuilder()).thenReturn(connectorBuilder);
        when(connectorBuilder.createNewInstance(anyString(), anyLong(), anyString(), anyString(), anyString(), any(ConnectorEvent.class), anyInt()))
                .thenReturn(connectorBuilder);
        when(connectorBuilder.done()).thenReturn(connectorInstance);

        final PersistentObject container = mock(PersistentObject.class);
        final List<SConnectorDefinition> connectors = getConnectorList();

        bpmInstancesCreator.createConnectorInstances(container, connectors, SConnectorInstance.FLOWNODE_TYPE);
        verify(connectorBuilder, times(1)).createNewInstance(anyString(), anyLong(), anyString(), anyString(), anyString(), any(ConnectorEvent.class), eq(0));
        verify(connectorBuilder, times(1)).createNewInstance(anyString(), anyLong(), anyString(), anyString(), anyString(), any(ConnectorEvent.class), eq(1));
        ignoreStubs(transactionExecutor);
        ignoreStubs(connectorInstanceService);
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
