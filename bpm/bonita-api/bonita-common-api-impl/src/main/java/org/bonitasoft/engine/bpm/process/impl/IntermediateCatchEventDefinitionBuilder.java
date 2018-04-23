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
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateCatchEventDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class IntermediateCatchEventDefinitionBuilder extends FlowElementContainerBuilder implements DescriptionBuilder {

    private final IntermediateCatchEventDefinitionImpl event;

    public IntermediateCatchEventDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final String eventName) {
        super(container, processDefinitionBuilder);
        event = new IntermediateCatchEventDefinitionImpl(eventName);
        container.addIntermediateCatchEvent(event);
    }

    /**
     * Adds a timer trigger on this event
     * @param timerType timer type
     * @param timerValue expression representing the timer value
     * @return
     */    
    public TimerEventTriggerDefinitionBuilder addTimerEventTriggerDefinition(final TimerType timerType, final Expression timerValue) {
        return new TimerEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), event, timerType, timerValue);
    }

    /**
     * Adds a message trigger on this event
     * @param messageName name of the message to be caught
     * @return
     */
    public CatchMessageEventTriggerDefinitionBuilder addMessageEventTrigger(final String messageName) {
        return new CatchMessageEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), event, messageName);
    }

    /**
     * Adds a signal trigger on this event
     * @param signalName name of the signal to be caught
     * @return
     */
    public CatchSignalEventTriggerDefinitionBuilder addSignalEventTrigger(final String signalName) {
        return new CatchSignalEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), event, signalName);
    }

    @Override
    public IntermediateCatchEventDefinitionBuilder addDescription(final String description) {
        event.setDescription(description);
        return this;
    }

    /**
     * Sets the display description on this event
     * 
     * @param displayDescription
     *            expression representing the display description
     * @return
     */
    public IntermediateCatchEventDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
        event.setDisplayDescription(displayDescription);
        return this;
    }

    /**
     * Sets the display name on this event
     * 
     * @param displayName
     *            expression representing the display name
     * @return
     */
    public IntermediateCatchEventDefinitionBuilder addDisplayName(final Expression displayName) {
        event.setDisplayName(displayName);
        return this;
    }

    /**
     * Sets the display description after completion on this event. This will be used to updated the display description when the event completes its
     * execution
     * 
     * @param displayDescriptionAfterCompletion
     *            expression representing the new display description after the event completion.
     * @return
     */
    public IntermediateCatchEventDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        event.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

}
