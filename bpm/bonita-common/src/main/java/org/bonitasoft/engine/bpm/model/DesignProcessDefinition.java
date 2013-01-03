/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.bpm.model;

import java.util.Set;

import com.bonitasoft.engine.bpm.model.ParameterDefinition;

/**
 * @author Matthieu Chaffotte
 */
public interface DesignProcessDefinition extends ProcessDefinition {

    String getDisplayName();

    String getDisplayDescription();

    FlowElementContainerDefinition getProcessContainer();

    Set<ParameterDefinition> getParameters();

    Set<ActorDefinition> getActors();

    ActorDefinition getActorInitiator();

    long getActorInitiatorId();

    String getStringIndexLabel(int index);

}
