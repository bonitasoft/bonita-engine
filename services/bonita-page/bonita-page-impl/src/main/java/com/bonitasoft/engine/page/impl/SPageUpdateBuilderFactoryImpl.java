package com.bonitasoft.engine.page.impl;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.page.SPageUpdateBuilder;
import com.bonitasoft.engine.page.SPageUpdateBuilderFactory;

public class SPageUpdateBuilderFactoryImpl implements SPageUpdateBuilderFactory {

    @Override
    public SPageUpdateBuilder createNewInstance(EntityUpdateDescriptor descriptor) {
        return new SPageUpdateBuilderImpl(descriptor);
    }

}
