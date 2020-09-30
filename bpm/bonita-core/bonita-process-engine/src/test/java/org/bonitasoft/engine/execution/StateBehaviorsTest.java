/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import static org.bonitasoft.engine.bpm.connector.ConnectorState.TO_BE_EXECUTED;
import static org.bonitasoft.engine.bpm.connector.ConnectorState.TO_RE_EXECUTE;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.filter.FilterResult;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SConnectorDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Phase order is: BEFORE_ON_ENTER > DURING_ON_ENTER > BEFORE_ON_FINISH > DURING_ON_FINISH > AFTER_ON_FINISH.
 *
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class StateBehaviorsTest {

    final long flownodeInstanceId = 3541L;
    final long processDefinitionId = 4567L;
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
    @Mock
    private ExpressionResolverService expressionResolverService;
    @InjectMocks
    private StateBehaviors stateBehaviors;

    @Before
    public void setConstants() {
        when(flowNodeInstance.getId()).thenReturn(flownodeInstanceId);
    }

    @Test
    public void should_mapUsingUserFilters_map_user_id_only_once() throws Exception {
        //given
        final FilterResult result = new FilterResult() {

            @Override
            public List<Long> getResult() {
                return Arrays.asList(1L, 2L, 3L, 2L);
            }

            @Override
            public boolean shouldAutoAssignTaskIfSingleResult() {
                return true;
            }
        };
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition),
                anyMap(),
                nullable(ClassLoader.class), nullable(SExpressionContext.class), eq("actor"));
        //when
        stateBehaviors.mapUsingUserFilters(flowNodeInstance, flowNodeDefinition, "actor", processDefinitionId,
                userFilterDefinition);
        //then
        ArgumentCaptor<SPendingActivityMapping> argumentCaptor = ArgumentCaptor.forClass(SPendingActivityMapping.class);
        verify(activityInstanceService, times(3)).addPendingActivityMappings(argumentCaptor.capture());
        verify(activityInstanceService, never()).assignHumanTask(anyLong(), anyLong());
        List<SPendingActivityMapping> allValues = argumentCaptor.getAllValues();
        assertThat(allValues).hasSize(3);
        assertThat(allValues).extracting("userId").containsOnly(1L, 2L, 3L);
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
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition),
                anyMap(),
                nullable(ClassLoader.class), nullable(SExpressionContext.class), eq("actor"));
        //when
        stateBehaviors.mapUsingUserFilters(flowNodeInstance, flowNodeDefinition, "actor", processDefinitionId,
                userFilterDefinition);
        //then
        ArgumentCaptor<SPendingActivityMapping> argumentCaptor = ArgumentCaptor.forClass(SPendingActivityMapping.class);
        verify(activityInstanceService, times(1)).addPendingActivityMappings(argumentCaptor.capture());
        verify(activityInstanceService, times(1)).assignHumanTask(anyLong(), eq(1l));
        List<SPendingActivityMapping> allValues = argumentCaptor.getAllValues();
        assertThat(allValues).extracting("userId").containsOnly(1L);
    }

    @Test
    public void should_mapUsingUserFilters_do_not_assign_if_only_one_and_auto_assign_is_false() throws Exception {
        //given
        final FilterResult result = getFilterResult(Arrays.asList(1L));
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition),
                anyMap(),
                nullable(ClassLoader.class), nullable(SExpressionContext.class), eq("actor"));
        //when
        stateBehaviors.mapUsingUserFilters(flowNodeInstance, flowNodeDefinition, "actor", processDefinitionId,
                userFilterDefinition);
        //then
        ArgumentCaptor<SPendingActivityMapping> argumentCaptor = ArgumentCaptor.forClass(SPendingActivityMapping.class);
        verify(activityInstanceService, times(1)).addPendingActivityMappings(argumentCaptor.capture());
        verify(activityInstanceService, never()).assignHumanTask(anyLong(), anyLong());
        List<SPendingActivityMapping> allValues = argumentCaptor.getAllValues();
        assertThat(allValues).extracting("userId").containsOnly(1L);
    }

    @Test
    public void should_mapUsingUserFilters_throw_exception_when_empty_return() throws Exception {
        //given
        final FilterResult result = getFilterResult(Collections.<Long> emptyList());
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition),
                anyMap(),
                nullable(ClassLoader.class), nullable(SExpressionContext.class), eq("actor"));
        expectedException.expect(SActivityStateExecutionException.class);
        expectedException.expectMessage("no user id returned by the user filter");
        //when
        stateBehaviors.mapUsingUserFilters(flowNodeInstance, flowNodeDefinition, "actor", processDefinitionId,
                userFilterDefinition);
    }

    @Test
    public void should_mapUsingUserFilters_throw_exception_when_null_return() throws Exception {
        //given
        final FilterResult result = getFilterResult(null);
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition),
                anyMap(),
                nullable(ClassLoader.class), nullable(SExpressionContext.class), eq("actor"));
        expectedException.expect(SActivityStateExecutionException.class);
        expectedException.expectMessage("no user id returned by the user filter");
        //when
        stateBehaviors.mapUsingUserFilters(flowNodeInstance, flowNodeDefinition, "actor", processDefinitionId,
                userFilterDefinition);
    }

    @Test
    public void should_mapUsingUserFilters_throw_exception_when_return_contains_minus_1() throws Exception {
        //given
        final FilterResult result = getFilterResult(Arrays.asList(-1L));
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition),
                anyMap(),
                nullable(ClassLoader.class), nullable(SExpressionContext.class), eq("actor"));
        expectedException.expect(SActivityStateExecutionException.class);
        expectedException.expectMessage("no user id returned by the user filter");
        //when
        stateBehaviors.mapUsingUserFilters(flowNodeInstance, flowNodeDefinition, "actor", processDefinitionId,
                userFilterDefinition);
    }

    @Test
    public void should_mapUsingUserFilters_throw_exception_when_return_contains_0() throws Exception {
        //given
        final FilterResult result = getFilterResult(Arrays.asList(0L));
        doReturn(result).when(userFilterService).executeFilter(eq(processDefinitionId), eq(userFilterDefinition),
                anyMap(),
                nullable(ClassLoader.class), nullable(SExpressionContext.class), eq("actor"));
        expectedException.expect(SActivityStateExecutionException.class);
        expectedException.expectMessage("no user id returned by the user filter");
        //when
        stateBehaviors.mapUsingUserFilters(flowNodeInstance, flowNodeDefinition, "actor", processDefinitionId,
                userFilterDefinition);
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

    @Test
    public void should_noConnectorHasStartedInCurrentList_return_true_when_first_connector_must_be_executed()
            throws Exception {
        //given
        List<SConnectorDefinition> sConnectorDefinitions = Arrays.<SConnectorDefinition> asList(
                new SConnectorDefinitionImpl("connector1", null, null, null),
                new SConnectorDefinitionImpl("connector2", null, null, null));
        SConnectorInstance sConnectorInstance = new SConnectorInstance("connector1", 12, null, null, null, null);
        sConnectorInstance.setState(TO_BE_EXECUTED.name());
        //when
        boolean noConnectorStarted = stateBehaviors.noConnectorHasStartedInCurrentList(sConnectorDefinitions,
                sConnectorInstance);
        //then
        assertThat(noConnectorStarted).isTrue();
    }

    @Test
    public void should_noConnectorHasStartedInCurrentList_return_false_when_first_connector_must_be_RE_executed()
            throws Exception {
        //given
        List<SConnectorDefinition> sConnectorDefinitions = Arrays.<SConnectorDefinition> asList(
                new SConnectorDefinitionImpl("connector1", null, null, null),
                new SConnectorDefinitionImpl("connector2", null, null, null));
        SConnectorInstance sConnectorInstance = new SConnectorInstance("connector1", 12, null, null, null, null);
        sConnectorInstance.setState(TO_RE_EXECUTE.name());
        //when
        boolean noConnectorStarted = stateBehaviors.noConnectorHasStartedInCurrentList(sConnectorDefinitions,
                sConnectorInstance);
        //then
        assertThat(noConnectorStarted).isFalse();
    }

    @Test
    public void should_noConnectorHasStartedInCurrentList_return_false_when_connector_must_be_executed_but_is_not_the_first()
            throws Exception {
        //given
        List<SConnectorDefinition> sConnectorDefinitions = Arrays.<SConnectorDefinition> asList(
                new SConnectorDefinitionImpl("connector1", null, null, null),
                new SConnectorDefinitionImpl("connector2", null, null, null));
        SConnectorInstance sConnectorInstance = new SConnectorInstance("connector2", 12, null, null, null, null);
        sConnectorInstance.setState(TO_BE_EXECUTED.name());
        //when
        boolean noConnectorStarted = stateBehaviors.noConnectorHasStartedInCurrentList(sConnectorDefinitions,
                sConnectorInstance);
        //then
        assertThat(noConnectorStarted).isFalse();
    }

    @Test
    public void should_noConnectorHasStartedInCurrentList_return_false_when_no_connector_must_be_executed_but_some_were_executed()
            throws Exception {
        //given
        List<SConnectorDefinition> sConnectorDefinitions = Arrays.<SConnectorDefinition> asList(
                new SConnectorDefinitionImpl("connector1", null, null, null),
                new SConnectorDefinitionImpl("connector2", null, null, null));
        //when
        boolean noConnectorStarted = stateBehaviors.noConnectorHasStartedInCurrentList(sConnectorDefinitions, null);
        //then
        assertThat(noConnectorStarted).isFalse();
    }

    @Test
    public void should_noConnectorHasStartedInCurrentList_return_true_when_no_connector_must_be_executed()
            throws Exception {
        //given
        List<SConnectorDefinition> sConnectorDefinitions = Collections.emptyList();
        //when
        boolean noConnectorStarted = stateBehaviors.noConnectorHasStartedInCurrentList(sConnectorDefinitions, null);
        //then
        assertThat(noConnectorStarted).isTrue();
    }
}
