/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.BatchInsertRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.UpdateDescriptor;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
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
            this.persistenceService.insert(record.getEntity());
            fireEvent(insertEvent);
            traceAfterMethod(methodName);
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
            this.persistenceService.insertInBatch(record.getEntity());
            fireEvent(insertEvent);
            traceAfterMethod(methodName);
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
            this.persistenceService.delete(record.getEntity());
            fireEvent(deleteEvent);
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
            this.persistenceService.update(desc);
            fireEvent(updateEvent);
            traceAfterMethod(methodName);
        } catch (final Exception e) {
            traceException(methodName, e);
            // FIXME what to do if some handlers fail?
            throw new SRecorderException(e);
        }
    }

    private void fireEvent(SEvent evt) throws FireEventException {
        if (evt != null) {
            this.eventService.fireEvent(evt);
        }
    }

    private boolean isTraceLoggable() {
        return this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE);
    }
    
    private void logTrace(String text) {
        this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, text);
    }
    
    private void traceException(String methodName, final Exception e) {
        if (isTraceLoggable()) {
            logTrace(LogUtil.getLogOnExceptionMethod(this.getClass(), methodName, e));
        }
    }

    private void traceAfterMethod(String methodName) {
        if (isTraceLoggable()) {
            logTrace(LogUtil.getLogAfterMethod(this.getClass(), methodName));
        }
    }

    private void traceBeforeMethod(String methodName) {
        if (isTraceLoggable()) {
            logTrace(LogUtil.getLogBeforeMethod(this.getClass(), methodName));
        }
    }
}
