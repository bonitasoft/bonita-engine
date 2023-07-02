/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.bonitasoft.engine.expression.ExpressionBuilder.getNonNullCopy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.internal.NamedDefinitionElementImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class FlowNodeDefinitionImpl extends NamedDefinitionElementImpl implements FlowNodeDefinition {

    private static final long serialVersionUID = 429640943678358154L;

    @XmlIDREF
    @XmlElement(type = TransitionDefinitionImpl.class, name = "incomingTransition")
    private final List<TransitionDefinition> incomings = new ArrayList<>();
    @XmlIDREF
    @XmlElement(type = TransitionDefinitionImpl.class, name = "outgoingTransition")
    private final List<TransitionDefinition> outgoings = new ArrayList<>();
    @XmlElement(type = ConnectorDefinitionImpl.class, name = "connector")
    private final List<ConnectorDefinition> connectors = new ArrayList<>();
    @Getter
    @Setter
    @XmlElement
    private String description;
    @Getter
    @XmlElement(type = ExpressionImpl.class)
    private Expression displayDescription;
    @Getter
    @XmlElement(type = ExpressionImpl.class)
    private Expression displayName;
    @Getter
    @XmlElement(type = ExpressionImpl.class)
    private Expression displayDescriptionAfterCompletion;
    @Getter
    @Setter
    @XmlIDREF
    @XmlElement(type = TransitionDefinitionImpl.class)
    private TransitionDefinition defaultTransition;

    public FlowNodeDefinitionImpl(final long id, final String name) {
        super(name);
        setId(id);
    }

    public FlowNodeDefinitionImpl(final String name) {
        super(name);
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

    public void setDisplayDescription(final Expression displayDescription) {
        this.displayDescription = getNonNullCopy(displayDescription);
    }

    public void setDisplayName(final Expression displayName) {
        this.displayName = getNonNullCopy(displayName);
    }

    public void setDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        this.displayDescriptionAfterCompletion = getNonNullCopy(displayDescriptionAfterCompletion);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }
}
