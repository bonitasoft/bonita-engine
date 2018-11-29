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
package org.bonitasoft.engine.business.application.model.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Elias Ricken de Medeiros
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SApplicationImpl extends PersistentObjectId implements SApplication {

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

    public SApplicationImpl(final String token, final String displayName, final String version, final long creationDate, final long createdBy,
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
