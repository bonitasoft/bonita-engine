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
package org.bonitasoft.engine.page;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author Matthieu Chaffotte
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "page")
public class SPage extends AbstractSPage {

    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String INSTALLATION_DATE = "installationDate";
    public static final String INSTALLED_BY = "installedBy";
    public static final String CONTENT_TYPE = "contentType";
    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
    public static final String ID = "id";
    public static final String PROVIDED = "provided";
    public static final String HIDDEN = "hidden";
    public static final String DISPLAY_NAME = "displayName";
    public static final String LAST_MODIFICATION_DATE = "lastModificationDate";
    public static final String LAST_UPDATE_BY = "lastUpdateBy";

    public SPage(final String name, final String description, final String displayName, final long installationDate,
            final long installedBy,
            final boolean provided, final long lastModificationDate, final long lastUpdatedBy,
            final String contentName) {
        super(name, installationDate, installedBy, provided, contentName);
        setDescription(description);
        setDisplayName(displayName);
        setProvided(provided);
        setLastModificationDate(lastModificationDate);
        setLastUpdatedBy(lastUpdatedBy);
    }

    public SPage(final String name, final String description, final String displayName, final long installationDate,
            final long installedBy,
            final boolean provided, boolean editable, boolean hidden, final long lastModificationDate,
            final long lastUpdatedBy,
            final String contentName) {
        this(name, installationDate, installedBy, provided, contentName);
        setDescription(description);
        setDisplayName(displayName);
        setProvided(provided);
        setLastModificationDate(lastModificationDate);
        setLastUpdatedBy(lastUpdatedBy);
        setHidden(hidden);
        setEditable(editable);
    }

    public SPage(final SPage sPage) {
        this(sPage.getName(), sPage.getDescription(), sPage.getDisplayName(), sPage.getInstallationDate(),
                sPage.getInstalledBy(), sPage.isProvided(), sPage.isEditable(), sPage.isHidden(), sPage
                        .getLastModificationDate(),
                sPage.getLastUpdatedBy(), sPage.getContentName());
        setContentType(sPage.getContentType());
        setProcessDefinitionId(sPage.getProcessDefinitionId());
    }

    public SPage(final String name, final long installationDate, final long installedBy, final boolean provided,
            final String contentName) {
        setName(name);
        setInstallationDate(installationDate);
        setInstalledBy(installedBy);
        setProvided(provided);
        setContentName(contentName);
        setContentType(SContentType.PAGE);
    }
}
