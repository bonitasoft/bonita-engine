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

import org.bonitasoft.engine.bpm.flownode.EventDefinition;
import org.bonitasoft.engine.bpm.flownode.EventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class EventDefinitionImpl extends FlowNodeDefinitionImpl implements EventDefinition {

    private static final long serialVersionUID = 2606127153595667535L;
@XmlElementWrapper(name = "eventTriggers")
@XmlElements({
        @XmlElement(name = "ErrorEventTrigger",type = CatchErrorEventTriggerDefinitionImpl.class),
        @XmlElement(name = "MessageEventTrigger",type = CatchMessageEventTriggerDefinitionImpl.class),
        @XmlElement(name = "CatchSignalEventTrigger",type = CatchSignalEventTriggerDefinitionImpl.class),
        @XmlElement(name = "TerminateEventTrigger",type = TerminateEventTriggerDefinitionImpl.class),
        @XmlElement(name = "ThrowErrorEventTrigger",type = ThrowErrorEventTriggerDefinitionImpl.class),
        @XmlElement(name = "ThrowMessageEventTrigger",type = ThrowMessageEventTriggerDefinitionImpl.class),
        @XmlElement(name = "ThrowSignalEventTrigger",type = ThrowSignalEventTriggerDefinitionImpl.class),
        @XmlElement(name = "TimerEventTrigger",type = TimerEventTriggerDefinitionImpl.class),
})
    private final List<EventTriggerDefinition> eventTriggers;

    public EventDefinitionImpl(final String name) {
        super(name);
        eventTriggers = new ArrayList<>();
    }

    public EventDefinitionImpl(final long id, final String name) {
        super(id, name);
        eventTriggers = new ArrayList<>();
    }

    public EventDefinitionImpl() {
        super();
        eventTriggers = new ArrayList<>();
    }
    @Override
    public List<EventTriggerDefinition> getEventTriggers() {
        return Collections.unmodifiableList(eventTriggers);
    }

    protected void addEventTrigger(final EventTriggerDefinition eventTrigger) {
        eventTriggers.add(eventTrigger);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (eventTriggers == null ? 0 : eventTriggers.hashCode());
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
        final EventDefinitionImpl other = (EventDefinitionImpl) obj;
        if (eventTriggers == null) {
            if (other.eventTriggers != null) {
                return false;
            }
        } else if (!eventTriggers.equals(other.eventTriggers)) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
