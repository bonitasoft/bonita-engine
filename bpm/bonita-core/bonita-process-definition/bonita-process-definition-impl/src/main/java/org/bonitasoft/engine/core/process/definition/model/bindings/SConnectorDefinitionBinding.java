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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.FailAction;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.impl.SConnectorDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SConnectorDefinitionBinding extends SNamedElementBinding {

    private String activationEvent;

    private String connectorId;

    private String version;

    private final Map<String, SExpression> inputs = new HashMap<String, SExpression>();

    private final List<SOperation> outputs = new ArrayList<SOperation>();

    private String failAction;

    private String errorCode;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        activationEvent = attributes.get(XMLSProcessDefinition.CONNECTOR_ACTIVATION_EVENT);
        connectorId = attributes.get(XMLSProcessDefinition.CONNECTOR_ID);
        version = attributes.get(XMLSProcessDefinition.CONNECTOR_VERSION);
        failAction = attributes.get(XMLSProcessDefinition.CONNECTOR_FAIL_ACTION);
        errorCode = attributes.get(XMLSProcessDefinition.CONNECTOR_ERROR_CODE);
    }

    @Override
    public Object getObject() {
        final SConnectorDefinitionImpl connectorDefinitionImpl = new SConnectorDefinitionImpl(name, connectorId, version,
                ConnectorEvent.valueOf(activationEvent));
        connectorDefinitionImpl.setId(id);
        connectorDefinitionImpl.setFailAction(FailAction.valueOf(failAction));
        connectorDefinitionImpl.setErrorCode(errorCode);
        for (final Entry<String, SExpression> entry : inputs.entrySet()) {
            connectorDefinitionImpl.addInput(entry.getKey(), entry.getValue());
        }
        for (final SOperation operation : outputs) {
            connectorDefinitionImpl.addOutput(operation);
        }
        return connectorDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.CONNECTOR_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.CONNECTOR_INPUT.equals(name)) {
            final Entry<?, ?> entry = (Entry<?, ?>) value;
            inputs.put((String) entry.getKey(), (SExpression) entry.getValue());
        } else if (XMLSProcessDefinition.OPERATION_NODE.equals(name)) {
            outputs.add((SOperation) value);
        }
    }

}
