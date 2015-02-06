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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.EventDefinition;
import org.bonitasoft.engine.bpm.flownode.EventTriggerDefinition;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public abstract class EventDefinitionImpl extends FlowNodeDefinitionImpl implements EventDefinition {

    private static final long serialVersionUID = 2606127153595667535L;

    private final List<EventTriggerDefinition> eventTriggers;

    public EventDefinitionImpl(final String name) {
        super(name);
        eventTriggers = new ArrayList<EventTriggerDefinition>();
    }

    public EventDefinitionImpl(final long id, final String name) {
        super(id, name);
        eventTriggers = new ArrayList<EventTriggerDefinition>();
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

}
