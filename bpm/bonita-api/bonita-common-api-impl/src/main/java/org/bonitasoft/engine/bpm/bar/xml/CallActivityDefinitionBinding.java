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
package org.bonitasoft.engine.bpm.bar.xml;

import org.bonitasoft.engine.bpm.flownode.CallableElementType;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CallActivityDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class CallActivityDefinitionBinding extends ActivityDefinitionBinding {

    private Expression callableElement;

    private Expression callableElementVersion;

    private final List<Operation> dataInputOperations = new ArrayList<>(3);

    private final Map<String, Expression> contractInputs = new HashMap<>();

    private final List<Operation> dataOutOperations = new ArrayList<>(3);

    private CallableElementType callableElementType;

    @Override
    public Object getObject() {
        final CallActivityDefinitionImpl callActivityDefinitionImpl = new CallActivityDefinitionImpl(id, name);
        fillNode(callActivityDefinitionImpl);
        return callActivityDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.CALL_ACTIVITY_NODE;
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.CALLABLE_ELEMENT_NODE.equals(name)) {
            callableElement = (Expression) value;
        } else if (XMLProcessDefinition.CALLABLE_ELEMENT_VERSION_NODE.equals(name)) {
            callableElementVersion = (Expression) value;
        } else if (XMLProcessDefinition.DATA_INPUT_OPERATION_NODE.equals(name)) {
            dataInputOperations.add((Operation) value);
        } else if (XMLProcessDefinition.CONTRACT_INPUT_EXPRESSION_NODE.equals(name)) {
            final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) value;
            contractInputs.put((String) entry.getKey(), (Expression) entry.getValue());
        } else if (XMLProcessDefinition.DATA_OUTPUT_OPERATION_NODE.equals(name)) {
            dataOutOperations.add((Operation) value);
        }
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        callableElementType = CallableElementType.valueOf(attributes.get(XMLProcessDefinition.CALLABLE_ELEMENT_TYPE));
    }

    protected void fillNode(final CallActivityDefinitionImpl callActivity) {
        super.fillNode(callActivity);
        callActivity.setCallableElement(callableElement);
        callActivity.setCallableElementVersion(callableElementVersion);
        for (final Operation operation : dataInputOperations) {
            callActivity.addDataInputOperation(operation);
        }
        for (Map.Entry<String, Expression> entry : contractInputs.entrySet()) {
            callActivity.addProcessStartContractInput(entry.getKey(), entry.getValue());
        }
        for (final Operation operation : dataOutOperations) {
            callActivity.addDataOutputOperation(operation);
        }
        callActivity.setCallableElementType(callableElementType);
    }

}
