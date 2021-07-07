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

import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.hibernate.annotations.Filter;

@Data
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@IdClass(PersistentObjectId.class)
@Filter(name = "tenantFilter")
public class AbstractSPage implements PersistentObject {

    @Id
    private long tenantId;
    @Id
    private long id;

    private String name;
    private String description;
    private String displayName;
    private long installationDate;
    private long installedBy;
    private boolean provided;
    private boolean editable = true;
    private boolean hidden;
    private boolean removable = true;
    private long lastModificationDate;
    private long lastUpdatedBy;
    private String contentName;
    private String contentType;
    private long processDefinitionId;

    /**
     * A MD5 sum hash of the content of the page.
     * It is used only for verifying if a provided page has change or not.
     * It is not used (and not filled) for user pages
     */
    private String pageHash;

    public AbstractSPage(final AbstractSPage sPage) {
        name = sPage.getName();
        description = sPage.getDescription();
        displayName = sPage.getDisplayName();
        installationDate = sPage.getInstallationDate();
        installedBy = sPage.getInstalledBy();
        provided = sPage.isProvided();
        hidden = sPage.isHidden();
        lastModificationDate = sPage.getLastModificationDate();
        lastUpdatedBy = sPage.getLastUpdatedBy();
        contentName = sPage.getContentName();
        contentType = sPage.getContentType();
        processDefinitionId = sPage.getProcessDefinitionId();
        editable = sPage.isEditable();
        removable = sPage.isRemovable();
        pageHash = sPage.getPageHash();
    }

    public AbstractSPage(final String name, final long installationDate, final long installedBy, final boolean provided,
            final String contentName) {
        this.name = name;
        this.installationDate = installationDate;
        this.installedBy = installedBy;
        this.provided = provided;
        this.contentType = SContentType.PAGE;
        this.contentName = contentName;
    }

}
