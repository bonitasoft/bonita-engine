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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
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
        verify(recorder).recordUpdateWithQuery(updateRecordCaptor.capture(), anyString(), anyString());
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
        // When
        service.setTaskPriority(flowNodeInstance, STaskPriority.HIGHEST);

        // Then
        verify(recorder).recordUpdateWithQuery(updateRecordCaptor.capture(), anyString(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("priority", "lastUpdateDate");
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
        verify(recorder).recordUpdateWithQuery(updateRecordCaptor.capture(), anyString(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("executedBy", "lastUpdateDate");
    }

    @Test
    public void should_setExecutedBySubstitute_update_lastUpdateDate() throws Exception {
        // When
        service.setExecutedBySubstitute(flowNodeInstance, 456L);

        // Then
        verify(recorder).recordUpdateWithQuery(updateRecordCaptor.capture(), anyString(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("executedBySubstitute", "lastUpdateDate");
    }

    @Test
    public void should_setExpectedEndDate_update_lastUpdateDate() throws Exception {
        // When
        service.setExpectedEndDate(flowNodeInstance, 123456789L);

        // Then
        verify(recorder).recordUpdateWithQuery(updateRecordCaptor.capture(), anyString(), anyString());
        assertThat(updateRecordCaptor.getValue().getFields().keySet())
                .contains("expectedEndDate", "lastUpdateDate");
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
