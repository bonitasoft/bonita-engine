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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FlowNodeInstancesServiceImplTest {

    @Mock
    private Recorder recorder;

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private ArchiveService archiveService;

    @Mock
    private SFlowNodeInstance flowNodeInstance;

    @Mock
    private FlowNodeState flowNodeState;

    @Captor
    private ArgumentCaptor<UpdateRecord> updateRecordCaptor;

    private ActivityInstanceServiceImpl service;

    @Before
    public void setUp() {
        service = new ActivityInstanceServiceImpl(recorder, persistenceService, archiveService);
    }

    // === Tests for setLastUpdateDate helper method behavior ===

    @Test
    public void should_setState_update_both_reachStateDate_and_lastUpdateDate() throws Exception {
        // Given
        when(flowNodeState.getId()).thenReturn(1);
        when(flowNodeState.getName()).thenReturn("ready");
        when(flowNodeState.isStable()).thenReturn(true);
        when(flowNodeState.isTerminal()).thenReturn(false);

        // When
        service.setState(flowNodeInstance, flowNodeState);

        // Then
        verify(recorder).recordUpdate(updateRecordCaptor.capture(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("stateId", "reachedStateDate", "lastUpdateDate");
    }

    @Test
    public void should_setExecuting_update_lastUpdateDate() throws Exception {
        // When
        service.setExecuting(flowNodeInstance);

        // Then
        verify(recorder).recordUpdate(updateRecordCaptor.capture(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("stateExecuting", "lastUpdateDate");
    }

    @Test
    public void should_updateDisplayName_update_lastUpdateDate() throws Exception {
        // Given
        when(flowNodeInstance.getDisplayName()).thenReturn("oldName");

        // When
        service.updateDisplayName(flowNodeInstance, "newName");

        // Then
        verify(recorder).recordUpdate(updateRecordCaptor.capture(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("displayName", "lastUpdateDate");
    }

    @Test
    public void should_updateDisplayDescription_update_lastUpdateDate() throws Exception {
        // Given
        when(flowNodeInstance.getDisplayDescription()).thenReturn("oldDesc");

        // When
        service.updateDisplayDescription(flowNodeInstance, "newDesc");

        // Then
        verify(recorder).recordUpdate(updateRecordCaptor.capture(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("displayDescription", "lastUpdateDate");
    }

    @Test
    public void should_setTaskPriority_update_lastUpdateDate() throws Exception {
        // Given
        when(flowNodeInstance.getTenantId()).thenReturn(101L);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.ACTIVITYINSTANCE_STATE),
                eq("updateHumanTaskPriority"))).thenReturn(1);

        // When
        service.setTaskPriority(flowNodeInstance, STaskPriority.HIGHEST);

        // Then
        verify(recorder).recordUpdateWithQuery(updateRecordCaptor.capture(),
                eq(FlowNodeInstanceService.ACTIVITYINSTANCE_STATE),
                eq("updateHumanTaskPriority"));
        // The update must stay minimal: the composite identifier (id + tenantId, the WHERE clause of the
        // atomic query) plus only the columns actually written. A whole-row recordUpdate would carry ~30
        // fields and fail here -- that is the lost update of BPA-167. Dropping tenantId would also fail
        // here, and would leave MySQL scanning and locking the whole table.
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .containsExactlyInAnyOrder("id", "tenantId", "priority", "lastUpdateDate");
        // A migrated customer's tenant id is NOT 1: installations that ran multi-tenant before 8.0.0 kept
        // their original id when their databases were split (e.g. 101). Binding a literal here would break
        // exactly those customers, so the value is asserted, not just the key.
        assertThat(updateRecordCaptor.getValue().getFields())
                .as("tenantId must be the value read from the flow node, never a literal")
                .containsEntry("tenantId", 101L);
        // Writer 2 deliberately does NOT refresh: its only caller (SetTaskPriority) neither re-reads nor
        // mutates the entity afterwards, and a gratuitous refresh would discard un-flushed mutations on it.
        // Pinned here so the asymmetry with setExpectedEndDate stays a decision rather than an oversight.
        verify(persistenceService, never()).refresh(flowNodeInstance);
    }

    @Test
    public void should_setStateCategory_update_lastUpdateDate() throws Exception {
        // When
        service.setStateCategory(flowNodeInstance, SStateCategory.ABORTING);

        // Then
        verify(recorder).recordUpdate(updateRecordCaptor.capture(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("stateCategory", "lastUpdateDate");
    }

    @Test
    public void should_setExecutedBy_update_lastUpdateDate() throws Exception {
        // When
        service.setExecutedBy(flowNodeInstance, 123L);

        // Then
        verify(recorder).recordUpdate(updateRecordCaptor.capture(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("executedBy", "lastUpdateDate");
    }

    @Test
    public void should_setExecutedBySubstitute_update_lastUpdateDate() throws Exception {
        // When
        service.setExecutedBySubstitute(flowNodeInstance, 456L);

        // Then
        verify(recorder).recordUpdate(updateRecordCaptor.capture(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("executedBySubstitute", "lastUpdateDate");
    }

    @Test
    public void should_setExpectedEndDate_update_lastUpdateDate() throws Exception {
        // Given
        when(flowNodeInstance.getTenantId()).thenReturn(101L);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.EXPECTED_END_DATE_MODIFIED),
                eq("updateHumanTaskExpectedEndDate"))).thenReturn(1);

        // When
        service.setExpectedEndDate(flowNodeInstance, 123456789L);

        // Then
        verify(recorder).recordUpdateWithQuery(updateRecordCaptor.capture(),
                eq(FlowNodeInstanceService.EXPECTED_END_DATE_MODIFIED),
                eq("updateHumanTaskExpectedEndDate"));
        // Minimality, as above: id + tenantId + the written columns only.
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .containsExactlyInAnyOrder("id", "tenantId", "expectedEndDate", "lastUpdateDate");
        assertThat(updateRecordCaptor.getValue().getFields())
                .as("tenantId must be the value read from the flow node, never a literal")
                .containsEntry("tenantId", 101L);
        // The bulk HQL update bypasses the managed entity, so the in-memory copy is stale. It must be
        // refreshed before the state machine re-reads it or re-dirties it in the same transaction --
        // otherwise the commit flush writes the stale snapshot back (BPA-167).
        verify(persistenceService).refresh(flowNodeInstance);
    }

    @Test
    public void should_setExpectedEndDate_bind_null_when_the_due_date_is_removed() throws Exception {
        // Given: null means "no expected end date" (StateBehaviors.updateExpectedDuration, SetExpectedEndDate)
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.EXPECTED_END_DATE_MODIFIED),
                eq("updateHumanTaskExpectedEndDate"))).thenReturn(1);

        // When
        service.setExpectedEndDate(flowNodeInstance, null);

        // Then the null is passed to the query as such, and the entity is still refreshed
        verify(recorder).recordUpdateWithQuery(updateRecordCaptor.capture(),
                eq(FlowNodeInstanceService.EXPECTED_END_DATE_MODIFIED),
                eq("updateHumanTaskExpectedEndDate"));
        assertThat(updateRecordCaptor.getValue().getFields()).containsEntry("expectedEndDate", null);
        verify(persistenceService).refresh(flowNodeInstance);
    }

    // === Tests for the fail-loud row-count guard on the atomic updates (BPA-167) ===

    @Test
    public void should_setTaskPriority_throw_retryable_when_a_human_task_row_matched_nothing() throws Exception {
        // Given a HUMAN TASK whose atomic update matched no row: the only way that happens is that the row was
        // archived or deleted after this transaction read it, which is transient. The work retryability
        // evaluator walks the whole cause chain and classifies a message-only exception NOT_RETRYABLE, which
        // fails the flow node instead of retrying it -- a regression against the pre-BPA-167 behaviour, where
        // Hibernate's own StaleStateException (a retryable class) surfaced here.
        SHumanTaskInstance humanTask = mock(SHumanTaskInstance.class);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.ACTIVITYINSTANCE_STATE),
                eq("updateHumanTaskPriority"))).thenReturn(0);

        // When / Then the guard must fail loud, and stay retryable
        assertThatThrownBy(() -> service.setTaskPriority(humanTask, STaskPriority.HIGHEST))
                .isInstanceOf(SFlowNodeModificationException.class)
                .hasCauseInstanceOf(SRetryableException.class);
    }

    @Test
    public void should_setTaskPriority_throw_without_retry_when_the_flow_node_is_not_a_human_task() throws Exception {
        // Given any OTHER flow node type -- this method accepts any SFlowNodeInstance from the public API. The
        // human-task query can never match it, so the failure is permanent and must fail fast: marking it
        // retryable would spend the work's 10 attempts with a doubling delay (about 17 minutes) on something
        // that cannot succeed, and fail the flow node anyway.
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.ACTIVITYINSTANCE_STATE),
                eq("updateHumanTaskPriority"))).thenReturn(0);

        // When / Then
        assertThatThrownBy(() -> service.setTaskPriority(flowNodeInstance, STaskPriority.HIGHEST))
                .isInstanceOf(SFlowNodeModificationException.class)
                .hasNoCause();
    }

    @Test
    public void should_setExpectedEndDate_throw_when_no_row_was_updated() throws Exception {
        // Given the atomic update matched no row. This is the guard most likely to be reached in
        // production: unlike assignHumanTask, setExpectedEndDate accepts any flow node id from the
        // public API, so a non-human-task id reaches it and matches zero rows.
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.EXPECTED_END_DATE_MODIFIED),
                eq("updateHumanTaskExpectedEndDate"))).thenReturn(0);

        // When / Then the guard must fail loud, never swallow and continue
        assertThatThrownBy(() -> service.setExpectedEndDate(flowNodeInstance, 123456789L))
                .isInstanceOf(SFlowNodeModificationException.class)
                // Not a human task, so the human-task query can never match: permanent, must not be retried.
                .hasNoCause();
    }

    @Test
    public void should_setExpectedEndDate_throw_retryable_when_a_human_task_row_matched_nothing() throws Exception {
        // Given a HUMAN TASK whose row vanished between the read and the update: transient, must stay retryable
        // so a work-borne caller retries instead of failing the flow node.
        SHumanTaskInstance humanTask = mock(SHumanTaskInstance.class);
        when(recorder.recordUpdateWithQuery(any(UpdateRecord.class),
                eq(FlowNodeInstanceService.EXPECTED_END_DATE_MODIFIED),
                eq("updateHumanTaskExpectedEndDate"))).thenReturn(0);

        // When / Then
        assertThatThrownBy(() -> service.setExpectedEndDate(humanTask, 123456789L))
                .isInstanceOf(SFlowNodeModificationException.class)
                .hasCauseInstanceOf(SRetryableException.class);
    }

    // === Tests for timestamp reuse behavior ===

    @Test
    public void should_setState_use_same_timestamp_for_reachStateDate_and_lastUpdateDate() throws Exception {
        // Given
        when(flowNodeState.getId()).thenReturn(1);
        when(flowNodeState.getName()).thenReturn("ready");
        when(flowNodeState.isStable()).thenReturn(true);
        when(flowNodeState.isTerminal()).thenReturn(false);

        // When
        service.setState(flowNodeInstance, flowNodeState);

        // Then
        verify(recorder).recordUpdate(updateRecordCaptor.capture(), anyString());
        Map<String, Object> fields = updateRecordCaptor.getValue().getFields();
        Long reachedStateDate = (Long) fields.get("reachedStateDate");
        Long lastUpdateDate = (Long) fields.get("lastUpdateDate");
        assertThat(lastUpdateDate).isNotNull();
        assertThat(lastUpdateDate).isEqualTo(reachedStateDate);
    }
}
