package com.bonitasoft.engine.page;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

public interface SPageUpdateContentBuilder {

    EntityUpdateDescriptor done();

    SPageUpdateContentBuilder updateContent(byte[] content);

}
