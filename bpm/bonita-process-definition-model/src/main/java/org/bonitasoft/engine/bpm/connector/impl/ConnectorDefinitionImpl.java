/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bpm.connector.impl;

import static org.bonitasoft.engine.operation.OperationBuilder.getNonNullCopy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.FailAction;
import org.bonitasoft.engine.bpm.flownode.impl.internal.NameExpressionMapAdapter;
import org.bonitasoft.engine.bpm.internal.NamedDefinitionElementImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.impl.OperationImpl;

/**
 * @author Baptiste Mesta
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class ConnectorDefinitionImpl extends NamedDefinitionElementImpl implements ConnectorDefinition {

    private static final long serialVersionUID = 1892648036453422626L;
    @XmlAttribute
    private final String connectorId;
    @XmlJavaTypeAdapter(NameExpressionMapAdapter.class)
    private final Map<String, Expression> inputs = new HashMap<>();

    @XmlElementWrapper(name = "outputs")
    @XmlElement(name = "operation", type = OperationImpl.class)
    private final List<Operation> outputs = new ArrayList<>();

    @XmlAttribute
    private final ConnectorEvent activationEvent;
    @XmlAttribute
    private final String version;

    @Setter
    @XmlAttribute
    private FailAction failAction = FailAction.FAIL;
    @Setter
    @XmlAttribute
    private String errorCode;

    public ConnectorDefinitionImpl(final String name, final String connectorId, final String version,
            final ConnectorEvent activationEvent) {
        super(name);
        this.connectorId = connectorId;
        this.version = version;
        this.activationEvent = activationEvent;
    }

    public ConnectorDefinitionImpl() {
        this.connectorId = "default id";
        this.version = "default version";
        this.activationEvent = ConnectorEvent.ON_ENTER;
    }

    public void addInput(final String name, final Expression expression) {
        inputs.put(name, ExpressionBuilder.getNonNullCopy(expression));
    }

    public void addOutput(final Operation operation) {
        outputs.add(getNonNullCopy(operation));
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }
}
