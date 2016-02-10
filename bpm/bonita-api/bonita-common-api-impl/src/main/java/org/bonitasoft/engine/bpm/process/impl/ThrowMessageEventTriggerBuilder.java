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
import org.bonitasoft.engine.bpm.flownode.impl.internal.ThrowEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ThrowMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class ThrowMessageEventTriggerBuilder extends FlowElementContainerBuilder {

    private final ThrowMessageEventTriggerDefinitionImpl messageTrigger;

    private final ProcessDefinitionBuilder processDefinitionBuilder2;

    protected ThrowMessageEventTriggerBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final ThrowEventDefinitionImpl event, final String messageName, final Expression targetProcess, final Expression targetFlowNode) {
        super(container, processDefinitionBuilder);
        processDefinitionBuilder2 = processDefinitionBuilder;
        messageTrigger = new ThrowMessageEventTriggerDefinitionImpl(messageName, targetProcess, targetFlowNode);
        event.addMessageEventTriggerDefinition(messageTrigger);
    }

    protected ThrowMessageEventTriggerBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final ThrowEventDefinitionImpl event, final String messageName, final Expression targetProcess) {
        super(container, processDefinitionBuilder);
        processDefinitionBuilder2 = processDefinitionBuilder;
        messageTrigger = new ThrowMessageEventTriggerDefinitionImpl(messageName, targetProcess);
        event.addMessageEventTriggerDefinition(messageTrigger);
    }

    /**
     * Sets the target process.
     * @param targetProcess expression representing the process that will receive the message.
     * @return
     */
    public ThrowMessageEventTriggerBuilder setTargetProcess(final Expression targetProcess) {
        messageTrigger.setTargetProcess(targetProcess);
        return this;
    }

    /**
     * Sets the target flow node.
     * @param targetFlowNode expression representing the flow node that will receive the message.
     * @return
     */
    public ThrowMessageEventTriggerBuilder setTargetFlowNode(final Expression targetFlowNode) {
        messageTrigger.setTargetFlowNode(targetFlowNode);
        return this;
    }

    /**
     * Adds a content to this message.
     * @param displayName expression representing the content display name.
     * @param messageContent expression representing the content value.
     * @return
     */
    public DataDefinitionBuilder addMessageContentExpression(final Expression displayName, final Expression messageContent) {
        final String dataName = displayName.getContent(); // FIXME evaluate the expression
        final String className = messageContent.getReturnType();
        return new DataDefinitionBuilder(getProcessBuilder(), getContainer(), messageTrigger, dataName, className, messageContent);
    }

    /**
     * Adds a correlation on this event.
     * <p> 
     * It's possible to define up to five correlations. If more then five correlations are defined, the process becomes invalid.
     * <p>
     * The expressions representing correlation key and correlation value are evaluated once during the flow node initialization. 
     * 
     * @param correlationKey expression representing the correlation key.
     * @param value expression representing the correlation value.
     * @return
     */
    public ThrowMessageEventTriggerBuilder addCorrelation(final Expression correlationKey, final Expression value) {
        messageTrigger.addCorrelation(correlationKey, value);
        if (messageTrigger.getCorrelations().size() > 5) {
            processDefinitionBuilder2.addError("Too much correlation on throw message: " + messageTrigger.getMessageName());
        }
        return this;
    }

}
