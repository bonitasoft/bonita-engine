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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SCallableElementType;
import org.bonitasoft.engine.core.process.definition.model.impl.SCallActivityDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SCallActivityDefinitionBinding extends SActivityDefinitionBinding {

    private SExpression callableElement;

    private SExpression callableElementVersion;

    private final List<SOperation> dataInputOperations = new ArrayList<SOperation>(3);

    private final List<SOperation> dataOutOperations = new ArrayList<SOperation>(3);

    private SCallableElementType callableElementType;

    @Override
    public Object getObject() {
        final SCallActivityDefinitionImpl callActivityDefinitionImpl = new SCallActivityDefinitionImpl(id, name);
        fillNode(callActivityDefinitionImpl);
        return callActivityDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.CALL_ACTIVITY_NODE;
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLSProcessDefinition.CALLABLE_ELEMENT_NODE.equals(name)) {
            callableElement = (SExpression) value;
        } else if (XMLSProcessDefinition.CALLABLE_ELEMENT_VERSION_NODE.equals(name)) {
            callableElementVersion = (SExpression) value;
        } else if (XMLSProcessDefinition.DATA_INPUT_OPERATION_NODE.equals(name)) {
            dataInputOperations.add((SOperation) value);
        } else if (XMLSProcessDefinition.DATA_OUTPUT_OPERATION_NODE.equals(name)) {
            dataOutOperations.add((SOperation) value);
        }
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        callableElementType = SCallableElementType.valueOf(attributes.get(XMLSProcessDefinition.CALLABLE_ELEMENT_TYPE));
    }

    protected void fillNode(final SCallActivityDefinitionImpl callActivity) {
        super.fillNode(callActivity);
        callActivity.setCallableElement(callableElement);
        callActivity.setCallableElementVersion(callableElementVersion);
        for (final SOperation operation : dataInputOperations) {
            callActivity.addDataInputOperation(operation);
        }
        for (final SOperation operation : dataOutOperations) {
            callActivity.addDataOutputOperation(operation);
        }
        callActivity.setCallableElementType(callableElementType);
    }

}
