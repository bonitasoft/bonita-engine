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

import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.IntermediateThrowEventDefinitionImpl;
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

    public ThrowMessageEventTriggerBuilder addMessageEventTrigger(final String messageName, final Expression targetProcess, final Expression targetFlowNode) {
        return new ThrowMessageEventTriggerBuilder(getProcessBuilder(), getContainer(), event, messageName, targetProcess, targetFlowNode);
    }

    public ThrowMessageEventTriggerBuilder addMessageEventTrigger(final String messageName, final Expression targetProcess) {
        return new ThrowMessageEventTriggerBuilder(getProcessBuilder(), getContainer(), event, messageName, targetProcess);
    }

    public ThrowSignalEventTriggerBuilder addSignalEventTrigger(final String signalName) {
        return new ThrowSignalEventTriggerBuilder(getProcessBuilder(), getContainer(), event, signalName);
    }

    @Override
    public IntermediateThrowEventDefinitionBuilder addDescription(final String description) {
        event.setDescription(description);
        return this;
    }

    public IntermediateThrowEventDefinitionBuilder addDisplayDescription(final Expression displayDescription) {
        event.setDisplayDescription(displayDescription);
        return this;
    }

    public IntermediateThrowEventDefinitionBuilder addDisplayName(final Expression displayName) {
        event.setDisplayName(displayName);
        return this;
    }

    public IntermediateThrowEventDefinitionBuilder addDisplayDescriptionAfterCompletion(final Expression displayDescriptionAfterCompletion) {
        event.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        return this;
    }

}
