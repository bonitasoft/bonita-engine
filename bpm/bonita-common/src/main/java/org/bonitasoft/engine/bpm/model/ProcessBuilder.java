/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.bpm.model;

import org.bonitasoft.engine.bpm.model.impl.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.bpm.model.impl.FlowElementContainerDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 */
public class ProcessBuilder extends FlowElementContainerBuilder {

    private final DesignProcessDefinitionImpl process;

    ProcessBuilder(final DesignProcessDefinitionImpl process, final ProcessDefinitionBuilder processDefinitionBuilder) {
        super((FlowElementContainerDefinitionImpl) process.getProcessContainer(), processDefinitionBuilder);
        this.process = process;
    }

    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder(getProcessBuilder(), process, parameterName, type);
    }

    public ActorDefinitionBuilder addActor(final String actorName) {
        return new ActorDefinitionBuilder(getProcessBuilder(), process, actorName, false);
    }

    public ActorDefinitionBuilder addActor(final String name, final boolean initiator) {
        return new ActorDefinitionBuilder(getProcessBuilder(), process, name, initiator);
    }

    public ActorDefinitionBuilder setActorInitiator(final String actorName) {
        return new ActorDefinitionBuilder(getProcessBuilder(), process, actorName, true);
    }

    public DocumentDefinitionBuilder addDocumentDefinition(final String name, final String fileName) {
        return new DocumentDefinitionBuilder(getProcessBuilder(), (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, fileName);
    }

    DesignProcessDefinitionImpl getProcessDefinition() {
        return process;
    }

}
