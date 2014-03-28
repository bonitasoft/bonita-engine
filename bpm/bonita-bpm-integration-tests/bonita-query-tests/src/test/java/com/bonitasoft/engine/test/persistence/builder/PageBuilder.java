package com.bonitasoft.engine.test.persistence.builder;

import com.bonitasoft.engine.page.impl.SPageImpl;
import com.bonitasoft.engine.page.impl.SPageWithContentImpl;

public class PageBuilder extends PersistentObjectBuilder<SPageWithContentImpl> {

    private String name;

    private String description;

    private String displayName;

    private long installationDate;

    private long installedBy;

    private boolean provided;

    private long lastModificationDate;

    private long lastUpdatedBy;

    private String contentName;

    private byte[] content;

    public static PageBuilder aPage() {
        return new PageBuilder();
    }

    @Override
    public SPageWithContentImpl _build() {
        SPageImpl sPageImpl = new SPageImpl(name, description, displayName, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy,
                contentName);
        return new SPageWithContentImpl(sPageImpl, content);
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

}
