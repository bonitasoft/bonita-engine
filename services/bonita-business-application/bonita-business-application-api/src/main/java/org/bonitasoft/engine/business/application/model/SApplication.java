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
package org.bonitasoft.engine.business.application.model;

import org.bonitasoft.engine.persistence.PersistentObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SApplication implements PersistentObject {

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
    private long id;
    private long tenantId;
    private String token;
    private String description;
    private String version;
    private String iconPath;
    private long creationDate;
    private long createdBy;
    private long lastUpdateDate;
    private long updatedBy;
    private String state;
    private Long homePageId;
    private String displayName;
    private Long profileId;
    private Long layoutId;
    private Long themeId;

    public SApplication(final String token, final String displayName, final String version, final long creationDate, final long createdBy,
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
