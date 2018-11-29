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
package org.bonitasoft.engine.page.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.page.SContentType;
import org.bonitasoft.engine.page.SPage;

/**
 * @author Baptiste Mesta
 */

@Data
@NoArgsConstructor
public class SPageImpl implements SPage {

    private long tenantId;
    private long id;
    private String name;
    private String description;
    private String displayName;
    private long installationDate;
    private long installedBy;
    private boolean provided;
    private boolean hidden;
    private long lastModificationDate;
    private long lastUpdatedBy;
    private String contentName;
    private String contentType;
    private long processDefinitionId;

    public SPageImpl(final String name, final String description, final String displayName, final long installationDate,
            final long installedBy,
            final boolean provided, final long lastModificationDate, final long lastUpdatedBy,
            final String contentName) {
        this(name, installationDate, installedBy, provided, contentName);
        setDescription(description);
        setDisplayName(displayName);
        setProvided(provided);
        setLastModificationDate(lastModificationDate);
        setLastUpdatedBy(lastUpdatedBy);
    }

    public SPageImpl(final String name, final String description, final String displayName, final long installationDate,
            final long installedBy,
            final boolean provided, boolean hidden, final long lastModificationDate, final long lastUpdatedBy,
            final String contentName) {
        this(name, installationDate, installedBy, provided, contentName);
        setDescription(description);
        setDisplayName(displayName);
        setProvided(provided);
        setLastModificationDate(lastModificationDate);
        setLastUpdatedBy(lastUpdatedBy);
        setHidden(hidden);
    }

    /**
     * @param sPage
     */
    public SPageImpl(final SPage sPage) {
        this(sPage.getName(), sPage.getDescription(), sPage.getDisplayName(), sPage.getInstallationDate(),
                sPage.getInstalledBy(), sPage.isProvided(), sPage.isHidden(), sPage
                        .getLastModificationDate(),
                sPage.getLastUpdatedBy(), sPage.getContentName());
        setContentType(sPage.getContentType());
        setProcessDefinitionId(sPage.getProcessDefinitionId());
    }

    public SPageImpl(final String name, final long installationDate, final long installedBy, final boolean provided,
            final String contentName) {
        setName(name);
        setInstallationDate(installationDate);
        setInstalledBy(installedBy);
        setProvided(provided);
        setContentName(contentName);
        setContentType(SContentType.PAGE);
    }
}
