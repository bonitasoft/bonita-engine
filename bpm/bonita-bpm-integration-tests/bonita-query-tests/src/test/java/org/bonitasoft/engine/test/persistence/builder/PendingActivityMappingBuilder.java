package org.bonitasoft.engine.test.persistence.builder;

import java.util.Random;

import org.bonitasoft.engine.core.process.instance.model.impl.SPendingActivityMappingImpl;


public class PendingActivityMappingBuilder extends PersistentObjectBuilder<SPendingActivityMappingImpl, PendingActivityMappingBuilder> {

    private long activityId = new Random().nextLong();
    private long userId;
    private long actorId;

    public static PendingActivityMappingBuilder aPendingActivityMapping() {
        return new PendingActivityMappingBuilder();
    }
    
    @Override
    PendingActivityMappingBuilder getThisBuilder() {
        return this;
    }

    @Override
    SPendingActivityMappingImpl _build() {
        SPendingActivityMappingImpl mapping = new SPendingActivityMappingImpl(activityId);
        mapping.setUserId(userId);
        mapping.setActorId(actorId);
        return mapping;
    }
    
    public PendingActivityMappingBuilder withUserId(long userId) {
        this.userId = userId;
        return this;
    }
    
    public PendingActivityMappingBuilder withActorId(long actorId) {
        this.actorId = actorId;
        return this;
    }
}
