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
     * @param type
     * @throws SRecorderException
     * @since 6.0
     */
    void recordInsert(InsertRecord record, String type) throws SRecorderException;

    /**
     * Delete a record from database
     * 
     * @param record
     *            the record for insert
     * @param type
     * @throws SRecorderException
     * @since 6.0
     */
    void recordDelete(DeleteRecord record, String type) throws SRecorderException;

    /**
     * Update a record from database
     * 
     * @param record
     *            the record for insert
     * @param type
     * @throws SRecorderException
     * @since 6.0
     */
    void recordUpdate(UpdateRecord record, String type) throws SRecorderException;

    /**
     * Update a record from database with a named query
     * If no rows have been updated the event is not thrown
     *
     * @param record
     *              the record for insert
     * @param type
     *              Object type
     * @param query
     *              NamedQuery to be used
     * @return number of updated rows
     * @throws SRecorderException
     * @since 7.6
     */
    int recordUpdateWithQuery(final UpdateRecord record, String type, String query) throws SRecorderException;

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
