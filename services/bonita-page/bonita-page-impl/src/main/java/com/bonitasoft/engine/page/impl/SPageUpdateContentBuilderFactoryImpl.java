package com.bonitasoft.engine.page.impl;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.page.SPageUpdateContentBuilder;
import com.bonitasoft.engine.page.SPageUpdateContentBuilderFactory;

public class SPageUpdateContentBuilderFactoryImpl implements SPageUpdateContentBuilderFactory {

    @Override
    public SPageUpdateContentBuilder createNewInstance(EntityUpdateDescriptor descriptor) {
        return new SPageUpdateContentBuilderImpl(descriptor);
    }

}
