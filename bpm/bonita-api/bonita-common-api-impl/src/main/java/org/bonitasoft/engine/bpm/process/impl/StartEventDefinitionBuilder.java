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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StartEventDefinitionImpl;
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

    /**
     * Adds a timer trigger on this event.
     * 
     * @param timerType
     *            timer type.
     * @param timerValue
     *            expression representing the timer value.
     * @return
     */
    public TimerEventTriggerDefinitionBuilder addTimerEventTriggerDefinition(final TimerType timerType, final Expression timerValue) {
        return new TimerEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), startEvent, timerType, timerValue);
    }

    /**
     * Adds a message trigger on this event.
     * 
     * @param messageName
     *            name of the message to be received.
     * @return
     */
    public CatchMessageEventTriggerDefinitionBuilder addMessageEventTrigger(final String messageName) {
        return new CatchMessageEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), startEvent, messageName);
    }

    /**
     * Adds a signal trigger on this event.
     * 
     * @param signalName
     *            name of the signal to be received.
     * @return
     */
    public CatchSignalEventTriggerDefinitionBuilder addSignalEventTrigger(final String signalName) {
        return new CatchSignalEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), startEvent, signalName);
    }

    /**
     * Adds an error trigger on this event.
     * 
     * @param errorCode
     *            the error code to be caught.
     * @return
     */
    public CatchErrorEventTiggerDefinitionBuilder addErrorEventTrigger(final String errorCode) {
        return new CatchErrorEventTiggerDefinitionBuilder(getProcessBuilder(), getContainer(), startEvent, errorCode);
    }

    /**
     * Adds an error trigger on this boundary event. As no error code is specified all errors will be caught
     * 
     * @return
     */
    public CatchErrorEventTiggerDefinitionBuilder addErrorEventTrigger() {
        return new CatchErrorEventTiggerDefinitionBuilder(getProcessBuilder(), getContainer(), startEvent);
    }

    @Override
    public StartEventDefinitionBuilder addDescription(final String description) {
        startEvent.setDescription(description);
        return this;
    }

    /**
     * Sets the display description on this element.
     * 
     * @param displayDescription
     *            expression representing the display description.
     * @return
     */
    public StartEventDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
        startEvent.setDisplayDescription(displayDescription);
        return this;
    }

    /**
     * Sets the display name on this element.
     * 
     * @param displayName
     *            expression representing the display name.
     * @return
     */
    public StartEventDefinitionBuilder addDisplayName(final Expression displayName) {
        startEvent.setDisplayName(displayName);
        return this;
    }

    /**
     * Sets the display description after completion on this event. This will be used to updated the display description when the activity completes its
     * execution.
     * 
     * @param displayDescriptionAfterCompletion
     *            expression representing the new display description after the event completion.
     * @return
     */
    public StartEventDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        startEvent.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

}
