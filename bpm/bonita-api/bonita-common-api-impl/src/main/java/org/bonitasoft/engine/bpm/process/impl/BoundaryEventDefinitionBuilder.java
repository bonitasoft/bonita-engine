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
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.BoundaryEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class BoundaryEventDefinitionBuilder extends FlowElementContainerBuilder implements DescriptionBuilder {

    private final BoundaryEventDefinitionImpl boundaryEvent;

    public BoundaryEventDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl process,
            final ActivityDefinitionImpl activity, final String name, final boolean isInterruptiong) {
        super(process, processDefinitionBuilder);
        boundaryEvent = new BoundaryEventDefinitionImpl(name);
        boundaryEvent.setInterrupting(isInterruptiong);
        activity.addBoundaryEventDefinition(boundaryEvent);
    }

    @Override
    public BoundaryEventDefinitionBuilder addDescription(final String description) {
        boundaryEvent.setDescription(description);
        return this;
    }

    /**
     * Sets the display description on this element.
     * 
     * @param displayDescription
     *        expression representing the display description.
     * @return
     */
    public BoundaryEventDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
        boundaryEvent.setDisplayDescription(displayDescription);
        return this;
    }

    /**
     * Sets the display name on this element.
     * 
     * @param displayName
     *        expression representing the display name.
     * @return
     */
    public BoundaryEventDefinitionBuilder addDisplayName(final Expression displayName) {
        boundaryEvent.setDisplayName(displayName);
        return this;
    }

    /**
     * Sets the display description after completion on this event. This will be used to updated the display description when the activity completes its
     * execution.
     * 
     * @param displayDescriptionAfterCompletion
     *        expression representing the new display description after the event completion.
     * @return
     */
    public BoundaryEventDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        boundaryEvent.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

    /**
     * Adds a timer trigger on this boundary event.
     * 
     * @param timerType timer type.
     * @param timerValue expression representing the timer value.
     * @return
     */
    public TimerEventTriggerDefinitionBuilder addTimerEventTriggerDefinition(final TimerType timerType, final Expression timerValue) {
        return new TimerEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), boundaryEvent, timerType, timerValue);
    }

    /**
     * Adds a message trigger on this boundary event.
     * 
     * @param messageName name of the message to be received.
     * @return
     */
    public CatchMessageEventTriggerDefinitionBuilder addMessageEventTrigger(final String messageName) {
        return new CatchMessageEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), boundaryEvent, messageName);
    }

    /**
     * Adds a signal trigger on this boundary event.
     * 
     * @param signalName name of the signal to be received.
     * @return
     */
    public CatchSignalEventTriggerDefinitionBuilder addSignalEventTrigger(final String signalName) {
        return new CatchSignalEventTriggerDefinitionBuilder(getProcessBuilder(), getContainer(), boundaryEvent, signalName);
    }

    /**
     * Adds an error trigger on this boundary event.
     * 
     * @param errorCode the error code to be caught.
     * @return
     */
    public CatchErrorEventTiggerDefinitionBuilder addErrorEventTrigger(final String errorCode) {
        return new CatchErrorEventTiggerDefinitionBuilder(getProcessBuilder(), getContainer(), boundaryEvent, errorCode);
    }

    /**
     * Adds an error trigger on this boundary event. As no error code is specified all errors will be caught.
     * 
     * @return
     */
    public CatchErrorEventTiggerDefinitionBuilder addErrorEventTrigger() {
        return new CatchErrorEventTiggerDefinitionBuilder(getProcessBuilder(), getContainer(), boundaryEvent);
    }

}
