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
package org.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.business.data.DataRetentionBdmTrackingRepository;
import org.bonitasoft.engine.business.data.SDataRetentionBdmTrackingException;
import org.bonitasoft.engine.business.data.model.SDataRetentionBdmTracking;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.services.SPersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataRetentionBdmTrackingServiceImplTest {

    @Mock
    private DataRetentionBdmTrackingRepository bdmTrackingRepository;

    @InjectMocks
    private DataRetentionBdmTrackingServiceImpl service;

    @Test
    void create_should_insert_tracking_record() throws Exception {
        //when
        service.create(42L, "com.example.Invoice");

        //then
        var captor = ArgumentCaptor.forClass(SDataRetentionBdmTracking.class);
        verify(bdmTrackingRepository).create(captor.capture());
        var tracking = captor.getValue();
        assertThat(tracking.getDataId()).isEqualTo(42L);
        assertThat(tracking.getDataClassname()).isEqualTo("com.example.Invoice");
        assertThat(tracking.getCreatedAt()).isPositive();
        assertThat(tracking.getLastModifiedAt()).isEqualTo(tracking.getCreatedAt());
    }

    @Test
    void create_should_wrap_persistence_exception() throws Exception {
        //given
        doThrow(new SPersistenceException("DB error")).when(bdmTrackingRepository).create(any());

        //when-then
        assertThatThrownBy(() -> service.create(1L, "com.example.Invoice"))
                .isInstanceOf(SDataRetentionBdmTrackingException.class)
                .hasMessageContaining("Failed to create BDM tracking record")
                .hasCauseInstanceOf(SPersistenceException.class);
    }

    @Test
    void upsert_should_update_existing_record() throws Exception {
        //given
        var existing = SDataRetentionBdmTracking.builder()
                .id(1L).dataId(42L).dataClassname("com.example.Invoice")
                .createdAt(1000L).lastModifiedAt(1000L).build();
        when(bdmTrackingRepository.getByDataIdAndClassname(42L, "com.example.Invoice"))
                .thenReturn(existing);

        //when
        service.upsert(42L, "com.example.Invoice");

        //then
        verify(bdmTrackingRepository).updateLastModifiedDate(existing);
        assertThat(existing.getLastModifiedAt()).isGreaterThan(1000L);
        assertThat(existing.getCreatedAt()).isEqualTo(1000L); // unchanged
    }

    @Test
    void upsert_should_create_record_when_missing() throws Exception {
        //given
        when(bdmTrackingRepository.getByDataIdAndClassname(42L, "com.example.Invoice"))
                .thenReturn(null);

        //when
        service.upsert(42L, "com.example.Invoice");

        //then
        var captor = ArgumentCaptor.forClass(SDataRetentionBdmTracking.class);
        verify(bdmTrackingRepository).create(captor.capture());
        var tracking = captor.getValue();
        assertThat(tracking.getDataId()).isEqualTo(42L);
        assertThat(tracking.getDataClassname()).isEqualTo("com.example.Invoice");
    }

    @Test
    void upsert_should_wrap_read_exception() throws Exception {
        //given
        when(bdmTrackingRepository.getByDataIdAndClassname(42L, "com.example.Invoice"))
                .thenThrow(new SBonitaReadException("DB error"));

        //when-then
        assertThatThrownBy(() -> service.upsert(42L, "com.example.Invoice"))
                .isInstanceOf(SDataRetentionBdmTrackingException.class)
                .hasMessageContaining("Failed to upsert data retention tracking record")
                .hasCauseInstanceOf(SBonitaReadException.class);
    }

    @Test
    void updateLastModifiedDate_should_delegate_to_repository() throws Exception {
        //when
        service.updateLastModifiedDate(5L);

        //then
        var captor = ArgumentCaptor.forClass(SDataRetentionBdmTracking.class);
        verify(bdmTrackingRepository).updateLastModifiedDate(captor.capture());
        var tracking = captor.getValue();
        assertThat(tracking.getId()).isEqualTo(5L);
        assertThat(tracking.getLastModifiedAt()).isPositive();
    }

    @Test
    void updateLastModifiedDate_should_wrap_persistence_exception() throws Exception {
        //given
        doThrow(new SPersistenceException("DB error")).when(bdmTrackingRepository).updateLastModifiedDate(any());

        //when-then
        assertThatThrownBy(() -> service.updateLastModifiedDate(5L))
                .isInstanceOf(SDataRetentionBdmTrackingException.class)
                .hasMessageContaining("Failed to update BDM tracking record")
                .hasCauseInstanceOf(SPersistenceException.class);
    }

    @Test
    void delete_should_delegate_to_repository() throws Exception {
        //given
        when(bdmTrackingRepository.delete(42L, "com.example.Invoice")).thenReturn(1);

        //when
        service.delete(42L, "com.example.Invoice");

        //then
        verify(bdmTrackingRepository).delete(42L, "com.example.Invoice");
    }

    @Test
    void delete_should_not_fail_when_no_record_found() throws Exception {
        //given
        when(bdmTrackingRepository.delete(42L, "com.example.Invoice")).thenReturn(0);

        //when-then — should not throw
        assertThatNoException().isThrownBy(() -> service.delete(42L, "com.example.Invoice"));
        verify(bdmTrackingRepository).delete(42L, "com.example.Invoice");
    }

    @Test
    void delete_should_wrap_persistence_exception() throws Exception {
        //given
        when(bdmTrackingRepository.delete(42L, "com.example.Invoice"))
                .thenThrow(new SPersistenceException("DB error"));

        //when-then
        assertThatThrownBy(() -> service.delete(42L, "com.example.Invoice"))
                .isInstanceOf(SDataRetentionBdmTrackingException.class)
                .hasMessageContaining("Failed to delete data retention tracking record")
                .hasCauseInstanceOf(SPersistenceException.class);
    }

    @Test
    void deleteAll_should_delegate_to_repository() throws Exception {
        //when
        service.deleteAll();

        //then
        verify(bdmTrackingRepository).deleteAll(any());
    }

    @Test
    void deleteAll_should_wrap_persistence_exception() throws Exception {
        //given
        doThrow(new SPersistenceException("DB error")).when(bdmTrackingRepository).deleteAll(any());

        //when-then
        assertThatThrownBy(() -> service.deleteAll())
                .isInstanceOf(SDataRetentionBdmTrackingException.class)
                .hasMessageContaining("Failed to delete all BDM tracking records")
                .hasCauseInstanceOf(SPersistenceException.class);
    }
}
