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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.FailAction;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Baptiste Mesta
 */
public class SConnectorDefinitionImpl extends SNamedElementImpl implements SConnectorDefinition {

    private static final long serialVersionUID = 7953224084604802080L;

    private final ConnectorEvent activationEvent;

    private final Map<String, SExpression> inputs;

    private final List<SOperation> outputs;

    private final String connectorId;

    private final String version;

    private FailAction failAction;

    private String errorCode;

    public SConnectorDefinitionImpl(final ConnectorDefinition connector) {
        super(connector.getName());
        activationEvent = connector.getActivationEvent();
        connectorId = connector.getConnectorId();
        version = connector.getVersion();
        failAction = connector.getFailAction();
        errorCode = connector.getErrorCode();
        inputs = new HashMap<String, SExpression>(connector.getInputs().size());
        for (final Entry<String, Expression> input : connector.getInputs().entrySet()) {
            final Expression value = input.getValue();
            if (value != null) {
                final SExpression sExpression = ServerModelConvertor.convertExpression(value);
                inputs.put(input.getKey(), sExpression);// creates SExpression
            }
        }
        outputs = new ArrayList<SOperation>(connector.getOutputs().size());
        for (final Operation operation : connector.getOutputs()) {
            final SOperation sOperation = ServerModelConvertor.convertOperation(operation);
            outputs.add(sOperation);
        }
        // setId(connector.getId()); TODO : Implement generation of id
    }

    public SConnectorDefinitionImpl(final String name, final String connectorId, final String version, final ConnectorEvent activationEvent) {
        super(name);
        this.connectorId = connectorId;
        this.version = version;
        this.activationEvent = activationEvent;
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
        return activationEvent;
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

    public void setFailAction(final FailAction failAction) {
        this.failAction = failAction;
    }

    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public FailAction getFailAction() {
        return failAction;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
