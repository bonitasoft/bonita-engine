/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.StartEventDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class StartEventDefinitionBuilder extends FlowElementContainerBuilder implements DescriptionBuilder {

    private final StartEventDefinitionImpl startEvent;

    StartEventDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final String eventName) {
        super(container, processDefinitionBuilder);
        startEvent = new StartEventDefinitionImpl(eventName);
        container.addStartEvent(startEvent);
    }

    public TimerEventTriggerDefinitionBuilder addTimerEventTriggerDefinition(final TimerType timerType, final Expression timerValue) {
        return new TimerEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), startEvent, timerType, timerValue);
    }

    public CatchMessageEventTriggerDefinitionBuilder addMessageEventTrigger(final String messageName) {
        return new CatchMessageEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), startEvent, messageName);
    }

    public CatchSignalEventTriggerDefinitionBuilder addSignalEventTrigger(final String signalName) {
        return new CatchSignalEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), startEvent, signalName);
    }

    public CatchErrorEventTiggerDefinitionBuilder addErrorEventTrigger(final String errorCode) {
        return new CatchErrorEventTiggerDefinitionBuilder(getProcessBuilder(), getContainer(), startEvent, errorCode);
    }

    public CatchErrorEventTiggerDefinitionBuilder addErrorEventTrigger() {
        return new CatchErrorEventTiggerDefinitionBuilder(getProcessBuilder(), getContainer(), startEvent);
    }

    @Override
    public StartEventDefinitionBuilder addDescription(final String description) {
        startEvent.setDescription(description);
        return this;
    }

    public StartEventDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
        startEvent.setDisplayDescription(displayDescription);
        return this;
    }

    public StartEventDefinitionBuilder addDisplayName(final Expression displayName) {
        startEvent.setDisplayName(displayName);
        return this;
    }

    public StartEventDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        startEvent.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

}
