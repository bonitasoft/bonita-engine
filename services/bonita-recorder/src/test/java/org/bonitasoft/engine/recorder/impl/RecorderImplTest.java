/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.recorder.impl;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.UUID;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta.
 */
@RunWith(MockitoJUnitRunner.class)
public class RecorderImplTest {

    @Mock
    private TechnicalLoggerService technicalLoggerService;
    @Mock
    private PersistenceService persistenceService;
    @Mock
    private EventService eventService;
    @InjectMocks
    private RecorderImpl recorder;

    @Test
    public void should_fire_event_when_recording_an_insert() throws Exception {
        MyPersistentObject entity = entity();
        recorder.recordInsert(insertRecord(entity), "theEvent");

        verify(eventService).fireEvent(argThat(match("theEvent_CREATED", entity)));
    }

    @Test
    public void should_fire_event_when_recording_an_update() throws Exception {
        MyPersistentObject entity = entity();
        recorder.recordUpdate(updateRecord(entity), "theEvent");

        verify(eventService).fireEvent(argThat(match("theEvent_UPDATED", entity)));
    }

    @Test
    public void should_fire_event_when_recording_a_delete() throws Exception {
        MyPersistentObject entity = entity();
        recorder.recordDelete(deleteRecord(entity), "theEvent");

        verify(eventService).fireEvent(argThat(match("theEvent_DELETED", entity)));
    }

    protected ArgumentMatcher<SEvent> match(String type, Object entity) {
        return sEvent -> sEvent.getType().equals(type) && sEvent.getObject().equals(entity);
    }

    private InsertRecord insertRecord(PersistentObject entity) {
        return new InsertRecord(entity);
    }

    private UpdateRecord updateRecord(PersistentObject entity) {
        return UpdateRecord.buildSetFields(entity, Collections.emptyMap());
    }

    private DeleteRecord deleteRecord(PersistentObject entity) {
        return new DeleteRecord(entity);
    }


    private MyPersistentObject entity() {
        return new MyPersistentObject();
    }

    private static class MyPersistentObject implements PersistentObject {

        private long id = UUID.randomUUID().getLeastSignificantBits();

        @Override
        public long getId() {
            return id;
        }

        @Override
        public String getDiscriminator() {
            return null;
        }

        @Override
        public void setId(long id) {
        }

        @Override
        public void setTenantId(long id) {
        }
    }
}
