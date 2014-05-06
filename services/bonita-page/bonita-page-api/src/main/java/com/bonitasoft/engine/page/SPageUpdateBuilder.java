package com.bonitasoft.engine.page;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

public interface SPageUpdateBuilder {

    EntityUpdateDescriptor done();

    SPageUpdateBuilder updateName(String value);

    SPageUpdateBuilder updateDescription(String value);

    SPageUpdateBuilder updateDisplayName(String value);

    SPageUpdateBuilder updateLastModificationDate(long currentTimeMillis);

    SPageUpdateBuilder updateLastUpdatedBy(long userId);

    SPageUpdateBuilder updateContentName(String value);

}
