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
package org.bonitasoft.engine.recorder;

import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.recorder.model.BatchInsertRecord;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;

public interface Recorder {

    /**
     * Add a record to database
     * 
     * @param record
     *            the record for insert
     * @param insertEvent
     *            the event for insert
     * @throws SRecorderException
     * @since 6.0
     */
    void recordInsert(InsertRecord record, SInsertEvent insertEvent) throws SRecorderException;

    /**
     * Delete a record from database
     * 
     * @param record
     *            the record for insert
     * @param deleteEvent
     *            the event for delete
     * @throws SRecorderException
     * @since 6.0
     */
    void recordDelete(DeleteRecord record, SDeleteEvent deleteEvent) throws SRecorderException;

    /**
     * Update a record from database
     * 
     * @param record
     *            the record for insert
     * @param updateEvent
     *            the event for update
     * @throws SRecorderException
     * @since 6.0
     */
    void recordUpdate(UpdateRecord record, SUpdateEvent updateEvent) throws SRecorderException;

    /**
     * @param record
     * @param insertEvent
     * @throws SRecorderException
     */
    void recordBatchInsert(BatchInsertRecord record, SInsertEvent insertEvent) throws SRecorderException;

    /**
     * Delete all records for a table from database, for the connected tenant
     * 
     * @param record
     *            table to clean
     * @throws SRecorderException
     * @since 6.1
     */
    void recordDeleteAll(DeleteAllRecord record) throws SRecorderException;

}
