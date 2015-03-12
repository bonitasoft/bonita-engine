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

import org.bonitasoft.engine.bpm.flownode.ThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowSignalEventTriggerDefinition;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class ThrowEventDefinitionImpl extends EventDefinitionImpl implements ThrowEventDefinition {

    private static final long serialVersionUID = -3142554305988571206L;

    private final List<ThrowMessageEventTriggerDefinition> messageEventTriggerDefinitions;

    private final List<ThrowSignalEventTriggerDefinition> signalEventTriggerDefinitions;

    public ThrowEventDefinitionImpl(final String name) {
        super(name);
        messageEventTriggerDefinitions = new ArrayList<ThrowMessageEventTriggerDefinition>(1);
        signalEventTriggerDefinitions = new ArrayList<ThrowSignalEventTriggerDefinition>(1);
    }

    public ThrowEventDefinitionImpl(final long id, final String name) {
        super(id, name);
        messageEventTriggerDefinitions = new ArrayList<ThrowMessageEventTriggerDefinition>(1);
        signalEventTriggerDefinitions = new ArrayList<ThrowSignalEventTriggerDefinition>(1);
    }

    @Override
    public List<ThrowMessageEventTriggerDefinition> getMessageEventTriggerDefinitions() {
        return Collections.unmodifiableList(messageEventTriggerDefinitions);
    }

    public void addMessageEventTriggerDefinition(final ThrowMessageEventTriggerDefinition messageEventTriggerDefinition) {
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (messageEventTriggerDefinitions == null ? 0 : messageEventTriggerDefinitions.hashCode());
        result = prime * result + (signalEventTriggerDefinitions == null ? 0 : signalEventTriggerDefinitions.hashCode());
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
        final ThrowEventDefinitionImpl other = (ThrowEventDefinitionImpl) obj;
        if (messageEventTriggerDefinitions == null) {
            if (other.messageEventTriggerDefinitions != null) {
                return false;
            }
        } else if (!messageEventTriggerDefinitions.equals(other.messageEventTriggerDefinitions)) {
            return false;
        }
        if (signalEventTriggerDefinitions == null) {
            if (other.signalEventTriggerDefinitions != null) {
                return false;
            }
        } else if (!signalEventTriggerDefinitions.equals(other.signalEventTriggerDefinitions)) {
            return false;
        }
        return true;
    }

}
