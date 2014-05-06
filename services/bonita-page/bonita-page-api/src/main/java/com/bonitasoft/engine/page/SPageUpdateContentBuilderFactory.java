package com.bonitasoft.engine.page;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

public interface SPageUpdateContentBuilderFactory {

    SPageUpdateContentBuilder createNewInstance(EntityUpdateDescriptor descriptor);
}
