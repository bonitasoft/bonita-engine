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

import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class FlowNodeDefinitionImpl extends NamedElementImpl implements FlowNodeDefinition {

    private static final long serialVersionUID = 429640943678358154L;
    //@XmlElementWrapper(name = "incomings")
    @XmlIDREF
    @XmlElement(type = TransitionDefinitionImpl.class, name = "incomingTransition")
    private final List<TransitionDefinition> incomings;
    //@XmlElementWrapper(name = "outgoingTransition")
    @XmlIDREF
    @XmlElement(type = TransitionDefinitionImpl.class, name = "outgoingTransition")
    private final List<TransitionDefinition> outgoings;
    //@XmlElementWrapper(name = "connectors")
    @XmlElement(type = ConnectorDefinitionImpl.class, name = "connector")
    private final List<ConnectorDefinition> connectors;
    @XmlAttribute
    private String description;
    @XmlElement(type = ExpressionImpl.class)
    private Expression displayDescription;
    @XmlElement(type = ExpressionImpl.class)
    private Expression displayName;
    @XmlElement(type = ExpressionImpl.class)
    private Expression displayDescriptionAfterCompletion;
    @XmlElement(type = TransitionDefinitionImpl.class)
    private TransitionDefinition defaultTransition;

    public FlowNodeDefinitionImpl(final long id, final String name) {
        super(name);
        incomings = new ArrayList<>();
        outgoings = new ArrayList<>();
        connectors = new ArrayList<>();
        setId(id);
    }

    public FlowNodeDefinitionImpl(final String name) {
        this(Math.abs(UUID.randomUUID().getLeastSignificantBits()), name);
    }

    public FlowNodeDefinitionImpl() {
        incomings = new ArrayList<>();
        outgoings = new ArrayList<>();
        connectors = new ArrayList<>();
    }

    @Override
    public TransitionDefinition getDefaultTransition() {
        return defaultTransition;
    }

    public void setDefaultTransition(final TransitionDefinition defaultTransition) {
        this.defaultTransition = defaultTransition;
    }

    @Override
    public List<TransitionDefinition> getOutgoingTransitions() {
        return Collections.unmodifiableList(outgoings);
    }

    @Override
    public List<TransitionDefinition> getIncomingTransitions() {
        return Collections.unmodifiableList(incomings);
    }

    @Override
    public List<ConnectorDefinition> getConnectors() {
        return Collections.unmodifiableList(connectors);
    }

    public void addIncomingTransition(final TransitionDefinition transition) {
        if (!incomings.contains(transition)) {
            incomings.add(transition);
        }
    }

    public void addIncomingTransition(int index, TransitionDefinition transition) {
        if (!incomings.contains(transition)) {
            incomings.add(index, transition);
        }
    }

    public void removeIncomingTransition(final TransitionDefinition transition) {
        incomings.remove(transition);
    }

    public void addOutgoingTransition(final TransitionDefinition transition) {
        if (!outgoings.contains(transition)) {
            outgoings.add(transition);
        }
    }

    public void addOutgoingTransition(final int index, final TransitionDefinition transition) {
        if (!outgoings.contains(transition)) {
            outgoings.add(index, transition);
        }
    }

    public void removeOutgoingTransition(final TransitionDefinition transition) {
        outgoings.remove(transition);
    }

    @Override
    public void addConnector(final ConnectorDefinition connectorDefinition) {
        connectors.add(connectorDefinition);
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setDisplayDescription(final Expression displayDescription) {
        this.displayDescription = displayDescription;
    }

    public void setDisplayName(final Expression displayName) {
        this.displayName = displayName;
    }

    public void setDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        this.displayDescriptionAfterCompletion = displayDescriptionAfterCompletion;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Expression getDisplayDescription() {
        return displayDescription;
    }

    @Override
    public Expression getDisplayName() {
        return displayName;
    }

    @Override
    public Expression getDisplayDescriptionAfterCompletion() {
        return displayDescriptionAfterCompletion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (connectors == null ? 0 : connectors.hashCode());
        result = prime * result + (defaultTransition == null ? 0 : defaultTransition.hashCode());
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (displayDescription == null ? 0 : displayDescription.hashCode());
        result = prime * result + (displayDescriptionAfterCompletion == null ? 0 : displayDescriptionAfterCompletion.hashCode());
        result = prime * result + (displayName == null ? 0 : displayName.hashCode());
        result = prime * result + (incomings == null ? 0 : incomings.hashCode());
        result = prime * result + (outgoings == null ? 0 : outgoings.hashCode());
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
        final FlowNodeDefinitionImpl other = (FlowNodeDefinitionImpl) obj;
        if (connectors == null) {
            if (other.connectors != null) {
                return false;
            }
        } else if (!connectors.equals(other.connectors)) {
            return false;
        }
        if (defaultTransition == null) {
            if (other.defaultTransition != null) {
                return false;
            }
        } else if (!defaultTransition.equals(other.defaultTransition)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (displayDescription == null) {
            if (other.displayDescription != null) {
                return false;
            }
        } else if (!displayDescription.equals(other.displayDescription)) {
            return false;
        }
        if (displayDescriptionAfterCompletion == null) {
            if (other.displayDescriptionAfterCompletion != null) {
                return false;
            }
        } else if (!displayDescriptionAfterCompletion.equals(other.displayDescriptionAfterCompletion)) {
            return false;
        }
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (incomings == null) {
            if (other.incomings != null) {
                return false;
            }
        } else if (!incomings.equals(other.incomings)) {
            return false;
        }
        if (outgoings == null) {
            if (other.outgoings != null) {
                return false;
            }
        } else if (!outgoings.equals(other.outgoings)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final int maxLen = 5;
        final StringBuilder builder = new StringBuilder();
        builder.append("FlowNodeDefinitionImpl [incomings=");
        builder.append(incomings != null ? incomings.subList(0, Math.min(incomings.size(), maxLen)) : null);
        builder.append(", outgoings=");
        builder.append(outgoings != null ? outgoings.subList(0, Math.min(outgoings.size(), maxLen)) : null);
        builder.append(", connectors=");
        builder.append(connectors != null ? connectors.subList(0, Math.min(connectors.size(), maxLen)) : null);
        builder.append(", description=");
        builder.append(description);
        builder.append(", displayDescription=");
        builder.append(displayDescription);
        builder.append(", displayName=");
        builder.append(displayName);
        builder.append(", displayDescriptionAfterCompletion=");
        builder.append(displayDescriptionAfterCompletion);
        builder.append(", defaultTransition=");
        builder.append(defaultTransition);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }
}
