package com.bonitasoft.engine.page.impl;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.page.SPageUpdateContentBuilder;

public class SPageUpdateContentBuilderImpl implements SPageUpdateContentBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SPageUpdateContentBuilderImpl(final EntityUpdateDescriptor descriptor) {
        super();
        this.descriptor = descriptor;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SPageUpdateContentBuilder updateContent(byte[] content) {
        descriptor.addField("content", content);
        return null;
    }

}
