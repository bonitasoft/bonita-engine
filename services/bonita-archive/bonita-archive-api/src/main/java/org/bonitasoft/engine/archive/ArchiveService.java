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
package org.bonitasoft.engine.archive;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface ArchiveService {

    /**
     * Archive the given entity in sliding archive if relevant and in the appropriate definitive archive
     * 
     * @param time
     *            The archive date
     * @param record
     *            Archive insert record containing the entity to be archived
     * @throws SRecorderException
     */
    void recordInsert(long time, ArchiveInsertRecord record) throws SRecorderException;

    /**
     * Archive the given entities in the definitive archive
     *
     * @param time
     *            the time of archiving
     * @param records
     *            Archive inserts record containing the entity to be archived
     * @throws SRecorderException
     *             in case of a write error
     */
    void recordInserts(long time, ArchiveInsertRecord... records) throws SRecorderException;

    /**
     * Remove the given entity from both sliding archive (if present) and the right archive level (if present)
     * This operation should normally to be used. This is for admin purpose only
     * 
     * @param record
     *            The delete record containing archived entity to be deleted
     * @throws SRecorderException
     */
    void recordDelete(DeleteRecord record) throws SRecorderException;

    /**
     * Get the ReadPersistenceService corresponding to the definitive archive
     * 
     * @return the ReadPersistenceService corresponding to the definitive archive
     */
    ReadPersistenceService getDefinitiveArchiveReadPersistenceService();

    /**
     * @param sourceObjectClass
     *            Persistent object to be judged achievable or not
     * @return Return true if the objects of the given class can be archived.
     */
    boolean isArchivable(Class<? extends PersistentObject> sourceObjectClass);

}
