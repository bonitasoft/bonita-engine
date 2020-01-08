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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.flownode.ThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowSignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ThrowEventDefinitionImpl extends EventDefinitionImpl implements ThrowEventDefinition {

    private static final long serialVersionUID = -3142554305988571206L;
    @XmlElement(type = ThrowMessageEventTriggerDefinitionImpl.class, name = "throwMessageEventTrigger")
    private final List<ThrowMessageEventTriggerDefinition> messageEventTriggerDefinitions;
    @XmlElement(type = ThrowSignalEventTriggerDefinitionImpl.class, name = "throwSignalEventTrigger")
    private final List<ThrowSignalEventTriggerDefinition> signalEventTriggerDefinitions;

    public ThrowEventDefinitionImpl(final String name) {
        super(name);
        messageEventTriggerDefinitions = new ArrayList<>(1);
        signalEventTriggerDefinitions = new ArrayList<>(1);
    }

    public ThrowEventDefinitionImpl(final long id, final String name) {
        super(id, name);
        messageEventTriggerDefinitions = new ArrayList<>(1);
        signalEventTriggerDefinitions = new ArrayList<>(1);
    }

    public ThrowEventDefinitionImpl() {
        super();
        messageEventTriggerDefinitions = new ArrayList<>(1);
        signalEventTriggerDefinitions = new ArrayList<>(1);
    }

    @Override
    public List<ThrowMessageEventTriggerDefinition> getMessageEventTriggerDefinitions() {
        return Collections.unmodifiableList(messageEventTriggerDefinitions);
    }

    public void addMessageEventTriggerDefinition(
            final ThrowMessageEventTriggerDefinition messageEventTriggerDefinition) {
        messageEventTriggerDefinitions.add(messageEventTriggerDefinition);
        addEventTrigger(messageEventTriggerDefinition);
    }

    @Override
    public List<ThrowSignalEventTriggerDefinition> getSignalEventTriggerDefinitions() {
        return Collections.unmodifiableList(signalEventTriggerDefinitions);
    }

    public void addSignalEventTriggerDefinition(final ThrowSignalEventTriggerDefinition signalEventTrigger) {
        signalEventTriggerDefinitions.add(signalEventTrigger);
        addEventTrigger(signalEventTrigger);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ThrowEventDefinitionImpl that = (ThrowEventDefinitionImpl) o;
        return Objects.equals(messageEventTriggerDefinitions, that.messageEventTriggerDefinitions) &&
                Objects.equals(signalEventTriggerDefinitions, that.signalEventTriggerDefinitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), messageEventTriggerDefinitions, signalEventTriggerDefinitions);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageEventTriggerDefinitions", messageEventTriggerDefinitions)
                .append("signalEventTriggerDefinitions", signalEventTriggerDefinitions)
                .toString();
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
