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
package org.bonitasoft.engine.business.data.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * Persistent entity that tracks the creation and last modification timestamps
 * of BDM object instances for data retention purposes.
 * <p>Each row records when a specific BDM object instance (identified by its
 * {@code dataId} and {@code dataClassname}) was created and last modified.
 * These timestamps are used by the data retention service to determine whether
 * a BDM object instance has exceeded its configured retention period.
 * <p>Mapped to the {@code data_retention_bdm_tracking} table in the Bonita engine schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "data_retention_bdm_tracking")
// Disabling cache because this entity is not expected to be accessed
// frequently (read by the data retention service executed by a CRON job)
@Cacheable(false)
public class SDataRetentionBdmTracking implements PersistentObject {

    @Id
    private long id;

    /**
     * The persistence ID of the tracked BDM object instance.
     */
    @Column(name = "data_id", nullable = false)
    private long dataId;

    /**
     * Fully qualified Java class name of the BDM object type.
     * <p>Example: {@code com.company.model.ContratClient}
     */
    @Column(name = "data_classname", nullable = false)
    private String dataClassname;

    /**
     * Epoch timestamp (milliseconds) when the BDM object instance was first created.
     */
    @Column(name = "created_at", nullable = false)
    private long createdAt;

    /**
     * Epoch timestamp (milliseconds) when the BDM object instance was last modified.
     */
    @Column(name = "last_modified_at", nullable = false)
    private long lastModifiedAt;

}
