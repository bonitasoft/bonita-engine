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
import org.bonitasoft.engine.bpm.flownode.impl.internal.ReceiveTaskDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Julien Molinaro
 * @author Matthieu Chaffotte
 */
public class ReceiveTaskDefinitionBuilder extends ActivityDefinitionBuilder {

    public ReceiveTaskDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl process,
            final String name, final String messageName) {
        super(process, processDefinitionBuilder, new ReceiveTaskDefinitionImpl(name, messageName));
    }

    /**
     * Adds a correlation on this receive task.
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
    public ReceiveTaskDefinitionBuilder addCorrelation(final Expression correlationKey, final Expression value) {
        final ReceiveTaskDefinitionImpl receiveTask = getActivity();
        receiveTask.addCorrelation(correlationKey, value);
        if (receiveTask.getTrigger().getCorrelations().size() > 5) {
            getProcessBuilder().addError("The limit of correlation keys are 5 on receive task: " + receiveTask.getName());
        }
        return this;
    }

    /**
     * Adds the given operation on this message event. Operations added here can be used to initialize process data from message content.
     *
     * @param operation
     *            operation to be added
     * @return
     */
    public ReceiveTaskDefinitionBuilder addMessageOperation(final Operation operation) {
        getActivity().addMessageOperation(operation);
        return this;
    }

    @Override
    ReceiveTaskDefinitionImpl getActivity() {
        return (ReceiveTaskDefinitionImpl) super.getActivity();
    }

}
