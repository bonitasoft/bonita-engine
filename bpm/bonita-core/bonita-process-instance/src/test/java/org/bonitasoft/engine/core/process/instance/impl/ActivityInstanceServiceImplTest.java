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
package org.bonitasoft.engine.core.process.instance.impl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstanceStateCounter;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.impl.SUserTaskInstanceBuilderFactoryImpl;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActivityInstanceServiceImplTest {

    final static String DISPLAY_NAME_255 = "123456789 123456789 123456789 123456789 123456789 123456789 123456789 12345123456789 123456789 123456789 123456789 123456789 123456789 123456789 12345123456789 123456789 123456789 123456789 123456789 123456789 123456789 12345000000000000000000000000000000";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private Recorder recorder;

    @InjectMocks
    private ActivityInstanceServiceImpl activityInstanceServiceImpl;

    private final SFlowNodeInstanceBuilderFactory keyProvider = new SUserTaskInstanceBuilderFactoryImpl();

    @Mock
    private SFlowNodeInstance sFlowNodeInstance;

    @Test
    public void getPossibleUserIdsOfPendingTasks() throws Exception {
        final List<Long> sUserIds = new ArrayList<Long>();
        Collections.addAll(sUserIds, 78l, 2l, 5l, 486l);
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<Long>> any())).thenReturn(sUserIds);

        final List<Long> userIds = activityInstanceServiceImpl.getPossibleUserIdsOfPendingTasks(2, 0, 10);
        assertEquals(sUserIds, userIds);
    }

    @Test(expected = SActivityReadException.class)
    public void throwExceptionwhenGettingPossibleUserIdsOfPendingTasksDueToPersistenceException() throws Exception {
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<Long>> any()))
                .thenThrow(new SBonitaReadException("database out"));
        activityInstanceServiceImpl.getPossibleUserIdsOfPendingTasks(2, 0, 10);
    }

    @Test
    public void getEmptyPossibleUserIdsOfPendingTasks() throws Exception {
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<Long>> any()))
                .thenReturn(Collections.<Long> emptyList());

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
        verify(recorder, times(1)).recordUpdate(recordCaptor.capture(), anyString());
        final Object actualValue = recordCaptor.getValue().getFields().get(attributeKey);
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    public void updateDisplayName_should_not_change_value_when_display_name_is_lower_than_255_characters()
            throws Exception {
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
    public void updateDisplayDescription_should_truncate_when_display_description_is_bigger_than_255_characters()
            throws Exception {
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
        when(persistenceService.searchEntity(SHumanTaskInstance.class, "AssignedAndPendingByRootProcessFor", options,
                parameters)).thenReturn(
                        new ArrayList<SHumanTaskInstance>());

        // When
        final List<SHumanTaskInstance> result = activityInstanceServiceImpl
                .searchAssignedAndPendingHumanTasksFor(rootProcessDefinitionId, userId, options);

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
        when(persistenceService.searchEntity(SHumanTaskInstance.class, "AssignedAndPendingByRootProcessFor", options,
                parameters)).thenThrow(
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
        when(persistenceService.getNumberOfEntities(SHumanTaskInstance.class, "AssignedAndPendingByRootProcessFor",
                options, parameters)).thenReturn(1L);

        // When
        final long result = activityInstanceServiceImpl
                .getNumberOfAssignedAndPendingHumanTasksFor(rootProcessDefinitionId, userId, options);

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
        when(persistenceService.getNumberOfEntities(SHumanTaskInstance.class, "AssignedAndPendingByRootProcessFor",
                options, parameters)).thenThrow(
                        new SBonitaReadException(""));

        // When
        activityInstanceServiceImpl.getNumberOfAssignedAndPendingHumanTasksFor(rootProcessDefinitionId, userId,
                options);
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
        final Map<String, Object> parameters = Collections.singletonMap("rootProcessDefinitionId",
                (Object) rootProcessDefinitionId);
        when(persistenceService.searchEntity(SHumanTaskInstance.class, "AssignedAndPendingByRootProcess", options,
                parameters)).thenReturn(
                        new ArrayList<SHumanTaskInstance>());

        // When
        final List<SHumanTaskInstance> result = activityInstanceServiceImpl
                .searchAssignedAndPendingHumanTasks(rootProcessDefinitionId, options);

        // Then
        assertNotNull(result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchAssignedAndPendingHumanTasksThrowException() throws Exception {
        // Given
        final long rootProcessDefinitionId = 10;
        final QueryOptions options = new QueryOptions(0, 10);
        final Map<String, Object> parameters = Collections.singletonMap("rootProcessDefinitionId",
                (Object) rootProcessDefinitionId);
        when(persistenceService.searchEntity(SHumanTaskInstance.class, "AssignedAndPendingByRootProcess", options,
                parameters)).thenThrow(
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
        final Map<String, Object> parameters = Collections.singletonMap("rootProcessDefinitionId",
                (Object) rootProcessDefinitionId);
        when(persistenceService.getNumberOfEntities(SHumanTaskInstance.class, "AssignedAndPendingByRootProcess",
                options, parameters)).thenReturn(1L);

        // When
        final long result = activityInstanceServiceImpl.getNumberOfAssignedAndPendingHumanTasks(rootProcessDefinitionId,
                options);

        // Then
        assertEquals(1L, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfAssignedAndPendingHumanTasksThrowException() throws Exception {
        // Given
        final long rootProcessDefinitionId = 10;
        final QueryOptions options = new QueryOptions(0, 10);
        final Map<String, Object> parameters = Collections.singletonMap("rootProcessDefinitionId",
                (Object) rootProcessDefinitionId);
        when(persistenceService.getNumberOfEntities(SHumanTaskInstance.class, "AssignedAndPendingByRootProcess",
                options, parameters)).thenThrow(
                        new SBonitaReadException(""));

        // When
        activityInstanceServiceImpl.getNumberOfAssignedAndPendingHumanTasks(rootProcessDefinitionId, options);
    }

    @Test
    public void getNumberOfFlownodesOfProcessDefinitionInAllStates_should_return_empty_collections_if_no_results()
            throws SBonitaReadException {
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<Map<String, Object>>> any()))
                .thenReturn(
                        Collections.<Map<String, Object>> emptyList());

        final List<SFlowNodeInstanceStateCounter> numberOfFlownodesInState = activityInstanceServiceImpl
                .getNumberOfFlownodesOfProcessDefinitionInAllStates(1L);
        assertThat(numberOfFlownodesInState).isEmpty();
    }

    @Test
    public void getNumberOfFlownodesInAllStates_should_return_empty_collections_if_no_results()
            throws SBonitaReadException {
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<Map<String, Object>>> any()))
                .thenReturn(
                        Collections.<Map<String, Object>> emptyList());

        final List<SFlowNodeInstanceStateCounter> numberOfFlownodesInState = activityInstanceServiceImpl
                .getNumberOfFlownodesInAllStates(2L);
        assertThat(numberOfFlownodesInState).isEmpty();
    }

    @Test
    public void getNumberOfArchivedFlownodesInAllStates_should_return_empty_collections_if_no_results()
            throws SBonitaReadException {
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<Map<String, Object>>> any()))
                .thenReturn(
                        Collections.<Map<String, Object>> emptyList());

        final List<SFlowNodeInstanceStateCounter> numberOfFlownodesInState = activityInstanceServiceImpl
                .getNumberOfArchivedFlownodesInAllStates(2L);
        assertThat(numberOfFlownodesInState).isEmpty();
    }

    @Test
    public void should_update_due_date_on_right_flownode_with_right_update_event_message() throws Exception {
        //given
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.EXPECTED_END_DATE_MODIFIED),
                eq("updateHumanTaskExpectedEndDate"))).thenReturn(1);

        //when
        activityInstanceServiceImpl.setExpectedEndDate(sFlowNodeInstance, 123L);

        ArgumentCaptor<UpdateRecord> updateRecordArgumentCaptor = ArgumentCaptor.forClass(UpdateRecord.class);

        //then
        verify(recorder).recordUpdateWithQuery(updateRecordArgumentCaptor.capture(),
                eq(FlowNodeInstanceService.EXPECTED_END_DATE_MODIFIED),
                eq("updateHumanTaskExpectedEndDate"));
        assertThat(updateRecordArgumentCaptor.getValue().getEntity()).as("should update entity")
                .isEqualTo(sFlowNodeInstance);
        // the atomic update must touch ONLY the intended columns: expectedEndDate, lastUpdateDate and the
        // id used as the WHERE parameter. Fails if anyone reverts to a whole-row recordUpdate (BPA-167).
        assertThat(updateRecordArgumentCaptor.getValue().getFields().keySet())
                .as("should update only the expectedEndDate and lastUpdateDate fields, plus the id parameter")
                .containsExactlyInAnyOrder("id", "expectedEndDate", "lastUpdateDate");
        assertThat(updateRecordArgumentCaptor.getValue().getFields().get("expectedEndDate"))
                .as("expectedEndDate should have the new value")
                .isEqualTo(123L);
    }

    @Test
    public void should_updateExpectedEndDate_throw_SFlowNodeModificationException_when_recorder_fails()
            throws Exception {
        //given
        // the matchers are deliberately loose so that the stub matches whatever event and query name the
        // writer uses: the expected exception then provably comes from the recorder failure and never from
        // the row-count guard, which would raise the very same exception type
        doThrow(SRecorderException.class).when(recorder).recordUpdateWithQuery(any(UpdateRecord.class), anyString(),
                anyString());

        //expect
        expectedException.expect(SFlowNodeModificationException.class);

        //when
        activityInstanceServiceImpl.setExpectedEndDate(sFlowNodeInstance, 123L);
    }

    @Test
    public void should_search_pending_tasks_assigned_to_a_user() throws Exception {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);
        List<SHumanTaskInstance> expectedResult = new ArrayList<>();
        when(persistenceService.searchEntity(SHumanTaskInstance.class, "PendingAssignedTo", options,
                Collections.singletonMap("userId", 61L)))
                .thenReturn(expectedResult);

        // When
        final List<SHumanTaskInstance> result = activityInstanceServiceImpl.searchPendingTasksAssignedTo(61L, options);

        // Then
        assertThat(result).describedAs("Human tasks list").isSameAs(expectedResult);
    }

    @Test
    public void should_search_pending_tasks_assigned_to_a_user_throw_exception_on_persitence_error() throws Exception {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.searchEntity(SHumanTaskInstance.class, "PendingAssignedTo", options,
                Collections.singletonMap("userId", 99L)))
                .thenThrow(new SBonitaReadException("Fake for test"));

        // When
        Throwable thrown = catchThrowable(() -> {
            activityInstanceServiceImpl.searchPendingTasksAssignedTo(99L, options);
        });

        // Then
        assertThat(thrown)
                .isInstanceOf(SBonitaReadException.class)
                .hasMessageContaining("Fake for test");
    }

    @Test
    public void should_count_number_of_pending_tasks_assigned_to_user() throws Exception {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(SHumanTaskInstance.class, "PendingAssignedTo", options,
                Collections.singletonMap("userId", 365L)))
                .thenReturn(14L);

        // When
        final long count = activityInstanceServiceImpl.getNumberOfPendingTasksAssignedTo(365L, options);

        // Then
        assertThat(count).describedAs("Number of human tasks").isEqualTo(14L);
    }

    @Test
    public void should_count_number_of_pending_tasks_assigned_to_user_throw_exception_on_persitence_error()
            throws Exception {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);
        when(persistenceService.getNumberOfEntities(SHumanTaskInstance.class, "PendingAssignedTo", options,
                Collections.singletonMap("userId", 3365L)))
                .thenThrow(new SBonitaReadException("Fake for test"));

        // When
        Throwable thrown = catchThrowable(() -> {
            activityInstanceServiceImpl.getNumberOfPendingTasksAssignedTo(3365L, options);
        });

        // Then
        assertThat(thrown)
                .isInstanceOf(SBonitaReadException.class)
                .hasMessageContaining("Fake for test");
    }

    // === Tests for lastUpdateDate on ActivityInstanceServiceImpl methods ===

    @Test
    public void should_assignHumanTask_update_lastUpdateDate() throws Exception {
        // Given
        SHumanTaskInstance humanTask = mock(SHumanTaskInstance.class);
        when(persistenceService.selectById(any())).thenReturn(humanTask);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class), eq("HUMAN_TASK_INSTANCE_ASSIGNEE"),
                eq("updateHumanTaskAssignee"))).thenReturn(1);

        // When
        activityInstanceServiceImpl.assignHumanTask(123L, 456L);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdateWithQuery(captor.capture(), eq("HUMAN_TASK_INSTANCE_ASSIGNEE"),
                eq("updateHumanTaskAssignee"));
        // the atomic update must touch ONLY the intended columns: assigneeId, claimedDate, lastUpdateDate
        // and the id used as the WHERE parameter. Fails if anyone reverts to a whole-row recordUpdate (BPA-167).
        assertThat(captor.getValue().getFields().keySet())
                .as("should update only the assignee fields, plus the id parameter")
                .containsExactlyInAnyOrder("id", "assigneeId", "claimedDate", "lastUpdateDate");
        // The bulk HQL update never touches the managed entity, so the in-memory copy is stale. Without
        // this refresh the commit flush writes the stale snapshot back over the whole row -- reverting the
        // assignment on the engine path, and making assignAndExecuteUserTask re-read a stale assignee.
        // This is the load-bearing defence of the fix; pinned so that deleting it cannot pass silently.
        verify(persistenceService).refresh(humanTask);
    }

    @Test
    public void should_releaseHumanTask_update_only_the_assignee_fields() throws Exception {
        // Given the release branch (userId == 0), whose descriptor must pair with the SAME named query.
        // The descriptor/query pairing is enforced only at runtime, so both branches need an oracle.
        SHumanTaskInstance humanTask = mock(SHumanTaskInstance.class);
        when(persistenceService.selectById(any())).thenReturn(humanTask);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class), eq("HUMAN_TASK_INSTANCE_ASSIGNEE"),
                eq("updateHumanTaskAssignee"))).thenReturn(1);

        // When
        activityInstanceServiceImpl.assignHumanTask(123L, 0L);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdateWithQuery(captor.capture(), eq("HUMAN_TASK_INSTANCE_ASSIGNEE"),
                eq("updateHumanTaskAssignee"));
        assertThat(captor.getValue().getFields().keySet())
                .as("the release branch must carry exactly the same key set as the assign branch")
                .containsExactlyInAnyOrder("id", "assigneeId", "claimedDate", "lastUpdateDate");
        assertThat(captor.getValue().getFields())
                .containsEntry("assigneeId", 0L)
                .containsEntry("claimedDate", 0L);
        verify(persistenceService).refresh(humanTask);
    }

    @Test
    public void should_assignHumanTaskIfNotAssigned_update_only_the_assignee_fields() throws Exception {
        // The conditional assign stays on `updateStrictHuman`. The descriptor key set and the named query's
        // parameters have to match exactly -- `setParameters` binds every descriptor key, so an extra key with
        // no matching HQL parameter throws at runtime, not at compile time.
        SHumanTaskInstance humanTask = mock(SHumanTaskInstance.class);
        when(persistenceService.selectById(any())).thenReturn(humanTask);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class), eq("HUMAN_TASK_INSTANCE_ASSIGNEE"),
                eq("updateStrictHuman"))).thenReturn(1);

        // When
        activityInstanceServiceImpl.assignHumanTaskIfNotAssigned(123L, 456L);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdateWithQuery(captor.capture(), eq("HUMAN_TASK_INSTANCE_ASSIGNEE"),
                eq("updateStrictHuman"));
        assertThat(captor.getValue().getFields().keySet())
                .as("should update only the assignee fields, plus the id parameter")
                .containsExactlyInAnyOrder("id", "assigneeId", "claimedDate", "lastUpdateDate");
    }

    @Test
    public void should_assignHumanTask_throw_SActivityModificationException_when_no_row_was_updated() throws Exception {
        // Given the atomic update matches no row (task concurrently deleted, or an id that no longer exists)
        SHumanTaskInstance humanTask = mock(SHumanTaskInstance.class);
        when(persistenceService.selectById(any())).thenReturn(humanTask);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class), eq("HUMAN_TASK_INSTANCE_ASSIGNEE"),
                eq("updateHumanTaskAssignee"))).thenReturn(0);

        // When
        final Throwable thrown = catchThrowable(() -> activityInstanceServiceImpl.assignHumanTask(123L, 456L));

        // Then the guard must fail loud, never swallow a missed update and report success
        assertThat(thrown)
                .as("assignHumanTask should fail loud when the atomic update updated no row")
                .isInstanceOf(SActivityModificationException.class)
                // A concurrently archived or deleted task is transient. The work retryability evaluator walks
                // the whole cause chain and classifies a message-only exception NOT_RETRYABLE, which puts the
                // flow node in FAILED state instead of retrying it -- a regression against the pre-BPA-167
                // behaviour, where Hibernate's own StaleStateException (a retryable class) surfaced here.
                .hasCauseInstanceOf(SRetryableException.class);
    }

    @Test
    public void should_incrementLoopCounter_update_lastUpdateDate() throws Exception {
        // Given
        SLoopActivityInstance loopInstance = mock(SLoopActivityInstance.class);
        when(loopInstance.getLoopCounter()).thenReturn(5);

        // When
        activityInstanceServiceImpl.incrementLoopCounter(loopInstance);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdate(captor.capture(), anyString());
        assertThat(captor.getValue().getFields().keySet())
                .contains("loopCounter", "lastUpdateDate");
    }

    @Test
    public void should_setLoopMax_update_lastUpdateDate() throws Exception {
        // Given
        SLoopActivityInstance loopActivity = mock(SLoopActivityInstance.class);

        // When
        activityInstanceServiceImpl.setLoopMax(loopActivity, 10);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdate(captor.capture(), anyString());
        assertThat(captor.getValue().getFields().keySet())
                .contains("loopMax", "lastUpdateDate");
    }

    @Test
    public void should_setLoopCardinality_update_lastUpdateDate() throws Exception {
        // Given
        SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);

        // When
        activityInstanceServiceImpl.setLoopCardinality(flowNode, 5);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdate(captor.capture(), anyString());
        assertThat(captor.getValue().getFields().keySet())
                .contains("loopCardinality", "lastUpdateDate");
    }

    @Test
    public void should_addMultiInstanceNumberOfActiveActivities_update_lastUpdateDate() throws Exception {
        // Given
        SMultiInstanceActivityInstance multiInstance = mock(SMultiInstanceActivityInstance.class);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED),
                eq("updateMultiInstanceActiveCounters"))).thenReturn(1);

        // When
        activityInstanceServiceImpl.addMultiInstanceNumberOfActiveActivities(multiInstance, 2);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdateWithQuery(captor.capture(),
                eq(FlowNodeInstanceService.MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED),
                eq("updateMultiInstanceActiveCounters"));
        // Exact, not `contains`: these three counter queries filter on `WHERE id = :id`. Pinning the key set
        // exactly is what stops a later change from adding a descriptor key without the matching
        // HQL parameter, which compiles, passes a loose assertion, and throws QueryParameterException in
        // production.
        assertThat(captor.getValue().getFields().keySet())
                .containsExactlyInAnyOrder("id", "number", "lastUpdateDate");
    }

    @Test
    public void should_addMultiInstanceNumberOfTerminatedActivities_update_lastUpdateDate() throws Exception {
        // Given
        SMultiInstanceActivityInstance multiInstance = mock(SMultiInstanceActivityInstance.class);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED),
                eq("updateMultiInstanceTerminatedCounters"))).thenReturn(1);

        // When
        activityInstanceServiceImpl.addMultiInstanceNumberOfTerminatedActivities(multiInstance, 1);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdateWithQuery(captor.capture(),
                eq(FlowNodeInstanceService.MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED),
                eq("updateMultiInstanceTerminatedCounters"));
        // Exact, not `contains`: these three counter queries filter on `WHERE id = :id`. Pinning the key set
        // exactly is what stops a later change from adding a descriptor key without the matching
        // HQL parameter, which compiles, passes a loose assertion, and throws QueryParameterException in
        // production.
        assertThat(captor.getValue().getFields().keySet())
                .containsExactlyInAnyOrder("id", "number", "lastUpdateDate");
    }

    @Test
    public void should_addMultiInstanceNumberOfCompletedActivities_update_lastUpdateDate() throws Exception {
        // Given
        SMultiInstanceActivityInstance multiInstance = mock(SMultiInstanceActivityInstance.class);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED),
                eq("updateMultiInstanceCompletedCounters"))).thenReturn(1);

        // When
        activityInstanceServiceImpl.addMultiInstanceNumberOfCompletedActivities(multiInstance, 1);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdateWithQuery(captor.capture(),
                eq(FlowNodeInstanceService.MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED),
                eq("updateMultiInstanceCompletedCounters"));
        // Exact, not `contains`: these three counter queries filter on `WHERE id = :id`. Pinning the key set
        // exactly is what stops a later change from adding a descriptor key without the matching
        // HQL parameter, which compiles, passes a loose assertion, and throws QueryParameterException in
        // production.
        assertThat(captor.getValue().getFields().keySet())
                .containsExactlyInAnyOrder("id", "number", "lastUpdateDate");
    }

    @Test
    public void should_setTokenCount_update_lastUpdateDate() throws Exception {
        // Given
        SActivityInstance activityInstance = mock(SActivityInstance.class);

        // When
        activityInstanceServiceImpl.setTokenCount(activityInstance, 5);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdate(captor.capture(), anyString());
        assertThat(captor.getValue().getFields().keySet())
                .contains("tokenCount", "lastUpdateDate");
    }

    @Test
    public void should_setAbortedByBoundaryEvent_update_lastUpdateDate() throws Exception {
        // Given
        SActivityInstance activityInstance = mock(SActivityInstance.class);

        // When
        activityInstanceServiceImpl.setAbortedByBoundaryEvent(activityInstance, 789L);

        // Then
        ArgumentCaptor<UpdateRecord> captor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdate(captor.capture(), anyString());
        assertThat(captor.getValue().getFields().keySet())
                .contains("abortedByBoundary", "lastUpdateDate");
    }

}
