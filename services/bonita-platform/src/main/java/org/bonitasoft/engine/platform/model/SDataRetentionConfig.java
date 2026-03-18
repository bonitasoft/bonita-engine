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
package org.bonitasoft.engine.platform.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * Persistent entity representing a data retention rule configured by a Platform Administrator
 * for a specific BDM object type.
 * <p>Each instance of this class defines when and how BDM object instances of a given Java type
 * should be automatically deleted by the data retention service. One rule exists
 * per BDM object type — the {@code dataClassname} column carries a unique constraint.
 * <p>A rule is considered active as soon as it is persisted. There is no enable/disable toggle:
 * to stop retention processing for an object type, the rule must be deleted entirely.
 * <p>Mapped to the {@code data_retention_config} table in the Bonita engine schema.
 * This table is never present in the BDM schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "data_retention_config")
public class SDataRetentionConfig implements PersistentObject {

    @Id
    private long id;

    /**
     * Fully qualified Java class name of the BDM object type this rule applies to.
     * <p>Example: {@code com.company.model.ContratClient}
     * <p>This value is derived from the deployed BDM model and must match an existing
     * object type at rule creation time. If the BDM is redeployed and this class no longer
     * exists in the new model, the rule is automatically deleted.
     * <p>Unique — only one retention rule may exist per BDM object type.
     */
    @Column(name = "data_classname", nullable = false)
    private String dataClassname;

    /**
     * The date field used as the starting point for the retention period calculation.
     * <p>Possible values:
     * <ul>
     * <li>{@code CREATION} — retention is calculated from the date the BDM object
     * instance was first created, as recorded in {@code data_retention_bdm_tracking.created_at}.
     * The clock never resets, regardless of subsequent modifications.</li>
     * <li>{@code LAST_UPDATE} — retention is calculated from the date the BDM object
     * instance was last modified, as recorded in {@code data_retention_bdm_tracking.last_modified_at}.
     * The clock resets to zero on every modification.</li>
     * </ul>
     *
     * @see SReferenceDate
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_date", nullable = false)
    private SReferenceDate referenceDate;

    /**
     * Duration of the retention period, expressed in days.
     * Must be a positive integer.
     */
    @Column(name = "retention_days", nullable = false)
    private int retentionDays;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "updated_at", nullable = false)
    private long updatedAt;

}
