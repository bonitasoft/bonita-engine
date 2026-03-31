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

import java.util.List;

import org.bonitasoft.engine.business.data.model.SDataRetentionBdmTracking;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * Repository for CRUD operations on {@link SDataRetentionBdmTracking} entities.
 * <p>
 * Tracking records are stored in the Bonita DB and record the creation and last
 * modification timestamps of BDM object instances for data retention purposes.
 */
public interface DataRetentionBdmTrackingRepository {

    /**
     * Persists a new tracking record.
     *
     * @param tracking the tracking record to create
     * @throws SPersistenceException if the insert fails
     */
    void create(SDataRetentionBdmTracking tracking) throws SPersistenceException;

    /**
     * Updates the {@code last_modified_at} column of an existing tracking record.
     *
     * @param tracking the tracking record whose {@code lastModifiedAt} has been set to the new value
     * @throws SPersistenceException if the update fails
     */
    void updateLastModifiedDate(SDataRetentionBdmTracking tracking) throws SPersistenceException;

    /**
     * Finds a tracking record by BDM object identifier and class name.
     *
     * @param dataId the persistence ID of the tracked BDM object instance
     * @param dataClassname the fully qualified class name of the BDM object type
     * @return the matching tracking record, or {@code null} if not found
     * @throws SBonitaReadException if the read operation fails
     */
    SDataRetentionBdmTracking getByDataIdAndClassname(long dataId, String dataClassname) throws SBonitaReadException;

    /**
     * Returns all tracking records for the given BDM class name.
     *
     * @param dataClassname the fully qualified class name of the BDM object type
     * @return the list of matching tracking records, or an empty list if none found
     * @throws SBonitaReadException if the read operation fails
     */
    List<SDataRetentionBdmTracking> getByClassname(String dataClassname) throws SBonitaReadException;

    /**
     * Deletes all tracking records matching the given filter options.
     *
     * @param filterOptions the filter criteria to restrict the deletion.
     *        <b>Warning:</b> passing an empty list deletes ALL tracking records
     * @throws SPersistenceException if the delete operation fails
     */
    void deleteAll(List<FilterOption> filterOptions) throws SPersistenceException;
}
