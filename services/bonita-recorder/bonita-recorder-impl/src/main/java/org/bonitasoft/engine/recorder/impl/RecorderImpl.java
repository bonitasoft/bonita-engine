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
package org.bonitasoft.engine.recorder.impl;

import java.util.List;

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.BatchInsertRecord;
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
public class RecorderImpl implements Recorder {

    private final TechnicalLoggerService logger;

    private final PersistenceService persistenceService;

    private final EventService eventService;

    public RecorderImpl(final PersistenceService persistenceService, final TechnicalLoggerService logger, final EventService eventService) {
        this.persistenceService = persistenceService;
        this.logger = logger;
        this.eventService = eventService;
    }

    @Override
    public void recordInsert(final InsertRecord record, final SInsertEvent insertEvent) throws SRecorderException {
        final String methodName = "recordInsert";

        traceBeforeMethod(methodName);
        try {
            persistenceService.insert(record.getEntity());
            fireEvent(insertEvent);
            traceAfterMethod(methodName);
        } catch (final SFireEventException e) {
            logFireEventExceptionAndThrowSRecorderException(e, methodName);
        } catch (final Exception e) {
            traceException(methodName, e);
            throw new SRecorderException(e);
        }
    }

    @Override
    public void recordBatchInsert(final BatchInsertRecord record, final SInsertEvent insertEvent) throws SRecorderException {
        final String methodName = "recordBatchInsert";

        traceBeforeMethod(methodName);
        try {
            persistenceService.insertInBatch(record.getEntity());
            fireEvent(insertEvent);
            traceAfterMethod(methodName);
        } catch (final SFireEventException e) {
            logFireEventExceptionAndThrowSRecorderException(e, methodName);
        } catch (final Exception e) {
            traceException(methodName, e);
            throw new SRecorderException(e);
        }
    }

    @Override
    public void recordDelete(final DeleteRecord record, final SDeleteEvent deleteEvent) throws SRecorderException {
        final String methodName = "recordDelete";

        traceBeforeMethod(methodName);
        try {
            persistenceService.delete(record.getEntity());
            fireEvent(deleteEvent);
            traceAfterMethod(methodName);
        } catch (final SFireEventException e) {
            logFireEventExceptionAndThrowSRecorderException(e, methodName);
        } catch (final Exception e) {
            traceException(methodName, e);
            throw new SRecorderException(e);
        }
    }

    @Override
    public void recordDeleteAll(final DeleteAllRecord record) throws SRecorderException {
        final String methodName = "recordDeleteAll";

        traceBeforeMethod(methodName);
        try {
            persistenceService.deleteByTenant(record.getEntityClass(), record.getFilters());
            traceAfterMethod(methodName);
        } catch (final Exception e) {
            traceException(methodName, e);
            throw new SRecorderException(e);
        }
    }

    @Override
    public void recordUpdate(final UpdateRecord record, final SUpdateEvent updateEvent) throws SRecorderException {
        String methodName = "recordUpdate";

        traceBeforeMethod(methodName);
        final UpdateDescriptor desc = UpdateDescriptor.buildSetFields(record.getEntity(), record.getFields());
        try {
            persistenceService.update(desc);
            fireEvent(updateEvent);
            traceAfterMethod(methodName);
        } catch (final SFireEventException e) {
            logFireEventExceptionAndThrowSRecorderException(e, methodName);
        } catch (final Exception e) {
            traceException(methodName, e);
            // FIXME what to do if some handlers fail?
            throw new SRecorderException(e);
        }
    }

    protected void logFireEventExceptionAndThrowSRecorderException(final SFireEventException e, String methodName) throws SRecorderException {
        final List<Exception> handlerExceptions = e.getHandlerExceptions();
        for (Exception exception : handlerExceptions) {
            traceException(methodName, exception);
        }
        throw new SRecorderException(e);
    }

    private void fireEvent(final SEvent evt) throws SFireEventException {
        if (evt != null) {
            eventService.fireEvent(evt);
        }
    }

    private boolean isTraceLoggable() {
        return logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE);
    }

    private void logTrace(final String text) {
        logger.log(this.getClass(), TechnicalLogSeverity.TRACE, text);
    }

    private void traceException(final String methodName, final Exception e) {
        if (isTraceLoggable()) {
            logTrace(LogUtil.getLogOnExceptionMethod(this.getClass(), methodName, e));
        }
    }

    private void traceAfterMethod(final String methodName) {
        if (isTraceLoggable()) {
            logTrace(LogUtil.getLogAfterMethod(this.getClass(), methodName));
        }
    }

    private void traceBeforeMethod(final String methodName) {
        if (isTraceLoggable()) {
            logTrace(LogUtil.getLogBeforeMethod(this.getClass(), methodName));
        }
    }
}
