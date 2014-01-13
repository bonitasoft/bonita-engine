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

import org.bonitasoft.engine.bpm.flownode.TerminateEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.EndEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.TerminateEventTriggerDefinitionImpl;
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

    public ThrowSignalEventTriggerBuilder addSignalEventTrigger(final String signalName) {
        return new ThrowSignalEventTriggerBuilder(getProcessBuilder(), getContainer(), endEvent, signalName);
    }

    public ThrowMessageEventTriggerBuilder addMessageEventTrigger(final String messageName, final Expression targetProcess, final Expression targetFlowNode) {
        return new ThrowMessageEventTriggerBuilder(getProcessBuilder(), getContainer(), endEvent, messageName, targetProcess, targetFlowNode);
    }

    public ThrowMessageEventTriggerBuilder addMessageEventTrigger(final String messageName, final Expression targetProcess) {
        return new ThrowMessageEventTriggerBuilder(getProcessBuilder(), getContainer(), endEvent, messageName, targetProcess);
    }

    public ThrowErrorEventTriggerBuilder addErrorEventTrigger(final String errorCode) {
        return new ThrowErrorEventTriggerBuilder(getProcessBuilder(), getContainer(), endEvent, errorCode);
    }

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

    public EndEventDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
        endEvent.setDisplayDescription(displayDescription);
        return this;
    }

    public EndEventDefinitionBuilder addDisplayName(final Expression displayName) {
        endEvent.setDisplayName(displayName);
        return this;
    }

    public EndEventDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        endEvent.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

}
