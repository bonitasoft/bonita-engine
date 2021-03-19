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
package org.bonitasoft.engine.business.application.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.hibernate.annotations.Filter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "business_app")
@IdClass(PersistentObjectId.class)
@Filter(name = "tenantFilter")
public class SApplication implements PersistentObject {

    // class must be present for the javadoc generation
    public static class SApplicationBuilder{}

    public static final String ID = "id";
    public static final String TOKEN = "token";
    public static final String DISPLAY_NAME = "displayName";
    public static final String VERSION = "version";
    public static final String DESCRIPTION = "description";
    public static final String ICON_PATH = "iconPath";
    public static final String CREATION_DATE = "creationDate";
    public static final String CREATED_BY = "createdBy";
    public static final String LAST_UPDATE_DATE = "lastUpdateDate";
    public static final String UPDATED_BY = "updatedBy";
    public static final String STATE = "state";
    public static final String HOME_PAGE_ID = "homePageId";
    public static final String PROFILE_ID = "profileId";
    public static final String LAYOUT_ID = "layoutId";
    public static final String THEME_ID = "themeId";

    @Id
    private long id;
    @Id
    private long tenantId;
    @Column
    private String token;
    @Column
    private String description;
    @Column
    private String version;
    @Column
    private String iconPath;
    @Column
    private long creationDate;
    @Column
    private long createdBy;
    @Column
    private long lastUpdateDate;
    @Column
    private long updatedBy;
    @Column
    private String state;
    @Column
    private Long homePageId;
    @Column
    private String displayName;
    @Column
    private Long profileId;
    @Column
    private Long layoutId;
    @Column
    private Long themeId;

    public SApplication(final String token, final String displayName, final String version, final long creationDate,
            final long createdBy,
            final String state, final Long layoutId, final Long themeId) {
        super();
        this.token = token;
        this.displayName = displayName;
        this.version = version;
        this.creationDate = creationDate;
        lastUpdateDate = creationDate; //at instantiation the creation date is the same as last update date
        this.createdBy = createdBy;
        updatedBy = createdBy;
        this.state = state;
        this.layoutId = layoutId;
        this.themeId = themeId;
    }

}
