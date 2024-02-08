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
package org.bonitasoft.engine.recorder.impl;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.UpdateDescriptor;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
@Slf4j
public class RecorderImpl implements Recorder {

    private final PersistenceService persistenceService;

    private final EventService eventService;

    public RecorderImpl(final PersistenceService persistenceService,
            final EventService eventService) {
        this.persistenceService = persistenceService;
        this.eventService = eventService;
    }

    @Override
    public void recordInsert(final InsertRecord record, String type) throws SRecorderException {
        try {
            persistenceService.insert(record.getEntity());
            eventService.fireEvent(createInsertEvent(record.getEntity(), type));
        } catch (final Exception e) {
            logExceptionsFromHandlers(e);
            throw new SRecorderException(e);
        }
    }

    private SInsertEvent createInsertEvent(PersistentObject entity, String type) {
        SInsertEvent sInsertEvent = new SInsertEvent(type + SEvent.CREATED);
        sInsertEvent.setObject(entity);
        return sInsertEvent;
    }

    private SDeleteEvent createDeleteEvent(PersistentObject entity, String type) {
        SDeleteEvent sDeleteEvent = new SDeleteEvent(type + SEvent.DELETED);
        sDeleteEvent.setObject(entity);
        return sDeleteEvent;
    }

    private SUpdateEvent createUpdateEvent(PersistentObject entity, Map<String, Object> updatedFields, String type) {
        SUpdateEvent sUpdateEvent = new SUpdateEvent(type + SEvent.UPDATED);
        sUpdateEvent.setObject(entity);
        sUpdateEvent.setUpdatedFields(updatedFields);
        return sUpdateEvent;
    }

    @Override
    public void recordDelete(final DeleteRecord record, String type) throws SRecorderException {
        try {
            persistenceService.delete(record.getEntity());
            eventService.fireEvent(createDeleteEvent(record.getEntity(), type));
        } catch (final Exception e) {
            logExceptionsFromHandlers(e);
            throw new SRecorderException(e);
        }
    }

    @Override
    public void recordDeleteAll(final DeleteAllRecord record) throws SRecorderException {
        try {
            persistenceService.deleteByTenant(record.getEntityClass(), record.getFilters());
        } catch (final Exception e) {
            logExceptionsFromHandlers(e);
            throw new SRecorderException(e);
        }
    }

    @Override
    public void recordUpdate(final UpdateRecord record, String type) throws SRecorderException {
        final UpdateDescriptor desc = UpdateDescriptor.buildSetFields(record.getEntity(), record.getFields());
        try {
            persistenceService.update(desc);
            eventService.fireEvent(createUpdateEvent(record.getEntity(), record.getFields(), type));
        } catch (final Exception e) {
            logExceptionsFromHandlers(e);
            throw new SRecorderException(e);
        }
    }

    @Override
    public int recordUpdateWithQuery(final UpdateRecord record, String type, String query) throws SRecorderException {

        try {
            int updateCount = persistenceService.update(query, record.getFields());
            if (updateCount > 0)
                eventService.fireEvent(createUpdateEvent(record.getEntity(), record.getFields(), type));
            return updateCount;
        } catch (final Exception e) {
            logExceptionsFromHandlers(e);
            throw new SRecorderException(e);
        }
    }

    private void logExceptionsFromHandlers(final Exception e) {
        if (!(e instanceof SFireEventException)) {
            return;
        }
        if (log.isDebugEnabled()) {
            final List<Exception> handlerExceptions = ((SFireEventException) e).getHandlerExceptions();
            for (Exception exception : handlerExceptions) {
                log.debug("error while executing handler e {}", ExceptionUtils.printLightWeightStacktrace(exception));
            }
        }
    }

}
