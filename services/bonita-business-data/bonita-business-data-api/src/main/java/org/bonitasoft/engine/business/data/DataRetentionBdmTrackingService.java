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

/**
 * Service that manages data retention tracking records for BDM entity instances.
 * <p>
 * Each tracking record stores the creation and last-modification timestamps of a
 * BDM object instance. These timestamps are consumed by the data retention service
 * to determine whether a BDM object has exceeded its configured retention period.
 * <p>
 * This interface lives in the API module so that it can be injected by modules
 * (e.g. {@code bonita-process-engine}) that do not depend on {@code bonita-persistence}.
 * The implementation delegates to {@code DataRetentionBdmTrackingRepository} in the
 * impl module, wrapping persistence exceptions into {@link SDataRetentionBdmTrackingException}.
 */
public interface DataRetentionBdmTrackingService {

    /**
     * Persists a new tracking record.
     *
     * @param dataId the persistence ID of the tracked BDM object instance
     * @param dataClassname the fully qualified class name of the BDM object type
     * @throws SDataRetentionBdmTrackingException if the insert fails
     */
    void create(long dataId, String dataClassname) throws SDataRetentionBdmTrackingException;

    /**
     * Upserts the {@code last_modified_at} timestamp for a BDM entity in the
     * data retention tracking table.
     * <p>If a tracking record already exists for the given {@code dataId} and
     * {@code dataClassname}, its {@code lastModifiedAt} is updated to the current time.
     * Otherwise, a new tracking record is created.
     *
     * @param dataId the persistence ID of the BDM entity
     * @param dataClassname the fully qualified Java class name of the BDM entity
     * @throws SDataRetentionBdmTrackingException if the upsert fails
     */
    void upsert(long dataId, String dataClassname) throws SDataRetentionBdmTrackingException;

    /**
     * Updates the {@code last_modified_at} column of an existing tracking record.
     *
     * @param id the ID of the tracking record to update
     * @throws SDataRetentionBdmTrackingException if the update fails
     */
    void updateLastModifiedDate(long id) throws SDataRetentionBdmTrackingException;

    /**
     * Deletes <b>ALL</b> tracking records.
     *
     * @throws SDataRetentionBdmTrackingException if the delete operation fails
     */
    void deleteAll() throws SDataRetentionBdmTrackingException;

}
