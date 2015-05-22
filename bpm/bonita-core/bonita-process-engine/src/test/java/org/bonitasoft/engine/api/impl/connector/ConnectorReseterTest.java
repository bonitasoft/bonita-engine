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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.bpm.connector.ConnectorStateReset;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectorReseterTest {

    @Mock
    private ConnectorInstanceService connectorInstanceService;

    private ConnectorReseter connectorReseter;

    @Mock
    private SConnectorInstanceWithFailureInfo connectorInstance;

    @Before
    public void setUp() throws Exception {
        connectorReseter = new ConnectorReseter(connectorInstanceService);
    }

    @Test
    public void resetState_should_change_the_connector_state_and_clean_stack_trace() throws Exception {
        //given
        given(connectorInstance.getStackTrace()).willReturn("A stack trace...");

        //when
        connectorReseter.resetState(connectorInstance, ConnectorStateReset.SKIPPED);

        //then
        verify(connectorInstanceService).setState(connectorInstance, ConnectorStateReset.SKIPPED.name());
        verify(connectorInstanceService).setConnectorInstanceFailureException(connectorInstance, null);
    }

    @Test
    public void resetState_should_not_clean_stack_trace_when_current_stack_trace_is_null() throws Exception {
        //given
        given(connectorInstance.getStackTrace()).willReturn(null);

        //when
        connectorReseter.resetState(connectorInstance, ConnectorStateReset.SKIPPED);

        //then
        verify(connectorInstanceService, never()).setConnectorInstanceFailureException(connectorInstance, null);
    }

    @Test(expected = ActivityExecutionException.class)
    public void resetState_should_throw_ActivityExecutionException_when_connectorInstance_service_throws_exception() throws Exception {
        //given
        doThrow(new SConnectorInstanceModificationException(new Exception())).when(connectorInstanceService).setState(connectorInstance,
                ConnectorStateReset.SKIPPED.name());

        //when
        connectorReseter.resetState(connectorInstance, ConnectorStateReset.SKIPPED);

        //then exception
    }

}
