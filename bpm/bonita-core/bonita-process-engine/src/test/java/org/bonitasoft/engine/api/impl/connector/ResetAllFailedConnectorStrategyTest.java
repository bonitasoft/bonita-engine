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

package org.bonitasoft.engine.api.impl.connector;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.connector.ConnectorStateReset;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResetAllFailedConnectorStrategyTest {

    public static final int MAX_RESULTS = 2;
    @Mock
    private ConnectorInstanceService connectorInstanceService;

    @Mock
    private ConnectorReseter connectorReseter;

    private ResetAllFailedConnectorStrategy strategy;

    @Before
    public void setUp() throws Exception {
        strategy = new ResetAllFailedConnectorStrategy(connectorInstanceService, connectorReseter, MAX_RESULTS);
    }

    @Test
    public void resetConnectorsOf_should_put_all_failed_connectors_in_the_state_TO_RE_EXECUTE() throws Exception {
        //given
        SConnectorInstanceWithFailureInfo instance1 = mock(SConnectorInstanceWithFailureInfo.class);
        SConnectorInstanceWithFailureInfo instance2 = mock(SConnectorInstanceWithFailureInfo.class);
        SConnectorInstanceWithFailureInfo instance3 = mock(SConnectorInstanceWithFailureInfo.class);
        given(
                connectorInstanceService.getConnectorInstancesWithFailureInfo(20L, SConnectorInstance.FLOWNODE_TYPE, ConnectorState.FAILED.name(), 0,
                        MAX_RESULTS)).willReturn(Arrays.asList(instance1, instance2));
        given(
                connectorInstanceService.getConnectorInstancesWithFailureInfo(20L, SConnectorInstance.FLOWNODE_TYPE, ConnectorState.FAILED.name(), MAX_RESULTS,
                        MAX_RESULTS)).willReturn(Arrays.asList(instance3));

        //when
        strategy.resetConnectorsOf(20L);

        //then
        verify(connectorReseter).resetState(instance1, ConnectorStateReset.TO_RE_EXECUTE);
        verify(connectorReseter).resetState(instance2, ConnectorStateReset.TO_RE_EXECUTE);
        verify(connectorReseter).resetState(instance3, ConnectorStateReset.TO_RE_EXECUTE);
    }

    @Test(expected = ActivityExecutionException.class)
    public void resetConnectorsOf_should_throw_ActivityExecution_Exception_when_connectorService_throws_exception() throws Exception {
        //given
        given(
                connectorInstanceService.getConnectorInstancesWithFailureInfo(20L, SConnectorInstance.FLOWNODE_TYPE, ConnectorState.FAILED.name(), 0,
                        MAX_RESULTS)).willThrow(new SConnectorInstanceReadException(new Exception()));

        //when
        strategy.resetConnectorsOf(20L);

        //then exception
    }

}
