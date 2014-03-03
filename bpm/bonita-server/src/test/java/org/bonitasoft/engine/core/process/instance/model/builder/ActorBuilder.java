package org.bonitasoft.engine.core.process.instance.model.builder;

import org.bonitasoft.engine.actor.mapping.model.impl.SActorImpl;


public class ActorBuilder extends Builder<SActorImpl> {

    public static ActorBuilder anActor() {
        return new ActorBuilder();
    }
    
    @Override
    SActorImpl _build() {
        return new SActorImpl();
    }

}
