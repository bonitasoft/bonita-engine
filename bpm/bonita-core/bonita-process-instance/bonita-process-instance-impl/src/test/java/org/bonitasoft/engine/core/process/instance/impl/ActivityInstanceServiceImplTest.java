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
package org.bonitasoft.engine.core.process.instance.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstanceStateCounter;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.impl.SUserTaskInstanceBuilderFactoryImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActivityInstanceServiceImplTest {

    final static String DISPLAY_NAME_255 = "123456789 123456789 123456789 123456789 123456789 123456789 123456789 12345123456789 123456789 123456789 123456789 123456789 123456789 123456789 12345123456789 123456789 123456789 123456789 123456789 123456789 123456789 12345000000000000000000000000000000";

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private Recorder recorder;

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private ActivityInstanceServiceImpl activityInstanceServiceImpl;

    private final SFlowNodeInstanceBuilderFactory keyProvider = new SUserTaskInstanceBuilderFactoryImpl();

    @Test
    public void getProcessInstanceId_should_return_container_id_if_containerType_is_process() throws Exception {
        final long expectedContainerId = 1L;

        final long processInstanceId = activityInstanceServiceImpl.getProcessInstanceId(expectedContainerId, DataInstanceContainer.PROCESS_INSTANCE.name());

        assertThat(processInstanceId).isEqualTo(expectedContainerId);
    }

    @Test
    public void getProcessInstanceId_should_return_taskInstance_processId_if_containerType_is_activity() throws Exception {
        final SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);
        when(flowNode.getParentProcessInstanceId()).thenReturn(1234L);
        final long activityInstanceId = 456L;
        when(flowNode.getId()).thenReturn(activityInstanceId);
        final ActivityInstanceServiceImpl spy = spy(activityInstanceServiceImpl);
        doReturn(flowNode).when(spy).getFlowNodeInstance(activityInstanceId);
        final long processInstanceId = spy.getProcessInstanceId(flowNode.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name());

        assertThat(processInstanceId).isEqualTo(flowNode.getParentProcessInstanceId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceId_should_throw_an_exception_for_a_not_supported_container_type() throws Exception {
        activityInstanceServiceImpl.getProcessInstanceId(1L, DataInstanceContainer.MESSAGE_INSTANCE.name());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks() throws Exception {
        final List<Long> sUserIds = new ArrayList<Long>();
        Collections.addAll(sUserIds, 78l, 2l, 5l, 486l);
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Long>> any())).thenReturn(sUserIds);

        final List<Long> userIds = activityInstanceServiceImpl.getPossibleUserIdsOfPendingTasks(2, 0, 10);
        assertEquals(sUserIds, userIds);
    }

    @Test(expected = SActivityReadException.class)
    public void throwExceptionwhenGettingPossibleUserIdsOfPendingTasksDueToPersistenceException() throws Exception {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Long>> any())).thenThrow(new SBonitaReadException("database out"));
        activityInstanceServiceImpl.getPossibleUserIdsOfPendingTasks(2, 0, 10);
    }

    @Test
    public void getEmptyPossibleUserIdsOfPendingTasks() throws Exception {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Long>> any())).thenReturn(Collections.<Long> emptyList());

        final List<Long> userIds = activityInstanceServiceImpl.getPossibleUserIdsOfPendingTasks(2, 0, 10);
        assertEquals(Collections.emptyList(), userIds);
    }

    @Test
    public void updateDisplayName_should_truncate_when_display_name_is_bigger_than_75_characters() throws Exception {
        // given
        final String displayName = DISPLAY_NAME_255 + "__";
        final SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);

        // when
        activityInstanceServiceImpl.updateDisplayName(flowNode, displayName);

        // then
        // the value must be truncated to 75 characters
        checkFlowNodeUpdate(keyProvider.getDisplayNameKey(), DISPLAY_NAME_255);
    }

    private void checkFlowNodeUpdate(final String attributeKey, final String expectedValue) throws SRecorderException {
        final ArgumentCaptor<UpdateRecord> recordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(recordCaptor.capture(), any(SUpdateEvent.class));
        final Object actualValue = recordCaptor.getValue().getFields().get(attributeKey);
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    public void updateDisplayName_should_not_change_value_when_display_name_is_lower_than_255_characters() throws Exception {
        // given
        final String displayName = "simple task";
        final SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);

        // when
        activityInstanceServiceImpl.updateDisplayName(flowNode, displayName);

        // then
        // keep original value
        checkFlowNodeUpdate(keyProvider.getDisplayNameKey(), displayName);
    }

    @Test
    public void updateDisplayName_should_not_change_value_when_display_name_is_255_characters_long() throws Exception {
        // given
        final SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);

        // when
        activityInstanceServiceImpl.updateDisplayName(flowNode, DISPLAY_NAME_255);

        // then
        // keep original value
        checkFlowNodeUpdate(keyProvider.getDisplayNameKey(), DISPLAY_NAME_255);
    }

    @Test
    public void updateDisplayDescription_should_truncate_when_display_description_is_bigger_than_255_characters() throws Exception {
        // given
        final String displayDescr255 = "123456789 123456789 123456789 123456789 123456789 "
                + "123456789 123456789 123456789 123456789 123456789 "
                + "123456789 123456789 123456789 123456789 123456789 "
                + "123456789 123456789 123456789 123456789 123456789 "
                + "123456789 123456789 123456789 123456789 123456789 12345";
        final String displayDescr = displayDescr255 + displayDescr255;
        final SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);

        // when
        activityInstanceServiceImpl.updateDisplayDescription(flowNode, displayDescr);

        // then
        // the value must be truncated to 255 characters
        checkFlowNodeUpdate(keyProvider.getDisplayDescriptionKey(), displayDescr255);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchAssignedAndPendingHumanTasksFor(long, long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public void searchAssignedAndPendingHumanTasksFor() throws Exception {
        // Given
        final long rootProcessDefinitionId = 10;
        final long userId = 6;
        final QueryOptions options = new QueryOptions(0, 10);
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        parameters.put("rootProcessDefinitionId", rootProcessDefinitionId);
        when(persistenceService.searchEntity(SHumanTaskInstance.class, "AssignedAndPendingByRootProcessFor", options, parameters)).thenReturn(
                new ArrayList<SHumanTaskInstance>());

        // When
        final List<SHumanTaskInstance> result = activityInstanceServiceImpl.searchAssignedAndPendingHumanTasksFor(rootProcessDefinitionId, userId, options);

        // Then
        assertNotNull(result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchAssignedAndPendingHumanTasksForThrowException() throws Exception {
        // Given
        final long rootProcessDefinitionId = 10;
        final long userId = 6;
        final QueryOptions options = new QueryOptions(0, 10);
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        parameters.put("rootProcessDefinitionId", rootProcessDefinitionId);
        when(persistenceService.searchEntity(SHumanTaskInstance.class, "AssignedAndPendingByRootProcessFor", options, parameters)).thenThrow(
                new SBonitaReadException(""));

        // When
        activityInstanceServiceImpl.searchAssignedAndPendingHumanTasksFor(rootProcessDefinitionId, userId, options);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfAssignedAndPendingHumanTasksFor(long, long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public void getNumberOfAssignedAndPendingHumanTasksFor() throws Exception {
        // Given
        final long rootProcessDefinitionId = 10;
        final long userId = 6;
        final QueryOptions options = new QueryOptions(0, 10);
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        parameters.put("rootProcessDefinitionId", rootProcessDefinitionId);
        when(persistenceService.getNumberOfEntities(SHumanTaskInstance.class, "AssignedAndPendingByRootProcessFor", options, parameters)).thenReturn(1L);

        // When
        final long result = activityInstanceServiceImpl.getNumberOfAssignedAndPendingHumanTasksFor(rootProcessDefinitionId, userId, options);

        // Then
        assertEquals(1L, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfAssignedAndPendingHumanTasksForThrowException() throws Exception {
        // Given
        final long rootProcessDefinitionId = 10;
        final long userId = 6;
        final QueryOptions options = new QueryOptions(0, 10);
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        parameters.put("rootProcessDefinitionId", rootProcessDefinitionId);
        when(persistenceService.getNumberOfEntities(SHumanTaskInstance.class, "AssignedAndPendingByRootProcessFor", options, parameters)).thenThrow(
                new SBonitaReadException(""));

        // When
        activityInstanceServiceImpl.getNumberOfAssignedAndPendingHumanTasksFor(rootProcessDefinitionId, userId, options);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#searchAssignedAndPendingHumanTasks(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public void searchAssignedAndPendingHumanTasks() throws Exception {
        // Given
        final long rootProcessDefinitionId = 10;
        final QueryOptions options = new QueryOptions(0, 10);
        final Map<String, Object> parameters = Collections.singletonMap("rootProcessDefinitionId", (Object) rootProcessDefinitionId);
        when(persistenceService.searchEntity(SHumanTaskInstance.class, "AssignedAndPendingByRootProcess", options, parameters)).thenReturn(
                new ArrayList<SHumanTaskInstance>());

        // When
        final List<SHumanTaskInstance> result = activityInstanceServiceImpl.searchAssignedAndPendingHumanTasks(rootProcessDefinitionId, options);

        // Then
        assertNotNull(result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchAssignedAndPendingHumanTasksThrowException() throws Exception {
        // Given
        final long rootProcessDefinitionId = 10;
        final QueryOptions options = new QueryOptions(0, 10);
        final Map<String, Object> parameters = Collections.singletonMap("rootProcessDefinitionId", (Object) rootProcessDefinitionId);
        when(persistenceService.searchEntity(SHumanTaskInstance.class, "AssignedAndPendingByRootProcess", options, parameters)).thenThrow(
                new SBonitaReadException(""));

        // When
        activityInstanceServiceImpl.searchAssignedAndPendingHumanTasks(rootProcessDefinitionId, options);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.impl.ActivityInstanceServiceImpl#getNumberOfAssignedAndPendingHumanTasks(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public void getNumberOfAssignedAndPendingHumanTasks() throws Exception {
        // Given
        final long rootProcessDefinitionId = 10;
        final QueryOptions options = new QueryOptions(0, 10);
        final Map<String, Object> parameters = Collections.singletonMap("rootProcessDefinitionId", (Object) rootProcessDefinitionId);
        when(persistenceService.getNumberOfEntities(SHumanTaskInstance.class, "AssignedAndPendingByRootProcess", options, parameters)).thenReturn(1L);

        // When
        final long result = activityInstanceServiceImpl.getNumberOfAssignedAndPendingHumanTasks(rootProcessDefinitionId, options);

        // Then
        assertEquals(1L, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfAssignedAndPendingHumanTasksThrowException() throws Exception {
        // Given
        final long rootProcessDefinitionId = 10;
        final QueryOptions options = new QueryOptions(0, 10);
        final Map<String, Object> parameters = Collections.singletonMap("rootProcessDefinitionId", (Object) rootProcessDefinitionId);
        when(persistenceService.getNumberOfEntities(SHumanTaskInstance.class, "AssignedAndPendingByRootProcess", options, parameters)).thenThrow(
                new SBonitaReadException(""));

        // When
        activityInstanceServiceImpl.getNumberOfAssignedAndPendingHumanTasks(rootProcessDefinitionId, options);
    }

    @Test
    public void getNumberOfFlownodesInAllStates_should_return_empty_collections_if_no_results() throws SBonitaReadException {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Map<String, Object>>> any())).thenReturn(
                Collections.<Map<String, Object>> emptyList());

        final List<SFlowNodeInstanceStateCounter> numberOfFlownodesInState = activityInstanceServiceImpl.getNumberOfFlownodesInAllStates(2L);
        assertThat(numberOfFlownodesInState).isEmpty();
    }

    @Test
    public void getNumberOfArchivedFlownodesInAllStates_should_return_empty_collections_if_no_results() throws SBonitaReadException {
        when(persistenceService.selectList(Matchers.<SelectListDescriptor<Map<String, Object>>> any())).thenReturn(
                Collections.<Map<String, Object>> emptyList());

        final List<SFlowNodeInstanceStateCounter> numberOfFlownodesInState = activityInstanceServiceImpl.getNumberOfArchivedFlownodesInAllStates(2L);
        assertThat(numberOfFlownodesInState).isEmpty();
    }

}
