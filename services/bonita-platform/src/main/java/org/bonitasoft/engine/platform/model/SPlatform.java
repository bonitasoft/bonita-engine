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
package org.bonitasoft.engine.platform.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PlatformPersistentObject;
import org.hibernate.annotations.Type;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "platform")
public class SPlatform implements PlatformPersistentObject {

    public static final String CREATED_BY = "createdBy";
    public static final String CREATED = "created";
    public static final String ID = "id";
    public static final String INITIAL_VERSION = "initialVersion";
    public static final String PREVIOUS_VERSION = "previousVersion";
    public static final String VERSION = "version";
    public static final String INFORMATION = "information";
    public static final String MAINTENANCE_MESSAGE = "maintenance_message";
    public static final String MAINTENANCE_MESSAGE_ACTIVE = "maintenance_message_active";

    @Id
    private long id;
    private long created;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "initial_bonita_version")
    private String initialBonitaVersion;
    @Column(name = "version")
    private String dbSchemaVersion;
    @Type(type = "materialized_clob")
    private String information;
    @Column(name = "application_version")
    private String applicationVersion;
    @Column(name = "maintenance_message")
    private String maintenanceMessage;
    @Column(name = "maintenance_message_active")
    private boolean maintenanceMessageActive;

    public SPlatform(final String dbSchemaVersion, final String initialBonitaVersion, final String applicationVersion,
            final String maintenanceMessage, final boolean maintenanceMessageActive,
            final String createdBy, final long created) {
        this.dbSchemaVersion = dbSchemaVersion;
        this.initialBonitaVersion = initialBonitaVersion;
        this.applicationVersion = applicationVersion;
        this.maintenanceMessage = maintenanceMessage;
        this.maintenanceMessageActive = maintenanceMessageActive;
        this.createdBy = createdBy;
        this.created = created;
    }

    @Override
    public void setTenantId(long id) {
        //no tenant id
    }
}
