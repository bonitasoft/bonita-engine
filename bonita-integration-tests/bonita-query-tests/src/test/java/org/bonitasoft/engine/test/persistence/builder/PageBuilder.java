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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.page.AbstractSPage;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageWithContent;

public class PageBuilder extends PersistentObjectBuilder<AbstractSPage, PageBuilder> {

    private String name;

    private String description;

    private String displayName;

    private long installationDate;

    private long installedBy;

    private boolean provided;

    private long lastModificationDate;

    private long lastUpdatedBy;

    private String contentName;

    private String contentType;

    private long processDefinitionId;

    private byte[] content;
    private boolean editable;

    public static PageBuilder aPage() {
        return new PageBuilder();
    }

    @Override
    public SPageWithContent _build() {
        final SPage sPage = new SPage(name, description, displayName, installationDate, installedBy, provided, editable,
                lastModificationDate, lastUpdatedBy,
                contentName);
        sPage.setProcessDefinitionId(processDefinitionId);

        return new SPageWithContent(sPage, content);
    }

    public PageBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public PageBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public PageBuilder withDisplayName(final String displayName) {
        this.displayName = displayName;
        return this;
    }

    public PageBuilder withInstallationDate(final long installationDate) {
        this.installationDate = installationDate;
        return this;
    }

    public PageBuilder withInstalledBy(final long installedBy) {
        this.installedBy = installedBy;
        return this;
    }

    public PageBuilder withProvided(final boolean provided) {
        this.provided = provided;
        return this;
    }

    public PageBuilder withLastModificationDate(final long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
        return this;
    }

    public PageBuilder withLastUpdatedBy(final long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        return this;
    }

    public PageBuilder withContentName(final String contentName) {
        this.contentName = contentName;
        return this;
    }

    public PageBuilder withContent(final byte[] content) {
        this.content = content;
        return this;
    }

    public PageBuilder withContentType(final String contentType) {
        this.contentType = contentType;
        return this;
    }

    public PageBuilder withProcessDefinitionId(final Long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }

    public PageBuilder makeEditable(Boolean editable) {
        this.editable = editable;
        return this;
    }

    @Override
    PageBuilder getThisBuilder() {
        return this;
    }

}
