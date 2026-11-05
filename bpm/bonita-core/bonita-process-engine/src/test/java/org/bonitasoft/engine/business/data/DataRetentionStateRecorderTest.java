/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataRetentionStateRecorderTest {

    @Mock
    private QueriableLoggerService queriableLoggerService;

    private DataRetentionStateRecorder recorder(boolean enabled) {
        return new DataRetentionStateRecorder(new DataRetentionSettings(enabled), queriableLoggerService);
    }

    private void lastRecordedState(String state) throws SBonitaReadException {
        List<SQueriableLog> logs = state == null ? List.of()
                : List.of(SQueriableLog.builder().actionType(DataRetentionStateRecorder.ACTION_TYPE)
                        .actionScope(state).build());
        doReturn(logs).when(queriableLoggerService).searchLogs(any(QueryOptions.class));
    }

    private SQueriableLog recordedLog() {
        var captor = ArgumentCaptor.forClass(SQueriableLog.class);
        verify(queriableLoggerService).log(anyString(), anyString(), captor.capture());
        return captor.getValue();
    }

    @Test
    void should_record_disabled_state_when_disabled_and_nothing_recorded_yet() throws Exception {
        //given
        lastRecordedState(null);

        //when
        recorder(false).init();

        //then
        var log = recordedLog();
        assertThat(log.getActionType()).isEqualTo("DATA_RETENTION_STATE_UPDATED");
        assertThat(log.getActionScope()).isEqualTo(DataRetentionStateRecorder.STATE_DISABLED);
        assertThat(log.getSeverity()).isEqualTo(SQueriableLogSeverity.INTERNAL);
        assertThat(log.getActionStatus()).isEqualTo(SQueriableLog.STATUS_OK);
        assertThat(log.getRawMessage()).contains("bonita.runtime.retention.enabled=false");
    }

    @Test
    void should_record_enabled_state_when_enabled_and_nothing_recorded_yet() throws Exception {
        //given
        lastRecordedState(null);

        //when
        recorder(true).init();

        //then
        var log = recordedLog();
        assertThat(log.getActionScope()).isEqualTo(DataRetentionStateRecorder.STATE_ENABLED);
        assertThat(log.getRawMessage()).contains("bonita.runtime.retention.enabled=true");
    }

    @Test
    void should_not_record_anything_when_state_is_unchanged() throws Exception {
        //given
        lastRecordedState(DataRetentionStateRecorder.STATE_DISABLED);

        //when
        recorder(false).init();

        //then
        verify(queriableLoggerService, never()).log(anyString(), anyString(), any(SQueriableLog.class));
    }

    @Test
    void should_record_disabled_state_when_previously_enabled() throws Exception {
        //given
        lastRecordedState(DataRetentionStateRecorder.STATE_ENABLED);

        //when
        recorder(false).init();

        //then
        assertThat(recordedLog().getActionScope()).isEqualTo(DataRetentionStateRecorder.STATE_DISABLED);
    }

    @Test
    void should_record_enabled_state_when_previously_disabled() throws Exception {
        //given
        lastRecordedState(DataRetentionStateRecorder.STATE_DISABLED);

        //when
        recorder(true).init();

        //then
        var log = recordedLog();
        assertThat(log.getActionScope()).isEqualTo(DataRetentionStateRecorder.STATE_ENABLED);
        assertThat(log.getRawMessage()).contains("bonita.runtime.retention.enabled=true");
    }

    @Test
    void should_not_fail_startup_when_queriable_log_cannot_be_read() throws Exception {
        //given
        doThrow(new SBonitaReadException("db down")).when(queriableLoggerService)
                .searchLogs(any(QueryOptions.class));

        //when-then
        assertThatNoException().isThrownBy(() -> recorder(false).init());
        verify(queriableLoggerService, never()).log(anyString(), anyString(), any(SQueriableLog.class));
    }
}
