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

import org.bonitasoft.engine.bpm.flownode.impl.internal.CatchEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CatchMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class CatchMessageEventTriggerDefinitionBuilder extends FlowElementContainerBuilder {

    protected final CatchMessageEventTriggerDefinitionImpl messageTrigger;

    public CatchMessageEventTriggerDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder,
            final FlowElementContainerDefinitionImpl container, final CatchEventDefinitionImpl event, final String messageName) {
        super(container, processDefinitionBuilder);
        messageTrigger = new CatchMessageEventTriggerDefinitionImpl(messageName);
        event.addMessageEventTrigger(messageTrigger);
    }

    /**
     * Adds the given operation on this message event. Operations added here can be used to initialize process data from message content.
     * 
     * @param operation
     *            operation to be added
     * @return
     */
    public CatchMessageEventTriggerDefinitionBuilder addOperation(final Operation operation) {
        messageTrigger.addOperation(operation);
        return this;
    }

    /**
     * Adds a correlation on this message event.
     * <p> 
     * It's possible to define up to five correlations. If more then five correlations are defined, the
     * process becomes invalid.
     * <p>
     * The expressions representing correlation key and correlation value are evaluated once during the flow node initialization 
     * 
     * @param correlationKey expression representing the correlation key
     * @param value expression representing the correlation value
     * @return
     */
    public CatchMessageEventTriggerDefinitionBuilder addCorrelation(final Expression correlationKey, final Expression value) {
        messageTrigger.addCorrelation(correlationKey, value);
        if (messageTrigger.getCorrelations().size() > 5) {
            getProcessBuilder().addError("Too much correlation on catch message: " + messageTrigger.getMessageName());
        }
        return this;
    }

}
