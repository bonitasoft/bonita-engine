/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bpm.process.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class DesignProcessDefinitionImpl extends ProcessDefinitionImpl implements DesignProcessDefinition {

    private static final long serialVersionUID = -4719128363958199300L;

    private String displayName;

    private String displayDescription;

    private final Set<ParameterDefinition> parameters;

    private final List<ActorDefinition> actors;

    private ActorDefinition actorInitiator;

    private FlowElementContainerDefinition flowElementContainer;

    private String stringIndexLabel1;

    private String stringIndexLabel2;

    private String stringIndexLabel3;

    private String stringIndexLabel4;

    private String stringIndexLabel5;

    private Expression stringIndexValue1;

    private Expression stringIndexValue2;

    private Expression stringIndexValue3;

    private Expression stringIndexValue4;

    private Expression stringIndexValue5;

    public DesignProcessDefinitionImpl(final String name, final String version) {
        super(name, version);
        parameters = new HashSet<ParameterDefinition>();
        actors = new ArrayList<ActorDefinition>();
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
    public List<ActorDefinition> getActorsList() {
        return actors;
    }

    @Deprecated
    @Override
    public Set<ActorDefinition> getActors() {
        return new HashSet<ActorDefinition>(actors);
    }

    @Override
    public Set<ParameterDefinition> getParameters() {
        return parameters;
    }

    public void addParameter(final ParameterDefinition parameter) {
        parameters.add(parameter);
    }

    public void addActor(final ActorDefinition actor) {
        actors.add(actor);
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

    @Override
    public Expression getStringIndexValue(final int index) {
        switch (index) {
            case 1:
                return stringIndexValue1;
            case 2:
                return stringIndexValue2;
            case 3:
                return stringIndexValue3;
            case 4:
                return stringIndexValue4;
            case 5:
                return stringIndexValue5;
            default:
                throw new IndexOutOfBoundsException("string index value must be between 1 and 5 (included)");
        }
    }

    public ActorDefinition getActor(final String actorName) {
        final Iterator<ActorDefinition> iterator = actors.iterator();
        ActorDefinition actorDefinition = null;
        boolean found = false;
        while (!found && iterator.hasNext()) {
            final ActorDefinition next = iterator.next();
            if (next.getName().equals(actorName)) {
                found = true;
                actorDefinition = next;
            }
        }
        return actorDefinition;
    }

    public void setStringIndex(final int index, final String label, final Expression initialValue) {
        switch (index) {
            case 1:
                stringIndexLabel1 = label;
                stringIndexValue1 = initialValue;
                break;
            case 2:
                stringIndexLabel2 = label;
                stringIndexValue2 = initialValue;
                break;
            case 3:
                stringIndexLabel3 = label;
                stringIndexValue3 = initialValue;
                break;
            case 4:
                stringIndexLabel4 = label;
                stringIndexValue4 = initialValue;
                break;
            case 5:
                stringIndexLabel5 = label;
                stringIndexValue5 = initialValue;
                break;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    @Override
    public String toString() {
        return "DesignProcessDefinitionImpl [displayName=" + displayName + ", displayDescription=" + displayDescription + ", parameters=" + parameters
                + ", actors=" + actors + ", actorInitiator=" + actorInitiator + ", flowElementContainer=" + flowElementContainer + ", stringIndexLabel1="
                + stringIndexLabel1 + ", stringIndexLabel2=" + stringIndexLabel2 + ", stringIndexLabel3=" + stringIndexLabel3 + ", stringIndexLabel4="
                + stringIndexLabel4 + ", stringIndexLabel5=" + stringIndexLabel5 + ", stringIndexValue1=" + stringIndexValue1 + ", stringIndexValue2="
                + stringIndexValue2 + ", stringIndexValue3=" + stringIndexValue3 + ", stringIndexValue4=" + stringIndexValue4 + ", stringIndexValue5="
                + stringIndexValue5 + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((actorInitiator == null) ? 0 : actorInitiator.hashCode());
        result = prime * result + ((actors == null) ? 0 : actors.hashCode());
        result = prime * result + ((displayDescription == null) ? 0 : displayDescription.hashCode());
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + ((flowElementContainer == null) ? 0 : flowElementContainer.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((stringIndexLabel1 == null) ? 0 : stringIndexLabel1.hashCode());
        result = prime * result + ((stringIndexLabel2 == null) ? 0 : stringIndexLabel2.hashCode());
        result = prime * result + ((stringIndexLabel3 == null) ? 0 : stringIndexLabel3.hashCode());
        result = prime * result + ((stringIndexLabel4 == null) ? 0 : stringIndexLabel4.hashCode());
        result = prime * result + ((stringIndexLabel5 == null) ? 0 : stringIndexLabel5.hashCode());
        result = prime * result + ((stringIndexValue1 == null) ? 0 : stringIndexValue1.hashCode());
        result = prime * result + ((stringIndexValue2 == null) ? 0 : stringIndexValue2.hashCode());
        result = prime * result + ((stringIndexValue3 == null) ? 0 : stringIndexValue3.hashCode());
        result = prime * result + ((stringIndexValue4 == null) ? 0 : stringIndexValue4.hashCode());
        result = prime * result + ((stringIndexValue5 == null) ? 0 : stringIndexValue5.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DesignProcessDefinitionImpl other = (DesignProcessDefinitionImpl) obj;
        if (actorInitiator == null) {
            if (other.actorInitiator != null)
                return false;
        } else if (!actorInitiator.equals(other.actorInitiator))
            return false;
        if (actors == null) {
            if (other.actors != null)
                return false;
        } else if (!actors.equals(other.actors))
            return false;
        if (displayDescription == null) {
            if (other.displayDescription != null)
                return false;
        } else if (!displayDescription.equals(other.displayDescription))
            return false;
        if (displayName == null) {
            if (other.displayName != null)
                return false;
        } else if (!displayName.equals(other.displayName))
            return false;
        if (flowElementContainer == null) {
            if (other.flowElementContainer != null)
                return false;
        } else if (!flowElementContainer.equals(other.flowElementContainer))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (stringIndexLabel1 == null) {
            if (other.stringIndexLabel1 != null)
                return false;
        } else if (!stringIndexLabel1.equals(other.stringIndexLabel1))
            return false;
        if (stringIndexLabel2 == null) {
            if (other.stringIndexLabel2 != null)
                return false;
        } else if (!stringIndexLabel2.equals(other.stringIndexLabel2))
            return false;
        if (stringIndexLabel3 == null) {
            if (other.stringIndexLabel3 != null)
                return false;
        } else if (!stringIndexLabel3.equals(other.stringIndexLabel3))
            return false;
        if (stringIndexLabel4 == null) {
            if (other.stringIndexLabel4 != null)
                return false;
        } else if (!stringIndexLabel4.equals(other.stringIndexLabel4))
            return false;
        if (stringIndexLabel5 == null) {
            if (other.stringIndexLabel5 != null)
                return false;
        } else if (!stringIndexLabel5.equals(other.stringIndexLabel5))
            return false;
        if (stringIndexValue1 == null) {
            if (other.stringIndexValue1 != null)
                return false;
        } else if (!stringIndexValue1.equals(other.stringIndexValue1))
            return false;
        if (stringIndexValue2 == null) {
            if (other.stringIndexValue2 != null)
                return false;
        } else if (!stringIndexValue2.equals(other.stringIndexValue2))
            return false;
        if (stringIndexValue3 == null) {
            if (other.stringIndexValue3 != null)
                return false;
        } else if (!stringIndexValue3.equals(other.stringIndexValue3))
            return false;
        if (stringIndexValue4 == null) {
            if (other.stringIndexValue4 != null)
                return false;
        } else if (!stringIndexValue4.equals(other.stringIndexValue4))
            return false;
        if (stringIndexValue5 == null) {
            if (other.stringIndexValue5 != null)
                return false;
        } else if (!stringIndexValue5.equals(other.stringIndexValue5))
            return false;
        return true;
    }

}
