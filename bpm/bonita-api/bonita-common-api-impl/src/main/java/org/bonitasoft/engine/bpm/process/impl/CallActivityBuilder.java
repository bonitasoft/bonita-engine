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

import org.bonitasoft.engine.bpm.flownode.CallableElementType;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CallActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class CallActivityBuilder extends ActivityDefinitionBuilder {

    public CallActivityBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container, final String name,
            final Expression callableElement, final Expression callableElementVersion) {
        super(container, processDefinitionBuilder, createNewCallActivity(name, callableElement, callableElementVersion));
    }

    private static CallActivityDefinitionImpl createNewCallActivity(final String name, final Expression callableElement, final Expression callableElementVersion) {
        final CallActivityDefinitionImpl activity = new CallActivityDefinitionImpl(name);
        activity.setCallableElement(callableElement);
        activity.setCallableElementVersion(callableElementVersion);
        activity.setCallableElementType(CallableElementType.PROCESS);
        return activity;
    }

    /**
     * Sets the name of target process
     * 
     * @param callableElement
     *            expression representing the process name
     * @return
     */
    public CallActivityBuilder setCallableElement(final Expression callableElement) {
        ((CallActivityDefinitionImpl) getActivity()).setCallableElement(callableElement);
        return this;
    }

    /**
     * Sets the version of target process
     * 
     * @param callableElementVersion
     *            expression representing the process version
     * @return
     */
    public CallActivityBuilder setCallableElementVersion(final Expression callableElementVersion) {
        ((CallActivityDefinitionImpl) getActivity()).setCallableElementVersion(callableElementVersion);
        return this;
    }

    /**
     * Adds a data input operation on this call activity. Data input operations will be evaluated during the target process instantiation and can be used to
     * transfer data from the caller process to the called one
     * 
     * @param dataInputOperation
     *            data input operation
     * @return
     */
    public CallActivityBuilder addDataInputOperation(final Operation dataInputOperation) {
        ((CallActivityDefinitionImpl) getActivity()).addDataInputOperation(dataInputOperation);
        return this;
    }

    /**
     * Adds a data output operation on this call activity. Data output operations will be evaluated during the target process completion and can be used to transfer data from called process to the caller one
     * 
     * @param dataOutputOperation
     *            data output operation
     * @return
     */
    public CallActivityBuilder addDataOutputOperation(final Operation dataOutputOperation) {
        ((CallActivityDefinitionImpl) getActivity()).addDataOutputOperation(dataOutputOperation);
        return this;
    }

}
