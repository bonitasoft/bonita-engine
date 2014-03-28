package com.bonitasoft.engine.page.impl;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.page.SPageUpdateBuilder;

public class SPageUpdateBuilderImpl implements SPageUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SPageUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        super();
        this.descriptor = descriptor;
    }

    @Override
    public SPageUpdateBuilder updateName(final String value) {
        descriptor.addField(SPageFields.PAGE_NAME, value);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateDescription(final String value) {
        descriptor.addField(SPageFields.PAGE_DESCRIPTION, value);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateDisplayName(final String value) {
        descriptor.addField(SPageFields.PAGE_DISPLAY_NAME, value);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateLastModificationDate(final long currentTimeMillis) {
        descriptor.addField(SPageFields.PAGE_LAST_MODIFICATION_DATE, currentTimeMillis);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateLastUpdatedBy(final long userId) {
        descriptor.addField(SPageFields.PAGE_LAST_UPDATED_BY, userId);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateContentName(final String value) {
        descriptor.addField(SPageFields.PAGE_CONTENT_NAME, value);
        return this;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

}
