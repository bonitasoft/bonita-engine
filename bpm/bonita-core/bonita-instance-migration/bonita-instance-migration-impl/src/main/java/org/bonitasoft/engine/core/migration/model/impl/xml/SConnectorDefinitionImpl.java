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

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.FailAction;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 */
public class SConnectorDefinitionImpl implements SConnectorDefinition {

    private static final long serialVersionUID = 1157290856193252501L;

    private final Map<String, SExpression> inputs;

    private final List<SOperation> outputs;

    private final String connectorId;

    private final String version;

    private final String name;

    public SConnectorDefinitionImpl(final String name, final String connectorId, final String version) {
        this.name = name;
        this.connectorId = connectorId;
        this.version = version;
        inputs = new HashMap<String, SExpression>();
        outputs = new ArrayList<SOperation>();
    }

    @Override
    public String getConnectorId() {
        return connectorId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public ConnectorEvent getActivationEvent() {
        return ConnectorEvent.ON_ENTER;
    }

    @Override
    public Map<String, SExpression> getInputs() {
        return inputs;
    }

    @Override
    public List<SOperation> getOutputs() {
        return outputs;
    }

    public void addInput(final String key, final SExpression value) {
        inputs.put(key, value);
    }

    public void addOutput(final SOperation operation) {
        outputs.add(operation);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getId() {
        return 0L;
    }

    @Override
    public FailAction getFailAction() {
        return null;
    }

    @Override
    public String getErrorCode() {
        return null;
    }

}
