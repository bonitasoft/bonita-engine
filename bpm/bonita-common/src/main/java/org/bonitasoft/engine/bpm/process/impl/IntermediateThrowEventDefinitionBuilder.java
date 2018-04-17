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

import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateThrowEventDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class IntermediateThrowEventDefinitionBuilder extends FlowElementContainerBuilder implements DescriptionBuilder {

    private final IntermediateThrowEventDefinitionImpl event;

    public IntermediateThrowEventDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final String eventName) {
        super(container, processDefinitionBuilder);
        event = new IntermediateThrowEventDefinitionImpl(eventName);
        container.addIntermediateThrowEvent(event);
    }

    /**
     * Adds a message on this event
     * @param messageName name of message to be sent
     * @param targetProcess target process
     * @param targetFlowNode target flow node
     * @return
     */
    public ThrowMessageEventTriggerBuilder addMessageEventTrigger(final String messageName, final Expression targetProcess, final Expression targetFlowNode) {
        return new ThrowMessageEventTriggerBuilder(getProcessBuilder(), getContainer(), event, messageName, targetProcess, targetFlowNode);
    }

    /**
     * Adds a message on this event
     * @param messageName name of message to be sent
     * @param targetProcess target process
     * @return
     */
    public ThrowMessageEventTriggerBuilder addMessageEventTrigger(final String messageName, final Expression targetProcess) {
        return new ThrowMessageEventTriggerBuilder(getProcessBuilder(), getContainer(), event, messageName, targetProcess);
    }


    /**
     * Adds a signal on this event
     * @param signalName name of the signal to be thrown
     * @return
     */
    public ThrowSignalEventTriggerBuilder addSignalEventTrigger(final String signalName) {
        return new ThrowSignalEventTriggerBuilder(getProcessBuilder(), getContainer(), event, signalName);
    }

    @Override
    public IntermediateThrowEventDefinitionBuilder addDescription(final String description) {
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
    public IntermediateThrowEventDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
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
    public IntermediateThrowEventDefinitionBuilder addDisplayName(final Expression displayName) {
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
    public IntermediateThrowEventDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        event.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

}
