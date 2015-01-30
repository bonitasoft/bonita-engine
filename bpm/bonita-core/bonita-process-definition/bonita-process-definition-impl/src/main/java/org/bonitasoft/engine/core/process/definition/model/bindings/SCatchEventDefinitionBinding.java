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
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SCatchEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchSignalEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerEventTriggerDefinition;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class SCatchEventDefinitionBinding extends SFlowNodeDefinitionBinding {

    private final List<STimerEventTriggerDefinition> timerEventTriggers;

    private final List<SCatchMessageEventTriggerDefinition> messageEventTriggers;

    private final List<SCatchSignalEventTriggerDefinition> signalEventTriggers;

    private boolean isInterrupting;

    public SCatchEventDefinitionBinding() {
        super();
        timerEventTriggers = new ArrayList<STimerEventTriggerDefinition>(1);
        messageEventTriggers = new ArrayList<SCatchMessageEventTriggerDefinition>(1);
        signalEventTriggers = new ArrayList<SCatchSignalEventTriggerDefinition>(1);
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        isInterrupting = Boolean.parseBoolean(attributes.get(XMLSProcessDefinition.INTERRUPTING));
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        super.setChildElement(name, value, attributes);
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLSProcessDefinition.TIMER_EVENT_TRIGGER_NODE.equals(name)) {
            timerEventTriggers.add((STimerEventTriggerDefinition) value);
        } else if (XMLSProcessDefinition.CATCH_MESSAGE_EVENT_TRIGGER_NODE.equals(name)) {
            messageEventTriggers.add((SCatchMessageEventTriggerDefinition) value);
        } else if (XMLSProcessDefinition.CATCH_SIGNAL_EVENT_TRIGGER_NODE.equals(name)) {
            signalEventTriggers.add((SCatchSignalEventTriggerDefinition) value);
        }
    }

    @Override
    protected void fillNode(final SFlowNodeDefinition flowNode) {
        super.fillNode(flowNode);
        final SCatchEventDefinitionImpl catchEventDefinition = (SCatchEventDefinitionImpl) flowNode;
        for (final STimerEventTriggerDefinition timerEventTrigger : timerEventTriggers) {
            catchEventDefinition.addTimerEventTrigger(timerEventTrigger);
        }
        for (final SCatchMessageEventTriggerDefinition messageEventTrigger : messageEventTriggers) {
            catchEventDefinition.addMessageEventTrigger(messageEventTrigger);
        }
        for (final SCatchSignalEventTriggerDefinition signalEventTrigger : signalEventTriggers) {
            catchEventDefinition.addSignalEventTrigger(signalEventTrigger);
        }
        catchEventDefinition.setInterrupting(isInterrupting);
    }

}
