/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.bpm.model.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bonitasoft.engine.bpm.model.ActorDefinition;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.FlowElementContainerDefinition;

import com.bonitasoft.engine.bpm.model.ParameterDefinition;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Baptiste Mesta
 */
public class DesignProcessDefinitionImpl extends ProcessDefinitionImpl implements DesignProcessDefinition {

    private static final long serialVersionUID = -4719128363958199300L;

    private String displayName;

    private String displayDescription;

    private final Set<ParameterDefinition> parameters;

    private final Set<ActorDefinition> actors;

    private ActorDefinition actorInitiator;

    private final long actorInitiatorId;

    private FlowElementContainerDefinition flowElementContainer;

    private String stringIndexLabel1;

    private String stringIndexLabel2;

    private String stringIndexLabel3;

    private String stringIndexLabel4;

    private String stringIndexLabel5;

    public DesignProcessDefinitionImpl(final String name, final String version) {
        super(name, version);
        parameters = new HashSet<ParameterDefinition>();
        actors = new HashSet<ActorDefinition>();
        // FIXME: pass actorInitiatorId as parameter:
        actorInitiatorId = -1;
    }

    public void setDisplayName(final String name) {
        displayName = name;
    }

    public void setDisplayDescription(final String description) {
        displayDescription = description;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDisplayDescription() {
        return displayDescription;
    }

    @Override
    public Set<ActorDefinition> getActors() {
        return actors;
    }

    @Override
    public Set<ParameterDefinition> getParameters() {
        return parameters;
    }

    public void addActor(final ActorDefinition actor) {
        actors.add(actor);
    }

    public void addParameter(final ParameterDefinition parameter) {
        parameters.add(parameter);
    }

    @Override
    public long getActorInitiatorId() {
        return actorInitiatorId;
    }

    @Override
    public ActorDefinition getActorInitiator() {
        return actorInitiator;
    }

    public void setActorInitiator(final ActorDefinition actorInitiator) {
        this.actorInitiator = actorInitiator;
    }

    @Override
    public FlowElementContainerDefinition getProcessContainer() {
        return flowElementContainer;
    }

    public void setProcessContainer(final FlowElementContainerDefinition processContainer) {
        flowElementContainer = processContainer;
    }

    @Override
    public String toString() {
        final int maxLen = 5;
        final StringBuilder builder = new StringBuilder();
        builder.append("DesignProcessDefinitionImpl [activities=");
        builder.append(toString(flowElementContainer.getActivities(), maxLen));
        builder.append(", transitions=").append(toString(flowElementContainer.getTransitions(), maxLen));
        builder.append(", parameters=").append(parameters != null ? toString(parameters, maxLen) : null);
        builder.append(", actors=").append(actors != null ? toString(actors, maxLen) : null);
        builder.append(", actorInitiator=").append(actorInitiator);
        builder.append(", actorInitiatorId=").append(actorInitiatorId);
        builder.append(", gateways=").append(toString(flowElementContainer.getGateways(), maxLen));
        builder.append(", connectors=").append(toString(flowElementContainer.getConnectors(), maxLen));
        builder.append(", startEvents=").append(toString(flowElementContainer.getStartEvents(), maxLen));
        builder.append(", intermediateCatchEvents=").append(toString(flowElementContainer.getIntermediateCatchEvents(), maxLen));
        builder.append(", endEvents=").append(toString(flowElementContainer.getEndEvents(), maxLen));
        builder.append(", intermediateThrowEvents=").append(toString(flowElementContainer.getIntermediateThrowEvents(), maxLen));
        builder.append(", dataDefinitions=").append(toString(flowElementContainer.getDataDefinitions(), maxLen));
        builder.append(", documentDefinitions=").append(toString(flowElementContainer.getDocumentDefinitions(), maxLen));
        builder.append(", displayName=").append(displayName);
        builder.append(", displayDescription=").append(displayDescription);
        builder.append("]");
        return builder.toString();
    }

    private String toString(final Collection<?> collection, final int maxLen) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (final Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (actorInitiator == null ? 0 : actorInitiator.hashCode());
        result = prime * result + (int) (actorInitiatorId ^ actorInitiatorId >>> 32);
        result = prime * result + (actors == null ? 0 : actors.hashCode());
        result = prime * result + (displayDescription == null ? 0 : displayDescription.hashCode());
        result = prime * result + (displayName == null ? 0 : displayName.hashCode());
        result = prime * result + (flowElementContainer == null ? 0 : flowElementContainer.hashCode());
        result = prime * result + (parameters == null ? 0 : parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DesignProcessDefinitionImpl other = (DesignProcessDefinitionImpl) obj;
        if (actorInitiator == null) {
            if (other.actorInitiator != null) {
                return false;
            }
        } else if (!actorInitiator.equals(other.actorInitiator)) {
            return false;
        }
        if (actorInitiatorId != other.actorInitiatorId) {
            return false;
        }
        if (actors == null) {
            if (other.actors != null) {
                return false;
            }
        } else if (!actors.equals(other.actors)) {
            return false;
        }
        if (displayDescription == null) {
            if (other.displayDescription != null) {
                return false;
            }
        } else if (!displayDescription.equals(other.displayDescription)) {
            return false;
        }
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (flowElementContainer == null) {
            if (other.flowElementContainer != null) {
                return false;
            }
        } else if (!flowElementContainer.equals(other.flowElementContainer)) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
        return true;
    }
    

    public void setStringIndexLabel(final int index, final String label) {
        switch (index) {
            case 1:
                stringIndexLabel1 = label;
                break;
            case 2:
                stringIndexLabel2 = label;
                break;
            case 3:
                stringIndexLabel3 = label;
                break;
            case 4:
                stringIndexLabel4 = label;
                break;
            case 5:
                stringIndexLabel5 = label;
                break;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    @Override
    public String getStringIndexLabel(final int index) {
        switch (index) {
            case 1:
                return stringIndexLabel1;
            case 2:
                return stringIndexLabel2;
            case 3:
                return stringIndexLabel3;
            case 4:
                return stringIndexLabel4;
            case 5:
                return stringIndexLabel5;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

}
