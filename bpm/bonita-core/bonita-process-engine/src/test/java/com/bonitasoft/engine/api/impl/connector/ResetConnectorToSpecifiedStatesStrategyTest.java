/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.impl.connector.ConnectorReseter;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.connector.ConnectorStateReset;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResetConnectorToSpecifiedStatesStrategyTest {

    public static final long FLOW_NODE_INSTANCE_ID = 20L;
    @Mock
    private ConnectorInstanceService connectorInstanceService;

    @Mock
    private ConnectorReseter connectorReseter;

    @Test
    public void resetConnectorsOf_should_call_set_state_for_each_element_of_the_map() throws Exception {
        //given
        Map<Long, ConnectorStateReset> connectorsToReset = new HashMap<Long, ConnectorStateReset>();
        connectorsToReset.put(1L, ConnectorStateReset.SKIPPED);
        connectorsToReset.put(2L, ConnectorStateReset.TO_RE_EXECUTE);

        ResetConnectorToSpecifiedStatesStrategy strategy = new ResetConnectorToSpecifiedStatesStrategy(connectorInstanceService, connectorReseter,
                connectorsToReset);

        SConnectorInstanceWithFailureInfo instance1 = mock(SConnectorInstanceWithFailureInfo.class);
        SConnectorInstanceWithFailureInfo instance2 = mock(SConnectorInstanceWithFailureInfo.class);
        given(connectorInstanceService.getConnectorInstanceWithFailureInfo(1)).willReturn(instance1);
        given(connectorInstanceService.getConnectorInstanceWithFailureInfo(2)).willReturn(instance2);

        //when
        strategy.resetConnectorsOf(FLOW_NODE_INSTANCE_ID);

        //then
        verify(connectorReseter).resetState(instance1, ConnectorStateReset.SKIPPED);
        verify(connectorReseter).resetState(instance2, ConnectorStateReset.TO_RE_EXECUTE);
    }

    @Test
    public void resetConnectorsOf_should_throw_Activity_Execution_Exception_when_there_are_remaining_failed_connectors() throws Exception {
        //given
        SConnectorInstanceWithFailureInfo instance1 = mock(SConnectorInstanceWithFailureInfo.class);
        SConnectorInstanceWithFailureInfo instance2 = mock(SConnectorInstanceWithFailureInfo.class);

        ResetConnectorToSpecifiedStatesStrategy strategy = new ResetConnectorToSpecifiedStatesStrategy(connectorInstanceService, connectorReseter,
                Collections.<Long, ConnectorStateReset> emptyMap());

        given(connectorInstanceService.getConnectorInstancesWithFailureInfo(FLOW_NODE_INSTANCE_ID, SConnectorInstance.FLOWNODE_TYPE, ConnectorState.FAILED.name(), 0, 1))
                .willReturn(Arrays.asList(instance1, instance2));

        try {
            //when
            strategy.resetConnectorsOf(FLOW_NODE_INSTANCE_ID);
            fail("Exception expected");
        } catch (ActivityExecutionException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("The flow node instance with id '" + FLOW_NODE_INSTANCE_ID + "' has still failed connectors.");
        }

    }

    @Test(expected = ActivityExecutionException.class)
    public void resetConnectorsOf_should_throw_exception_when_connector_instance_is_not_found() throws Exception {
        //given
        ResetConnectorToSpecifiedStatesStrategy strategy = new ResetConnectorToSpecifiedStatesStrategy(connectorInstanceService, connectorReseter,
                Collections.singletonMap(1L, ConnectorStateReset.SKIPPED));
        given(connectorInstanceService.getConnectorInstanceWithFailureInfo(1)).willThrow(new SConnectorInstanceNotFoundException(""));

        //when
        strategy.resetConnectorsOf(FLOW_NODE_INSTANCE_ID);

        //then exception
    }

}
