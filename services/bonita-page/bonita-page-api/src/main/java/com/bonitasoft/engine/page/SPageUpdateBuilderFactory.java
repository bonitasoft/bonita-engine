package com.bonitasoft.engine.page;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

public interface SPageUpdateBuilderFactory {

    SPageUpdateBuilder createNewInstance(EntityUpdateDescriptor descriptor);
}
