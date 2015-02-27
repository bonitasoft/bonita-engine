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
package org.bonitasoft.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bpm.connector.ConnectorState.EXECUTING;
import static org.bonitasoft.engine.bpm.connector.ConnectorState.TO_BE_EXECUTED;
import static org.bonitasoft.engine.execution.StateBehaviors.AFTER_ON_FINISH;
import static org.bonitasoft.engine.execution.StateBehaviors.BEFORE_ON_ENTER;
import static org.bonitasoft.engine.execution.StateBehaviors.BEFORE_ON_FINISH;
import static org.bonitasoft.engine.execution.StateBehaviors.DURING_ON_ENTER;
import static org.bonitasoft.engine.execution.StateBehaviors.DURING_ON_FINISH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.filter.FilterResult;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.filter.exception.SUserFilterExecutionException;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SConnectorDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.expression.model.SExpression;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Phase order is: BEFORE_ON_ENTER > DURING_ON_ENTER > BEFORE_ON_FINISH > DURING_ON_FINISH > AFTER_ON_FINISH.
 *
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class StateBehaviorsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private SFlowNodeInstance flowNodeInstance;
    @Mock
    private SProcessDefinition processDefinition;
    @Mock
    private SFlowElementContainerDefinition containerDefinition;
    @Mock
    private SUserTaskDefinition flowNodeDefinition;
    @Mock
    private ConnectorInstanceService connectorInstanceService;
    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private UserFilterService userFilterService;
    @Mock
    private SUserFilterDefinition userFilterDefinition;
    @Mock
    private ClassLoaderService classLoaderService;
    @InjectMocks
    private StateBehaviors behaviors;

    final long flownodeInstanceId = 3541L;
    final long processDefinitionId = 4567L;

    private final static Map<Integer, String> phaseNames = new HashMap<Integer, String>(5);
    static {
        phaseNames.put(1, "BEFORE_ON_ENTER");
        phaseNames.put(2, "DURING_ON_ENTER");
        phaseNames.put(4, "BEFORE_ON_FINISH");
        phaseNames.put(5, "DURING_ON_FINISH");
        phaseNames.put(16, "AFTER_ON_FINISH");
    }

    private static final List<Integer> KNOWN_PHASES = Arrays.asList(BEFORE_ON_ENTER, // 1
            DURING_ON_ENTER, // 2
            BEFORE_ON_FINISH, // 4
            DURING_ON_FINISH, // 8
            AFTER_ON_FINISH); // 16

    @Before
    public void setConstants() {
        when(processDefinition.getProcessContainer()).thenReturn(containerDefinition);
        when(containerDefinition.getFlowNode(anyLong())).thenReturn(flowNodeDefinition);
        when(flowNodeInstance.getId()).thenReturn(flownodeInstanceId);
        when(processDefinition.getId()).thenReturn(processDefinitionId);
    }

    protected void checkConnectorsPhases(final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag,
            final Integer... expectedPhases) {
        final List<Integer> expectedPhaseList = Arrays.asList(expectedPhases);
        for (final Integer expectedPhase : KNOWN_PHASES) {
            if (expectedPhaseList.contains(expectedPhase)) {
                assertTrue("Phase " + getPhaseName(expectedPhase) + " should be present", isPhaseIncluded(connectorAndFlag.getKey(), expectedPhase));
            } else {
                assertFalse("Phase " + getPhaseName(expectedPhase) + " should NOT be present", isPhaseIncluded(connectorAndFlag.getKey(), expectedPhase));
            }
        }
    }

    private String getPhaseName(final Integer expectedPhase) {
        return phaseNames.get(expectedPhase);
    }

    protected void checkNoConnectorsAndAllDefaultPhases(final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag) {
        checkConnectorsPhases(connectorAndFlag, BEFORE_ON_ENTER, BEFORE_ON_FINISH, AFTER_ON_FINISH);
        assertThat(connectorAndFlag.getValue().getKey()).isEqualTo(null);
        assertThat(connectorAndFlag.getValue().getValue()).isEqualTo(null);
    }

    private boolean isPhaseIncluded(final int actualPhases, final int expectedPhase) {
        return (actualPhases & expectedPhase) != 0;
    }

    @Test
    public void nullFlowNodeDefinitionShouldReturnNoConnectorsAndAllPhases() throws Exception {
        // given:
        when(containerDefinition.getFlowNode(anyLong())).thenReturn(null);
        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, true);
        // then:
        checkNoConnectorsAndAllDefaultPhases(connectorAndFlag);
    }

    @Test
    public void flowNodeWithNoConnectorsShouldReturnNoConnectorsAndAllPhases() throws Exception {
        // given:
        when(flowNodeDefinition.getConnectors(ConnectorEvent.ON_ENTER)).thenReturn(Collections.<SConnectorDefinition> emptyList());
        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, true);
        checkNoConnectorsAndAllDefaultPhases(connectorAndFlag);
    }

    @Test
    public void flowNodeWithNoConnectorInstanceToExecuteShouldReturnNoConnectorsAndAllPhases() throws Exception {
        // given:
        final SConnectorDefinition sConnectorDefinitionImpl = new SConnectorDefinitionImpl("name", "connectorId", "version", ConnectorEvent.ON_ENTER);
        final List<SConnectorDefinition> connectors = Arrays.asList(sConnectorDefinitionImpl);
        when(flowNodeDefinition.getConnectors(ConnectorEvent.ON_ENTER)).thenReturn(connectors);
        when(connectorInstanceService.getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER))).thenReturn(null);
        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, true);
        // then:
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER));
        checkNoConnectorsAndAllDefaultPhases(connectorAndFlag);
    }

    @Test(expected = SActivityStateExecutionException.class)
    public void flowNodeWithNoConnectorInstanceOnEnterInStateTO_BE_EXECUTEDShouldThrowException() throws Exception {
        // given:
        final SConnectorDefinition sConnectorDefinitionImpl = new SConnectorDefinitionImpl("some_name", "connectorId", "version", ConnectorEvent.ON_ENTER);
        final List<SConnectorDefinition> connectors = Arrays.asList(sConnectorDefinitionImpl);
        when(flowNodeDefinition.getConnectors(ConnectorEvent.ON_ENTER)).thenReturn(connectors);
        final SConnectorInstance nextConnectorInstanceToExecute = mock(SConnectorInstance.class);
        when(nextConnectorInstanceToExecute.getState()).thenReturn("dummyState");
        when(nextConnectorInstanceToExecute.getName()).thenReturn("no_matching_name");
        when(connectorInstanceService.getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER))).thenReturn(
                nextConnectorInstanceToExecute);

        // when:
        try {
            behaviors.getConnectorToExecuteAndFlag(processDefinition, flowNodeInstance, true, true);
        } finally {
            // then:
            verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER));
        }
    }

    @Test
    public void firstConnectorNotYetInExecutingStateShouldReturnFirst2Phases() throws Exception {
        // given:
        final String connectorName = "connector_name";
        final SConnectorDefinition sConnectorDefinition = new SConnectorDefinitionImpl(connectorName, "connectorId", "version", ConnectorEvent.ON_ENTER);
        final List<SConnectorDefinition> connectors = Arrays.asList(sConnectorDefinition);
        when(flowNodeDefinition.getConnectors(ConnectorEvent.ON_ENTER)).thenReturn(connectors);
        final SConnectorInstance nextConnectorInstanceToExecute = mock(SConnectorInstance.class);
        when(connectorInstanceService.getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER))).thenReturn(
                nextConnectorInstanceToExecute);
        when(nextConnectorInstanceToExecute.getState()).thenReturn(TO_BE_EXECUTED.name());
        when(nextConnectorInstanceToExecute.getName()).thenReturn(connectorName);

        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, true);

        // then:
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER));
        checkConnectorsPhases(connectorAndFlag, BEFORE_ON_ENTER, DURING_ON_ENTER);
        assertThat(connectorAndFlag.getValue().getKey()).isEqualTo(nextConnectorInstanceToExecute);
        assertThat(connectorAndFlag.getValue().getValue()).isEqualTo(sConnectorDefinition);
    }

    @Test
    public void firstConnectorAlreadyExecutingShouldReturnDURING_ON_ENTERonly() throws Exception {
        // given:
        final String connectorName = "connector_name";
        final SConnectorDefinition sConnectorDefinition = new SConnectorDefinitionImpl(connectorName, "connectorId", "version", ConnectorEvent.ON_ENTER);
        final List<SConnectorDefinition> connectors = Arrays.asList(sConnectorDefinition);
        when(flowNodeDefinition.getConnectors(ConnectorEvent.ON_ENTER)).thenReturn(connectors);
        final SConnectorInstance nextConnectorInstanceToExecute = mock(SConnectorInstance.class);
        when(nextConnectorInstanceToExecute.getState()).thenReturn(EXECUTING.name());
        when(nextConnectorInstanceToExecute.getName()).thenReturn(connectorName);
        when(connectorInstanceService.getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER))).thenReturn(
                nextConnectorInstanceToExecute);

        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, true);

        // then:
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER));
        checkConnectorsPhases(connectorAndFlag, DURING_ON_ENTER);
        assertThat(connectorAndFlag.getValue().getKey()).isEqualTo(nextConnectorInstanceToExecute);
        assertThat(connectorAndFlag.getValue().getValue()).isEqualTo(sConnectorDefinition);
    }

    @Test
    public void secondConnectorOnExecutingFlownodeShouldReturnPhase_2_only() throws Exception {
        // given:
        final String connectorName = "connector_name";
        final SConnectorDefinition firstConnector = new SConnectorDefinitionImpl("non_matching_name", "connectorId", "version", ConnectorEvent.ON_ENTER);
        final SConnectorDefinition secondConnector = new SConnectorDefinitionImpl(connectorName, "connectorId", "version", ConnectorEvent.ON_ENTER);
        final List<SConnectorDefinition> connectors = Arrays.asList(firstConnector, secondConnector);
        when(flowNodeDefinition.getConnectors(ConnectorEvent.ON_ENTER)).thenReturn(connectors);

        final SConnectorInstance nextConnectorInstanceToExecute = mock(SConnectorInstance.class);
        when(nextConnectorInstanceToExecute.getState()).thenReturn(TO_BE_EXECUTED.name());
        when(nextConnectorInstanceToExecute.getName()).thenReturn(connectorName);
        when(connectorInstanceService.getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER))).thenReturn(
                nextConnectorInstanceToExecute);

        when(flowNodeInstance.isStateExecuting()).thenReturn(true);

        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, true);

        // then:
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER));
        checkConnectorsPhases(connectorAndFlag, DURING_ON_ENTER);
        assertThat(connectorAndFlag.getValue().getKey()).isEqualTo(nextConnectorInstanceToExecute);
        assertThat(connectorAndFlag.getValue().getValue()).isEqualTo(secondConnector);
    }

    @Test
    public void connectorsOnFinishToExecuteWithOnEnterConnAlreadyExecutedShouldReturnPhases_3_4() throws Exception {
        // given:
        haveConnectorAlreadyExecuted(ConnectorEvent.ON_ENTER);
        haveConnectorsToBeExecuted(ConnectorEvent.ON_FINISH);

        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, true);

        // then:
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER));
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_FINISH));
        checkConnectorsPhases(connectorAndFlag, BEFORE_ON_FINISH, DURING_ON_FINISH);
    }

    @Test
    public void connectorsOnFinishAlreadyExecutedWithOnEnterConnAlreadyExecutedShouldReturnPhase_5_only() throws Exception {
        // given:
        haveConnectorAlreadyExecuted(ConnectorEvent.ON_ENTER);
        haveConnectorAlreadyExecuted(ConnectorEvent.ON_FINISH);

        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, true);

        // then:
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER));
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_FINISH));
        checkConnectorsPhases(connectorAndFlag, AFTER_ON_FINISH);
    }

    @Test
    public void secondConnectorOnFinishShouldReturnPhase_4_only() throws Exception {
        // given:
        // no connectors on enter, only connectors on finish:
        final String connectorName = "connector_name";
        final SConnectorDefinition firstConnector = new SConnectorDefinitionImpl("non_matching_name", "connectorId", "version", ConnectorEvent.ON_ENTER);
        final SConnectorDefinition secondConnector = new SConnectorDefinitionImpl(connectorName, "connectorId", "version", ConnectorEvent.ON_ENTER);
        final List<SConnectorDefinition> connectors = Arrays.asList(firstConnector, secondConnector);
        when(flowNodeDefinition.getConnectors(ConnectorEvent.ON_FINISH)).thenReturn(connectors);

        final SConnectorInstance nextConnectorInstanceToExecute = mock(SConnectorInstance.class);
        when(nextConnectorInstanceToExecute.getState()).thenReturn(TO_BE_EXECUTED.name());
        when(nextConnectorInstanceToExecute.getName()).thenReturn(connectorName);
        when(connectorInstanceService.getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_FINISH))).thenReturn(
                nextConnectorInstanceToExecute);

        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, true);

        // then:
        verify(connectorInstanceService, times(0)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER));
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_FINISH));
        checkConnectorsPhases(connectorAndFlag, DURING_ON_FINISH);
        assertThat(connectorAndFlag.getValue().getKey()).isEqualTo(nextConnectorInstanceToExecute);
        assertThat(connectorAndFlag.getValue().getValue()).isEqualTo(secondConnector);
    }

    @Test
    public void ignoredConnectorOnEnterWithOnEnterConnAndNoOnFinishConnExecutedShouldReturnPhases_1_3_5() throws Exception {
        // given:
        haveConnectorAlreadyExecuted(ConnectorEvent.ON_ENTER);

        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, false, true);

        // then:
        checkConnectorsPhases(connectorAndFlag, BEFORE_ON_ENTER, BEFORE_ON_FINISH, AFTER_ON_FINISH);
    }

    @Test
    public void ignoredConnectorOnEnterWithOnEnterConnExecutedAndOnFinishConnNotExecutedShouldReturnPhases_1_3_4() throws Exception {
        // given:
        haveConnectorAlreadyExecuted(ConnectorEvent.ON_ENTER);
        haveConnectorsToBeExecuted(ConnectorEvent.ON_FINISH);

        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, false, true);

        // then:
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_FINISH));
        checkConnectorsPhases(connectorAndFlag, BEFORE_ON_ENTER, BEFORE_ON_FINISH, DURING_ON_FINISH);
    }

    @Test
    public void ignoredConnectorOnFinishWithOnEnterConnExecutedAndOnFinishConnNotExecutedShouldReturnPhases_3_5() throws Exception {
        // given:
        haveConnectorAlreadyExecuted(ConnectorEvent.ON_ENTER);
        haveConnectorsToBeExecuted(ConnectorEvent.ON_FINISH);
        when(flowNodeInstance.isStateExecuting()).thenReturn(true);

        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, false);

        // then:
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER));
        checkConnectorsPhases(connectorAndFlag, BEFORE_ON_FINISH, AFTER_ON_FINISH);
    }

    @Test
    public void ignoredConnectorOnFinishWithOnEnterConnExecutedAndOnFinishConnExecutedShouldReturnPhases_3_5() throws Exception {
        // given:
        haveConnectorAlreadyExecuted(ConnectorEvent.ON_ENTER);
        haveConnectorAlreadyExecuted(ConnectorEvent.ON_FINISH);
        when(flowNodeInstance.isStateExecuting()).thenReturn(true);

        // when:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorAndFlag = behaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, true, false);

        // then:
        verify(connectorInstanceService, times(1)).getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(ConnectorEvent.ON_ENTER));
        checkConnectorsPhases(connectorAndFlag, BEFORE_ON_FINISH, AFTER_ON_FINISH);
    }

    protected void haveConnectorsToBeExecuted(final ConnectorEvent connectorEventPhase) throws SConnectorInstanceReadException {
        haveConnector(connectorEventPhase, true);
    }

    protected void haveConnectorAlreadyExecuted(final ConnectorEvent connectorEventPhase) throws SConnectorInstanceReadException {
        haveConnector(connectorEventPhase, false);
    }

    protected void haveConnector(final ConnectorEvent connectorEventPhase, final boolean haveConnectorToBeExecuted) throws SConnectorInstanceReadException {
        haveConnector(connectorEventPhase, haveConnectorToBeExecuted, TO_BE_EXECUTED);
    }

    protected void haveConnector(final ConnectorEvent connectorEventPhase, final boolean haveConnectorToBeExecuted, final ConnectorState connectorState)
            throws SConnectorInstanceReadException {
        final String connectorName = "connector_name";
        final SConnectorDefinition firstConnector = new SConnectorDefinitionImpl(connectorName, "connectorId", "version", ConnectorEvent.ON_ENTER);
        final List<SConnectorDefinition> connectors = Arrays.asList(firstConnector);
        when(flowNodeDefinition.getConnectors(connectorEventPhase)).thenReturn(connectors);

        SConnectorInstance connectorInstance = null;
        if (haveConnectorToBeExecuted) {
            connectorInstance = mock(SConnectorInstance.class);
            when(connectorInstance.getState()).thenReturn(connectorState.name());
            when(connectorInstance.getName()).thenReturn(connectorName);
        }
        when(connectorInstanceService.getNextExecutableConnectorInstance(eq(flownodeInstanceId), anyString(), eq(connectorEventPhase))).thenReturn(
                connectorInstance);
    }


    @Test
    public void should_mapUsingUserFilters_map_user_id_only_once() throws Exception {
        //given
        final FilterResult result = new FilterResult() {
            @Override
            public List<Long> getResult() {
                return Arrays.asList(1L,2L,3L,2L);
            }

            @Override
            public boolean shouldAutoAssignTaskIfSingleResult() {
                return true;
            }
        };
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition), anyMapOf(String.class, SExpression.class),
                any(ClassLoader.class), any(SExpressionContext.class), eq("actor"));
        //when
        behaviors.mapUsingUserFilters(flowNodeInstance,flowNodeDefinition,"actor",processDefinitionId,userFilterDefinition);
        //then
        ArgumentCaptor<SPendingActivityMapping> argumentCaptor = ArgumentCaptor.forClass(SPendingActivityMapping.class);
        verify(activityInstanceService, times(3)).addPendingActivityMappings(argumentCaptor.capture());
        verify(activityInstanceService,never()).assignHumanTask(anyLong(), anyLong());
        List<SPendingActivityMapping> allValues = argumentCaptor.getAllValues();
        assertThat(allValues).hasSize(3);
        assertThat(allValues).extracting("userId").containsOnly(1L,2L,3L);
    }

    @Test
    public void should_mapUsingUserFilters_assign_if_only_one_and_auto_assign() throws Exception {
        //given
        final FilterResult result = new FilterResult() {
            @Override
            public List<Long> getResult() {
                return Arrays.asList(1L);
            }

            @Override
            public boolean shouldAutoAssignTaskIfSingleResult() {
                return true;
            }
        };
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition), anyMapOf(String.class, SExpression.class),
                any(ClassLoader.class), any(SExpressionContext.class), eq("actor"));
        //when
        behaviors.mapUsingUserFilters(flowNodeInstance,flowNodeDefinition,"actor",processDefinitionId,userFilterDefinition);
        //then
        ArgumentCaptor<SPendingActivityMapping> argumentCaptor = ArgumentCaptor.forClass(SPendingActivityMapping.class);
        verify(activityInstanceService, times(1)).addPendingActivityMappings(argumentCaptor.capture());
        verify(activityInstanceService,times(1)).assignHumanTask(anyLong(), eq(1l));
        List<SPendingActivityMapping> allValues = argumentCaptor.getAllValues();
        assertThat(allValues).extracting("userId").containsOnly(1L);
    }

    @Test
    public void should_mapUsingUserFilters_do_not_assign_if_only_one_and_auto_assign_is_false() throws Exception {
        //given
        final FilterResult result = getFilterResult(Arrays.asList(1L));
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition), anyMapOf(String.class, SExpression.class),
                any(ClassLoader.class), any(SExpressionContext.class), eq("actor"));
        //when
        behaviors.mapUsingUserFilters(flowNodeInstance,flowNodeDefinition,"actor",processDefinitionId,userFilterDefinition);
        //then
        ArgumentCaptor<SPendingActivityMapping> argumentCaptor = ArgumentCaptor.forClass(SPendingActivityMapping.class);
        verify(activityInstanceService, times(1)).addPendingActivityMappings(argumentCaptor.capture());
        verify(activityInstanceService,never()).assignHumanTask(anyLong(), anyLong());
        List<SPendingActivityMapping> allValues = argumentCaptor.getAllValues();
        assertThat(allValues).extracting("userId").containsOnly(1L);
    }
    @Test
    public void should_mapUsingUserFilters_throw_exception_when_empty_return() throws Exception {
        //given
        final FilterResult result = getFilterResult(Collections.<Long>emptyList());
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition), anyMapOf(String.class, SExpression.class),
                any(ClassLoader.class), any(SExpressionContext.class), eq("actor"));
        expectedException.expect(SActivityStateExecutionException.class);
        expectedException.expectMessage("no user id returned by the user filter");
        //when
        behaviors.mapUsingUserFilters(flowNodeInstance, flowNodeDefinition, "actor", processDefinitionId, userFilterDefinition);
    }
    @Test
    public void should_mapUsingUserFilters_throw_exception_when_null_return() throws Exception {
        //given
        final FilterResult result = getFilterResult(null);
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition), anyMapOf(String.class, SExpression.class),
                any(ClassLoader.class), any(SExpressionContext.class), eq("actor"));
        expectedException.expect(SActivityStateExecutionException.class);
        expectedException.expectMessage("no user id returned by the user filter");
        //when
        behaviors.mapUsingUserFilters(flowNodeInstance, flowNodeDefinition, "actor", processDefinitionId, userFilterDefinition);
    }

    @Test
    public void should_mapUsingUserFilters_throw_exception_when_return_contains_minus_1() throws Exception {
        //given
        final FilterResult result = getFilterResult(Arrays.asList(-1L));
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition), anyMapOf(String.class, SExpression.class),
                any(ClassLoader.class), any(SExpressionContext.class), eq("actor"));
        expectedException.expect(SActivityStateExecutionException.class);
        expectedException.expectMessage("no user id returned by the user filter");
        //when
        behaviors.mapUsingUserFilters(flowNodeInstance,flowNodeDefinition,"actor",processDefinitionId,userFilterDefinition);
    }

    @Test
    public void should_mapUsingUserFilters_throw_exception_when_return_contains_0() throws Exception {
        //given
        final FilterResult result = getFilterResult(Arrays.asList(0L));
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition), anyMapOf(String.class, SExpression.class),
                any(ClassLoader.class), any(SExpressionContext.class), eq("actor"));
        expectedException.expect(SActivityStateExecutionException.class);
        expectedException.expectMessage("no user id returned by the user filter");
        //when
        behaviors.mapUsingUserFilters(flowNodeInstance,flowNodeDefinition,"actor",processDefinitionId,userFilterDefinition);
    }

    FilterResult getFilterResult(final List<Long> theResult) {
        return new FilterResult() {
                @Override
                public List<Long> getResult() {
                    return theResult;
                }

                @Override
                public boolean shouldAutoAssignTaskIfSingleResult() {
                    return false;
                }
            };
    }
}
