/**
 * Copyright (C) 2025 Bonitasoft S.A.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BusinessDataRepositoryEventAspectTest {

    private EventService eventService;
    private BusinessDataRepositoryEventAspect aspect;
    private Entity entity;

    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        aspect = new BusinessDataRepositoryEventAspect(eventService);
        entity = mock(Entity.class);
    }

    @Test
    void should_fire_insert_event_on_persist_when_id_is_null() throws Throwable {
        when(entity.getPersistenceId()).thenReturn(null);
        Object dummyResult = new Object();
        var joinPoint = mock(org.aspectj.lang.ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(dummyResult);

        Object result = aspect.aroundPersist(joinPoint, entity);

        ArgumentCaptor<SInsertEvent> captor = ArgumentCaptor.forClass(SInsertEvent.class);
        verify(eventService).fireEvent(captor.capture());
        SInsertEvent event = captor.getValue();
        assertThat(event.getType()).isEqualTo("BUSINESS_DATA_CREATED");
        assertThat(event.getObject()).isEqualTo(entity);
        assertThat(result).isSameAs(dummyResult);
    }

    @Test
    void should_fire_update_event_on_persist_when_id_is_not_null() throws Throwable {
        when(entity.getPersistenceId()).thenReturn(123L);
        Object dummyResult = new Object();
        var joinPoint = mock(org.aspectj.lang.ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(dummyResult);

        Object result = aspect.aroundPersist(joinPoint, entity);

        ArgumentCaptor<SUpdateEvent> captor = ArgumentCaptor.forClass(SUpdateEvent.class);
        verify(eventService).fireEvent(captor.capture());
        SUpdateEvent event = captor.getValue();
        assertThat(event.getType()).isEqualTo("BUSINESS_DATA_UPDATED");
        assertThat(event.getObject()).isEqualTo(entity);
        assertThat(result).isSameAs(dummyResult);
    }

    @Test
    void should_fire_delete_event_on_remove() throws Exception {
        aspect.afterRemove(entity);
        ArgumentCaptor<SDeleteEvent> captor = ArgumentCaptor.forClass(SDeleteEvent.class);
        verify(eventService).fireEvent(captor.capture());
        SDeleteEvent event = captor.getValue();
        assertThat(event.getType()).isEqualTo("BUSINESS_DATA_DELETED");
        assertThat(event.getObject()).isEqualTo(entity);
    }

    @Test
    void should_fire_delete_event_on_removeById() throws Exception {
        aspect.afterRemoveById(entity);
        ArgumentCaptor<SDeleteEvent> captor = ArgumentCaptor.forClass(SDeleteEvent.class);
        verify(eventService).fireEvent(captor.capture());
        SDeleteEvent event = captor.getValue();
        assertThat(event.getType()).isEqualTo("BUSINESS_DATA_DELETED");
        assertThat(event.getObject()).isEqualTo(entity);
    }

    @Test
    void should_fire_insert_event_on_merge_when_id_is_null() throws Throwable {
        when(entity.getPersistenceId()).thenReturn(null);
        Object dummyResult = new Object();
        var joinPoint = mock(org.aspectj.lang.ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(dummyResult);

        Object result = aspect.aroundMerge(joinPoint, entity);

        ArgumentCaptor<SInsertEvent> captor = ArgumentCaptor.forClass(SInsertEvent.class);
        verify(eventService).fireEvent(captor.capture());
        SInsertEvent event = captor.getValue();
        assertThat(event.getType()).isEqualTo("BUSINESS_DATA_CREATED");
        assertThat(event.getObject()).isEqualTo(entity);
        assertThat(result).isSameAs(dummyResult);
    }

    @Test
    void should_fire_update_event_on_merge_when_id_is_not_null() throws Throwable {
        when(entity.getPersistenceId()).thenReturn(123L);
        Object dummyResult = new Object();
        var joinPoint = mock(org.aspectj.lang.ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(dummyResult);

        Object result = aspect.aroundMerge(joinPoint, entity);

        ArgumentCaptor<SUpdateEvent> captor = ArgumentCaptor.forClass(SUpdateEvent.class);
        verify(eventService).fireEvent(captor.capture());
        SUpdateEvent event = captor.getValue();
        assertThat(event.getType()).isEqualTo("BUSINESS_DATA_UPDATED");
        assertThat(event.getObject()).isEqualTo(entity);
        assertThat(result).isSameAs(dummyResult);
    }
}
