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
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchSignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CatchEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowNodeDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class CatchEventDefinitionBinding extends FlowNodeDefinitionBinding {

    private final List<TimerEventTriggerDefinition> timerEventTriggers;

    private final List<CatchMessageEventTriggerDefinition> messageEventTriggers;

    private final List<CatchSignalEventTriggerDefinition> signalEventTriggers;

    boolean isInterrupting;

    public CatchEventDefinitionBinding() {
        super();
        timerEventTriggers = new ArrayList<TimerEventTriggerDefinition>();
        messageEventTriggers = new ArrayList<CatchMessageEventTriggerDefinition>();
        signalEventTriggers = new ArrayList<CatchSignalEventTriggerDefinition>();
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        isInterrupting = Boolean.parseBoolean(attributes.get(XMLProcessDefinition.INTERRUPTING));
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        super.setChildElement(name, value, attributes);
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.TIMER_EVENT_TRIGGER_NODE.equals(name)) {
            timerEventTriggers.add((TimerEventTriggerDefinition) value);
        }
        if (XMLProcessDefinition.CATCH_MESSAGE_EVENT_TRIGGER_NODE.equals(name)) {
            messageEventTriggers.add((CatchMessageEventTriggerDefinition) value);
        }
        if (XMLProcessDefinition.CATCH_SIGNAL_EVENT_TRIGGER_NODE.equals(name)) {
            signalEventTriggers.add((CatchSignalEventTriggerDefinition) value);
        }
    }

    @Override
    protected void fillNode(final FlowNodeDefinitionImpl flowNode) {
        super.fillNode(flowNode);
        final CatchEventDefinitionImpl catchEventDefinition = (CatchEventDefinitionImpl) flowNode;
        for (final TimerEventTriggerDefinition timerEventTrigger : timerEventTriggers) {
            catchEventDefinition.addTimerEventTrigger(timerEventTrigger);
        }
        for (final CatchMessageEventTriggerDefinition messageEventTrigger : messageEventTriggers) {
            catchEventDefinition.addMessageEventTrigger(messageEventTrigger);
        }
        for (final CatchSignalEventTriggerDefinition signalEventTrigger : signalEventTriggers) {
            catchEventDefinition.addSignalEventTrigger(signalEventTrigger);
        }
        catchEventDefinition.setInterrupting(isInterrupting);
    }

}
