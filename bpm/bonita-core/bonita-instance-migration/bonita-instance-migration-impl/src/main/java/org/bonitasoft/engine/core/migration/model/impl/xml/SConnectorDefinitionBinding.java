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
package org.bonitasoft.engine.core.migration.model.impl.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.core.migration.model.impl.SConnectorDefinitionWithEnablementImpl;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Baptiste Mesta
 */
public class SConnectorDefinitionBinding extends ElementBinding {

    private String connectorId;

    private String version;

    private final Map<String, SExpression> inputs = new HashMap<String, SExpression>();

    private final List<SOperation> outputs = new ArrayList<SOperation>();

    private String name;

    private SExpression expression;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        name = attributes.get(XMLSMigrationPlan.NAME);
        connectorId = attributes.get(XMLSMigrationPlan.CONNECTOR_ID);
        version = attributes.get(XMLSMigrationPlan.CONNECTOR_VERSION);
    }

    @Override
    public Object getObject() {
        final SConnectorDefinitionImpl sConnectorDefinitionImpl = new SConnectorDefinitionImpl(name, connectorId, version);
        // connectorDefinitionImpl.setId(id); TODO : Uncomment when generate id
        for (final Entry<String, SExpression> entry : inputs.entrySet()) {
            sConnectorDefinitionImpl.addInput(entry.getKey(), entry.getValue());
        }
        for (final SOperation operation : outputs) {
            sConnectorDefinitionImpl.addOutput(operation);
        }
        final SConnectorDefinitionWithEnablementImpl sConnectorDefinitionWithEnablementImpl = new SConnectorDefinitionWithEnablementImpl(
                sConnectorDefinitionImpl, expression);
        return sConnectorDefinitionWithEnablementImpl;
    }

    @Override
    public String getElementTag() {
        return XMLSMigrationPlan.CONNECTOR_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSMigrationPlan.CONNECTOR_INPUT.equals(name)) {
            final Entry<?, ?> entry = (Entry<?, ?>) value;
            inputs.put((String) entry.getKey(), (SExpression) entry.getValue());
        }
        if (XMLSMigrationPlan.OPERATION_NODE.equals(name)) {
            outputs.add((SOperation) value);
        }
        if (XMLSMigrationPlan.ENABLEMENT.equals(name)) {
            expression = (SExpression) value;
        }

    }

}
