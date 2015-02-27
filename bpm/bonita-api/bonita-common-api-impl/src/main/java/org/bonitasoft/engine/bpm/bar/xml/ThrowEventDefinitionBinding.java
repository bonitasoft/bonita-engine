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
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowSignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowNodeDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ThrowEventDefinitionImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class ThrowEventDefinitionBinding extends FlowNodeDefinitionBinding {

    private final List<ThrowMessageEventTriggerDefinition> messageEventTriggers;

    private final List<ThrowSignalEventTriggerDefinition> signalEventTriggers;

    public ThrowEventDefinitionBinding() {
        messageEventTriggers = new ArrayList<ThrowMessageEventTriggerDefinition>(5);
        signalEventTriggers = new ArrayList<ThrowSignalEventTriggerDefinition>(1);
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.THROW_MESSAGE_EVENT_TRIGGER_NODE.equals(name)) {
            messageEventTriggers.add((ThrowMessageEventTriggerDefinition) value);
        }
        if (XMLProcessDefinition.THROW_SIGNAL_EVENT_TRIGGER_NODE.equals(name)) {
            signalEventTriggers.add((ThrowSignalEventTriggerDefinition) value);
        }
    }

    @Override
    protected void fillNode(final FlowNodeDefinitionImpl flowNode) {
        super.fillNode(flowNode);
        final ThrowEventDefinitionImpl throwEventDefinition = (ThrowEventDefinitionImpl) flowNode;
        for (final ThrowMessageEventTriggerDefinition messageEventTrigger : messageEventTriggers) {
            throwEventDefinition.addMessageEventTriggerDefinition(messageEventTrigger);
        }
        for (final ThrowSignalEventTriggerDefinition signalEventTrigger : signalEventTriggers) {
            throwEventDefinition.addSignalEventTriggerDefinition(signalEventTrigger);
        }
    }

}
