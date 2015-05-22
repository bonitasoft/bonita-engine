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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.ConnectorInstanceBuilder.aConnectorInstance;

import java.util.List;

import javax.inject.Inject;

import org.assertj.core.api.Condition;
import org.assertj.core.util.Lists;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.core.process.instance.model.impl.SConnectorInstanceImpl;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.bonitasoft.engine.test.persistence.repository.ConnectorInstanceRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Julien Reboul
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ConnectorInstanceQueriesTest {

    @Inject
    private ConnectorInstanceRepository repository;
    private SConnectorInstanceImpl expectedConnector1;
    private SConnectorInstanceImpl expectedConnector2;
    private String containerType;
    private long containerId;

    private SConnectorInstanceImpl expectedConnector3;

    private SConnectorInstanceImpl expectedConnector4;

    private SConnectorInstanceImpl expectedConnector5;

    private SConnectorInstance connectorInstanceOfDifferentContainer;

    private SConnectorInstanceImpl expectedConnector6;

    /**
     * 
     */
    @Before
    public void setUp() {
        containerType = "Pouet";
        containerId = 1L;
        long differentContainerId = 5L;
        long tenantId = 100L;
        expectedConnector1 = aConnectorInstance().setContainerId(containerId).setContainerType(containerType).setActivationEvent(ConnectorEvent.ON_FINISH)
                .withFailureInfo(false).setState(ConnectorState.EXECUTING.toString()).build();
        expectedConnector2 = aConnectorInstance().setContainerId(containerId).setContainerType(containerType).setActivationEvent(ConnectorEvent.ON_ENTER)
                .setState(ConnectorState.EXECUTING.toString()).withFailureInfo(false)
                .build();
        expectedConnector3 = aConnectorInstance().setContainerId(containerId).setContainerType(containerType).setActivationEvent(ConnectorEvent.ON_ENTER)
                .setState(ConnectorState.DONE.toString()).withFailureInfo(false)
                .build();
        expectedConnector4 = aConnectorInstance().setContainerId(containerId).setContainerType(containerType).setActivationEvent(ConnectorEvent.ON_ENTER)
                .setState(ConnectorState.TO_BE_EXECUTED.toString()).withFailureInfo(false)
                .build();
        expectedConnector5 = aConnectorInstance().setContainerId(containerId).setContainerType(containerType).setActivationEvent(ConnectorEvent.ON_ENTER)
                .setState(ConnectorState.FAILED.toString()).withFailureInfo(false)
                .build();
        expectedConnector6 = aConnectorInstance().setContainerId(containerId).setContainerType(containerType)
                .setActivationEvent(ConnectorEvent.ON_FINISH)
                .setState(ConnectorState.FAILED.toString()).withFailureInfo(false)
                .build();
        expectedConnector1.setId(10L);
        expectedConnector2.setId(2L);
        expectedConnector3.setId(7L);
        expectedConnector4.setId(160L);
        expectedConnector5.setId(1L);
        expectedConnector6.setId(6L);
        repository.add(expectedConnector1);
        repository.add(expectedConnector2);
        repository.add(expectedConnector3);
        repository.add(expectedConnector4);
        repository.add(expectedConnector5);
        repository.add(expectedConnector6);
        connectorInstanceOfDifferentContainer = repository.add(aConnectorInstance().setContainerId(differentContainerId).setContainerType(containerType)
                .setActivationEvent(ConnectorEvent.ON_ENTER)
                .withFailureInfo(false).build());// unexpected connector on different container
        SConnectorInstanceImpl differentTenantConnector = aConnectorInstance().setContainerId(containerId).setContainerType(containerType)
                .setActivationEvent(ConnectorEvent.ON_FINISH)
                .withFailureInfo(false).build();
        differentTenantConnector.setTenantId(tenantId);
        differentTenantConnector.setId(differentContainerId);
        repository.add(differentTenantConnector);// unexpected connector on different tenant
    }

    @Test
    public void getConnectorInstances() {
        List<SConnectorInstance> connectors = repository.getConnectorInstances(containerId, containerType, PersistentObjectBuilder.DEFAULT_TENANT_ID);

        assertThat(connectors).containsOnly(expectedConnector1, expectedConnector2, expectedConnector6, expectedConnector3, expectedConnector4,
                expectedConnector5);
    }

    @Test
    public void getConnectorInstancesOrderedById() {

        List<SConnectorInstance> connectors = repository
                .getConnectorInstancesOrderedById(containerId, containerType, PersistentObjectBuilder.DEFAULT_TENANT_ID);

        assertThat(connectors).containsExactly(expectedConnector5, expectedConnector2, expectedConnector6, expectedConnector3, expectedConnector1,
                expectedConnector4);
    }

    @Test
    public void getNumberOfConnectorInstances() {
        long nbOfConnectors = repository
                .getNumberOfConnectorInstances(containerId, containerType, PersistentObjectBuilder.DEFAULT_TENANT_ID);
        assertThat(nbOfConnectors).isEqualTo(6);
    }

    @Test
    public void getNextExecutableConnectorInstance() {
        SConnectorInstance connectors = repository
                .getNextExecutableConnectorInstance(containerId, containerType, ConnectorEvent.ON_ENTER, PersistentObjectBuilder.DEFAULT_TENANT_ID);

        assertThat(connectors).isSameAs(expectedConnector2);
    }

    @Test
    public void searchSConnectorInstance() {
        List<SConnectorInstance> connectors = repository
                .searchSConnectorInstance(containerId, containerType, PersistentObjectBuilder.DEFAULT_TENANT_ID);

        assertThat(connectors).containsOnly(expectedConnector1, expectedConnector2, expectedConnector3, expectedConnector4, expectedConnector5,
                expectedConnector6, connectorInstanceOfDifferentContainer);
    }

    @Test
    public void getNumberOfSConnectorInstance() {
        long nbOfConnectors = repository
                .getNumberOfSConnectorInstance(containerId, containerType, PersistentObjectBuilder.DEFAULT_TENANT_ID);

        assertThat(nbOfConnectors).isEqualTo(7);
    }

    @Test
    public void getConnectorInstancesWithState() {
        List<SConnectorInstance> connectors = repository
                .getConnectorInstances(containerId, containerType, ConnectorEvent.ON_ENTER, ConnectorState.TO_BE_EXECUTED.toString(),
                        PersistentObjectBuilder.DEFAULT_TENANT_ID);

        assertThat(connectors).containsOnly(expectedConnector4);
    }

    @Test
    public void getConnectorInstanceWithFailureInfo() {
        List<SConnectorInstanceWithFailureInfo> connectors = repository
                .getConnectorInstanceWithFailureInfo(containerId, PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final List<Long> idsToFind = Lists.newArrayList(expectedConnector1.getId(), expectedConnector2.getId(), expectedConnector3.getId(),
                expectedConnector4.getId(), expectedConnector5.getId(), expectedConnector6.getId());
        assertThat(connectors).areExactly(6, new Condition<SConnectorInstanceWithFailureInfo>() {

            @Override
            public boolean matches(SConnectorInstanceWithFailureInfo connector) {
                return idsToFind.remove(connector.getId());
            }
        });
        assertThat(idsToFind).isEmpty();
    }

    @Test
    public void getConnectorInstancesWithFailureInState() {
        List<SConnectorInstanceWithFailureInfo> connectors = repository
                .getConnectorInstancesWithFailureInfo(containerId, containerType, ConnectorState.FAILED.toString(),
                        PersistentObjectBuilder.DEFAULT_TENANT_ID);

        final List<Long> idsToFind = Lists.newArrayList(expectedConnector5.getId(), expectedConnector6.getId());
        assertThat(connectors).hasSize(2);
        assertThat(idsToFind).contains(connectors.get(0).getId());
        assertThat(idsToFind).contains(connectors.get(1).getId());
    }

}
