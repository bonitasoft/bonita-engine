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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class MessageEventTriggerDefinitionImpl implements MessageEventTriggerDefinition {

    private static final long serialVersionUID = -190616505159460399L;
    @XmlAttribute
    private final String messageName;
    @XmlElementWrapper(name = "correlations")
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
        messageName = "default name";
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (correlations == null ? 0 : correlations.hashCode());
        result = prime * result + (messageName == null ? 0 : messageName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MessageEventTriggerDefinitionImpl other = (MessageEventTriggerDefinitionImpl) obj;
        if (correlations == null) {
            if (other.correlations != null) {
                return false;
            }
        } else if (!correlations.equals(other.correlations)) {
            return false;
        }
        if (messageName == null) {
            if (other.messageName != null) {
                return false;
            }
        } else if (!messageName.equals(other.messageName)) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

}
