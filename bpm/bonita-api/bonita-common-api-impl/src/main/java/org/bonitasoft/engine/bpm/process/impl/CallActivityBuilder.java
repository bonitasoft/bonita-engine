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

import org.bonitasoft.engine.bpm.flownode.CallableElementType;
import org.bonitasoft.engine.bpm.flownode.impl.CallActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinitionImpl;
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

    public CallActivityBuilder setCallableElement(final Expression callableElement) {
        ((CallActivityDefinitionImpl) getActivity()).setCallableElement(callableElement);
        return this;
    }

    public CallActivityBuilder setCallableElementVersion(final Expression callableElementVersion) {
        ((CallActivityDefinitionImpl) getActivity()).setCallableElementVersion(callableElementVersion);
        return this;
    }

    public CallActivityBuilder addDataInputOperation(final Operation dataInputOperation) {
        ((CallActivityDefinitionImpl) getActivity()).addDataInputOperation(dataInputOperation);
        return this;
    }

    public CallActivityBuilder addDataOutputOperation(final Operation dataOutputOperation) {
        ((CallActivityDefinitionImpl) getActivity()).addDataOutputOperation(dataOutputOperation);
        return this;
    }

}
