package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.actor.mapping.model.impl.SActorImpl;


public class ActorBuilder extends PersistentObjectBuilder<SActorImpl, ActorBuilder> {

    @Override
    ActorBuilder getThisBuilder() {

        return this;
    }

    public static ActorBuilder anActor() {
        return new ActorBuilder();
    }
    
    @Override
    SActorImpl _build() {
        return new SActorImpl();
    }

}
