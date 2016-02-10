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
package org.bonitasoft.engine.bpm.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class ConnectorInstanceServiceIntegrationTests extends CommonBPMServicesTest {

    private ConnectorInstanceService connectorInstanceService;

    @Before
    public void setUp() {
        connectorInstanceService = getTenantAccessor().getConnectorInstanceService();
    }

    @After
    public void tearDown() throws Exception {
        cleanConnectorInstances();
    }

    private void cleanConnectorInstances() throws Exception {
        getTransactionService().begin();
        final QueryOptions queryOptions = new QueryOptions(0, 100, SConnectorInstance.class, "id", OrderByType.ASC);
        List<SConnectorInstance> connetorInstances = null;
        do {
            connetorInstances = connectorInstanceService.searchConnectorInstances(queryOptions);
            for (final SConnectorInstance connectorInstance : connetorInstances) {
                connectorInstanceService.deleteConnectorInstance(connectorInstance);
            }
        } while (!connetorInstances.isEmpty());
        getTransactionService().complete();
    }

    private SConnectorInstance createConnectorInTransaction(final String name, final long containerId, final String containerType, final String connectorId,
            final String version, final ConnectorEvent activationEvent, final int executionOrder) throws SBonitaException {
        final SConnectorInstance connectorInstance = BuilderFactory.get(SConnectorInstanceBuilderFactory.class)
                .createNewInstance(name, containerId, containerType, connectorId, version, activationEvent, executionOrder).done();
        getTransactionService().begin();
        connectorInstanceService.createConnectorInstance(connectorInstance);
        getTransactionService().complete();
        return connectorInstance;
    }

    private SConnectorInstance getNextInTransaction(final long containerId, final String containerType, final ConnectorEvent activationEvent)
            throws SBonitaException {
        getTransactionService().begin();
        final SConnectorInstance connectorInstance = connectorInstanceService.getNextExecutableConnectorInstance(containerId, containerType,
                activationEvent);
        getTransactionService().complete();
        return connectorInstance;
    }

    private void setStateIntransaction(final long connectorId, final String state) throws SBonitaException {
        getTransactionService().begin();
        final SConnectorInstance connectorInstance = connectorInstanceService.getConnectorInstance(connectorId);
        connectorInstanceService.setState(connectorInstance, state);
        getTransactionService().complete();
    }

    @Test
    public void testGetNextExecutableConnectorInstance() throws Exception {
        final long containerId = 1L;
        final String containerType = SConnectorInstance.FLOWNODE_TYPE;
        final ConnectorEvent activationEvent = ConnectorEvent.ON_ENTER;

        // insert two connector instances
        final SConnectorInstance connectorA = createConnectorInTransaction("a", containerId, containerType, "myConnector-1", "1.0", activationEvent, 1);
        final SConnectorInstance connectorB = createConnectorInTransaction("b", containerId, containerType, "myConnector-2", "1.0", activationEvent, 2);

        // check that the next connector to be executed is the first inserted
        checkNextExecutableConnector(containerId, containerType, activationEvent, "a");

        // mark the first connector as done: the next connector must be the second one
        setStateIntransaction(connectorA.getId(), ConnectorService.DONE);
        checkNextExecutableConnector(containerId, containerType, activationEvent, "b");

        // mark the second connector as done: no more connectors are expected
        setStateIntransaction(connectorB.getId(), ConnectorService.DONE);
        assertNull(getNextInTransaction(containerId, containerType, activationEvent));
    }

    @Test
    public void testGetNextExecutableConnectorInstanceWithFaillingConnectors() throws Exception {
        final long containerId = 1L;
        final String containerType = SConnectorInstance.FLOWNODE_TYPE;
        final ConnectorEvent activationEvent = ConnectorEvent.ON_ENTER;

        // insert two connector instances
        final SConnectorInstance connectorA = createConnectorInTransaction("a", containerId, containerType, "myConnector-1", "1.0", activationEvent, 1);
        createConnectorInTransaction("b", containerId, containerType, "myConnector-2", "1.0", activationEvent, 2);

        // check that the next connector to be executed is the first inserted
        checkNextExecutableConnector(containerId, containerType, activationEvent, "a");

        // put the first connector in the state to re execute: the next connector must remain the first connector
        setStateIntransaction(connectorA.getId(), ConnectorService.TO_RE_EXECUTE);
        checkNextExecutableConnector(containerId, containerType, activationEvent, "a");
    }

    private void checkNextExecutableConnector(final long containerId, final String containerType, final ConnectorEvent activationEvent,
            final String connectorName) throws SBonitaException {
        final SConnectorInstance nextConnectorInstance = getNextInTransaction(containerId, containerType, activationEvent);
        assertEquals(connectorName, nextConnectorInstance.getName());
    }

}
