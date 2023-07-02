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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CallableElementType;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.impl.OperationImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class CallActivityDefinitionImpl extends ActivityDefinitionImpl implements CallActivityDefinition {

    private static final long serialVersionUID = -6798914951807258918L;

    @Getter
    @XmlElement(type = ExpressionImpl.class)
    private Expression callableElement;
    @Getter
    @XmlElement(type = ExpressionImpl.class)
    private Expression callableElementVersion;
    @XmlElement(type = OperationImpl.class, name = "dataInputOperation")
    private final List<Operation> dataInputOperations = new ArrayList<>(3);

    @XmlJavaTypeAdapter(NameExpressionMapAdapter.class)
    @XmlElement(name = "contractInput")
    private final Map<String, Expression> contractInputs = new HashMap<>();

    @XmlElement(type = OperationImpl.class, name = "dataOutputOperation")
    private final List<Operation> dataOutputOperations = new ArrayList<>(3);
    @Getter
    @Setter
    @XmlAttribute
    private CallableElementType callableElementType;

    public CallActivityDefinitionImpl(final String name) {
        super(name);
    }

    public CallActivityDefinitionImpl(final long id, final String name) {
        super(id, name);
    }

    public void setCallableElement(final Expression callableElement) {
        this.callableElement = ExpressionBuilder.getNonNullCopy(callableElement);
    }

    public void setCallableElementVersion(final Expression callableElementVersion) {
        this.callableElementVersion = ExpressionBuilder.getNonNullCopy(callableElementVersion);
    }

    @Override
    public List<Operation> getDataInputOperations() {
        return Collections.unmodifiableList(dataInputOperations);
    }

    public void addDataInputOperation(final Operation dataInputOperation) {
        dataInputOperations.add(OperationBuilder.getNonNullCopy(dataInputOperation));
    }

    @Override
    public List<Operation> getDataOutputOperations() {
        return Collections.unmodifiableList(dataOutputOperations);
    }

    public void addDataOutputOperation(final Operation dataOutputOperation) {
        dataOutputOperations.add(OperationBuilder.getNonNullCopy(dataOutputOperation));
    }

    public void addProcessStartContractInput(String inputName, Expression value) {
        contractInputs.put(inputName, ExpressionBuilder.getNonNullCopy(value));
    }

    @Override
    public Map<String, Expression> getProcessStartContractInputs() {
        return contractInputs;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
