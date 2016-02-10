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

import org.bonitasoft.engine.bpm.flownode.TerminateEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.EndEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.TerminateEventTriggerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 */
public class EndEventDefinitionBuilder extends FlowElementContainerBuilder implements DescriptionBuilder {

    private final EndEventDefinitionImpl endEvent;

    EndEventDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container, final String name) {
        super(container, processDefinitionBuilder);
        endEvent = new EndEventDefinitionImpl(name);
        container.addEndEvent(endEvent);
    }

    /**
     * Adds a signal on this event
     * @param signalName name of the signal to be thrown
     * @return
     */
    public ThrowSignalEventTriggerBuilder addSignalEventTrigger(final String signalName) {
        return new ThrowSignalEventTriggerBuilder(getProcessBuilder(), getContainer(), endEvent, signalName);
    }

    /**
     * Adds a message on this event
     * @param messageName name of message to be sent
     * @param targetProcess target process
     * @param targetFlowNode target flow node
     * @return
     */
    public ThrowMessageEventTriggerBuilder addMessageEventTrigger(final String messageName, final Expression targetProcess, final Expression targetFlowNode) {
        return new ThrowMessageEventTriggerBuilder(getProcessBuilder(), getContainer(), endEvent, messageName, targetProcess, targetFlowNode);
    }

    /**
     * Adds a message on this event
     * @param messageName name of message to be sent
     * @param targetProcess target process
     * @return
     */
    public ThrowMessageEventTriggerBuilder addMessageEventTrigger(final String messageName, final Expression targetProcess) {
        return new ThrowMessageEventTriggerBuilder(getProcessBuilder(), getContainer(), endEvent, messageName, targetProcess);
    }

    /**
     * Adds an error on this event 
     * @param errorCode error code of the error to be thrown
     * @return
     */
    public ThrowErrorEventTriggerBuilder addErrorEventTrigger(final String errorCode) {
        return new ThrowErrorEventTriggerBuilder(getProcessBuilder(), getContainer(), endEvent, errorCode);
    }

    /**
     * Marks this event as a terminate event
     * @return
     */
    public EndEventDefinitionBuilder addTerminateEventTrigger() {
        final TerminateEventTriggerDefinition triggerDefinition = new TerminateEventTriggerDefinitionImpl();
        endEvent.setTerminateEventTriggerDefinition(triggerDefinition);
        return this;
    }

    @Override
    public EndEventDefinitionBuilder addDescription(final String description) {
        endEvent.setDescription(description);
        return this;
    }

    /**
    * Sets the display description on this event
    * 
    * @param displayDescription
    *            expression representing the display description
    * @return
    */
    public EndEventDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
        endEvent.setDisplayDescription(displayDescription);
        return this;
    }

    /**
     * Sets the display name on this event
     * 
     * @param displayName
     *            expression representing the display name
     * @return
     */
    public EndEventDefinitionBuilder addDisplayName(final Expression displayName) {
        endEvent.setDisplayName(displayName);
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
    public EndEventDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        endEvent.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

}
