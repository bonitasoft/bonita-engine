/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.bonitasoft.engine.bpm.flownode.impl.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.bpm.userfilter.impl.UserFilterDefinitionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class HumanTaskDefinitionImpl extends TaskDefinitionImpl implements HumanTaskDefinition {

    private static final long serialVersionUID = -7657152341382296289L;
    @XmlAttribute
    private final String actorName;
    @XmlElement(type = UserFilterDefinitionImpl.class)
    private UserFilterDefinition userFilterDefinition;
    @XmlAttribute
    private Long expectedDuration;
    @XmlAttribute
    private String priority;

    public HumanTaskDefinitionImpl(){
        actorName = "default actor name";
    }
    public HumanTaskDefinitionImpl(final String name, final String actorName) {
        super(name);
        this.actorName = actorName;
    }

    public HumanTaskDefinitionImpl(final long id, final String name, final String actorName) {
        super(id, name);
        this.actorName = actorName;
    }

    @Override
    public String getActorName() {
        return actorName;
    }

    @Override
    public UserFilterDefinition getUserFilter() {
        return userFilterDefinition;
    }

    @Override
    public void setUserFilter(final UserFilterDefinition userFilterDefinition) {
        this.userFilterDefinition = userFilterDefinition;
    }

    public void setExpectedDuration(final long expectedDuration) {
        this.expectedDuration = expectedDuration;
    }

    public void setPriority(final String priority) {
        this.priority = priority;
    }

    @Override
    public Long getExpectedDuration() {
        return expectedDuration;
    }

    @Override
    public String getPriority() {
        return priority;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (actorName == null ? 0 : actorName.hashCode());
        result = prime * result + (expectedDuration == null ? 0 : expectedDuration.hashCode());
        result = prime * result + (priority == null ? 0 : priority.hashCode());
        result = prime * result + (userFilterDefinition == null ? 0 : userFilterDefinition.hashCode());
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
        final HumanTaskDefinitionImpl other = (HumanTaskDefinitionImpl) obj;
        if (actorName == null) {
            if (other.actorName != null) {
                return false;
            }
        } else if (!actorName.equals(other.actorName)) {
            return false;
        }
        if (expectedDuration == null) {
            if (other.expectedDuration != null) {
                return false;
            }
        } else if (!expectedDuration.equals(other.expectedDuration)) {
            return false;
        }
        if (priority == null) {
            if (other.priority != null) {
                return false;
            }
        } else if (!priority.equals(other.priority)) {
            return false;
        }
        if (userFilterDefinition == null) {
            if (other.userFilterDefinition != null) {
                return false;
            }
        } else if (!userFilterDefinition.equals(other.userFilterDefinition)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final int maxLen = 5;
        final StringBuilder builder = new StringBuilder();
        builder.append("HumanTaskDefinitionImpl [actorName=");
        builder.append(actorName);
        builder.append(", userFilterDefinition=");
        builder.append(userFilterDefinition);
        builder.append(", expectedDuration=");
        builder.append(expectedDuration);
        builder.append(", priority=");
        builder.append(priority);
        builder.append(", getDataDefinitions()=");
        builder.append(getDataDefinitions() != null ? getDataDefinitions().subList(0, Math.min(getDataDefinitions().size(), maxLen)) : null);
        builder.append(", getOperations()=");
        builder.append(getOperations() != null ? getOperations().subList(0, Math.min(getOperations().size(), maxLen)) : null);
        builder.append(", getLoopCharacteristics()=");
        builder.append(getLoopCharacteristics());
        builder.append(", getIncomingTransitions()=");
        builder.append(getIncomingTransitions() != null ? getIncomingTransitions().subList(0, Math.min(getIncomingTransitions().size(), maxLen)) : null);
        builder.append(", getOutgoingTransitions()=");
        builder.append(getOutgoingTransitions() != null ? getOutgoingTransitions().subList(0, Math.min(getOutgoingTransitions().size(), maxLen)) : null);
        builder.append(", getConnectors()=");
        builder.append(getConnectors() != null ? getConnectors().subList(0, Math.min(getConnectors().size(), maxLen)) : null);
        builder.append(", getDescription()=");
        builder.append(getDescription());
        builder.append(", getDisplayDescription()=");
        builder.append(getDisplayDescription());
        builder.append(", getDisplayName()=");
        builder.append(getDisplayName());
        builder.append(", getDisplayDescriptionAfterCompletion()=");
        builder.append(getDisplayDescriptionAfterCompletion());
        builder.append(", getName()=");
        builder.append(getName());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
