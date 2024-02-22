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
package org.bonitasoft.engine.profile.model;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "profile")
@IdClass(PersistentObjectId.class)
public class SProfile implements PersistentObject {

    public static final String PROFILE_IDS = "profileIds";
    public static final String ICON_PATH = "iconPath";
    public static final String DESCRIPTION = "description";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String IS_DEFAULT = "isDefault";
    public static final String CREATION_DATE = "creationDate";
    public static final String CREATED_BY = "createdBy";
    public static final String LAST_UPDATE_DATE = "lastUpdateDate";
    public static final String LAST_UPDATED_BY = "lastUpdatedBy";
    @Id
    private long id;
    @Id
    private long tenantId;
    @Column
    private boolean isDefault;
    @Column
    private String name;
    @Column
    private String description;
    @Column
    private long creationDate;
    @Column
    private long createdBy;
    @Column
    private long lastUpdateDate;
    @Column
    private long lastUpdatedBy;

    public SProfile(final SProfile profile) {
        super();
        tenantId = profile.getTenantId();
        id = profile.getId();
        isDefault = profile.isDefault();
        name = profile.getName();
        description = profile.getDescription();
        creationDate = profile.getCreationDate();
        createdBy = profile.getCreatedBy();
        lastUpdateDate = profile.getLastUpdateDate();
        lastUpdatedBy = profile.getLastUpdatedBy();
    }

}
