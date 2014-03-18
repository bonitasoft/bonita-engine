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
    public SPageUpdateBuilder updateName(String value) {
        descriptor.addField("name", value);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateDescription(String value) {
        descriptor.addField("description", value);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateDisplayName(String value) {
        descriptor.addField("displayName", value);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateLastModificationDate(long currentTimeMillis) {
        descriptor.addField("lastModificationDate", currentTimeMillis);
        return this;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

}
