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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SThrowEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowSignalEventTriggerDefinition;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class SThrowEventDefinitionBinding extends SFlowNodeDefinitionBinding {

    private final List<SThrowMessageEventTriggerDefinition> messageEventTriggers;

    private final List<SThrowSignalEventTriggerDefinition> signalEventTriggers;

    public SThrowEventDefinitionBinding() {
        messageEventTriggers = new ArrayList<SThrowMessageEventTriggerDefinition>(5);
        signalEventTriggers = new ArrayList<SThrowSignalEventTriggerDefinition>(1);
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLSProcessDefinition.THROW_MESSAGE_EVENT_TRIGGER_NODE.equals(name)) {
            messageEventTriggers.add((SThrowMessageEventTriggerDefinition) value);
        } else if (XMLSProcessDefinition.THROW_SIGNAL_EVENT_TRIGGER_NODE.equals(name)) {
            signalEventTriggers.add((SThrowSignalEventTriggerDefinition) value);
        }
    }

    @Override
    protected void fillNode(final SFlowNodeDefinition flowNode) {
        super.fillNode(flowNode);
        final SThrowEventDefinitionImpl throwEventDefinition = (SThrowEventDefinitionImpl) flowNode;
        for (final SThrowMessageEventTriggerDefinition messageEventTrigger : messageEventTriggers) {
            throwEventDefinition.addMessageEventTriggerDefinition(messageEventTrigger);
        }
        for (final SThrowSignalEventTriggerDefinition signalEventTrigger : signalEventTriggers) {
            throwEventDefinition.addSignalEventTriggerDefinition(signalEventTrigger);
        }
    }

}
