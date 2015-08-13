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

import org.bonitasoft.engine.bpm.flownode.CorrelationDefinition;
import org.bonitasoft.engine.bpm.flownode.MessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class MessageEventTriggerDefinitionImpl implements MessageEventTriggerDefinition {

    private static final long serialVersionUID = -190616505159460399L;
    @XmlAttribute(name = "name")
    private final String messageName;
    @XmlElement(type = CorrelationDefinitionImpl.class, name = "correlation")
    private final List<CorrelationDefinition> correlations;

    public MessageEventTriggerDefinitionImpl(final String name) {
        messageName = name;
        correlations = new ArrayList<>(1);
    }

    public MessageEventTriggerDefinitionImpl(final String name, final List<CorrelationDefinition> correlations) {
        messageName = name;
        this.correlations = correlations;
    }

    public MessageEventTriggerDefinitionImpl(final MessageEventTriggerDefinition trigger) {
        messageName = trigger.getMessageName();
        correlations = trigger.getCorrelations();
    }

    public MessageEventTriggerDefinitionImpl() {
        messageName = null;
        correlations = new ArrayList<>(1);
    }

    @Override
    public String getMessageName() {
        return messageName;
    }

    @Override
    public List<CorrelationDefinition> getCorrelations() {
        return Collections.unmodifiableList(correlations);
    }

    public void addCorrelation(final Expression key, final Expression value) {
        correlations.add(new CorrelationDefinitionImpl(key, value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageEventTriggerDefinitionImpl that = (MessageEventTriggerDefinitionImpl) o;
        return Objects.equals(messageName, that.messageName) &&
                Objects.equals(correlations, that.correlations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageName, correlations);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

}
