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
 */
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CallableElementType;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.impl.OperationImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CallActivityDefinitionImpl extends ActivityDefinitionImpl implements CallActivityDefinition {

    private static final long serialVersionUID = -6798914951807258918L;
    @XmlElement(type = ExpressionImpl.class)
    private Expression callableElement;
    @XmlElement(type = ExpressionImpl.class)
    private Expression callableElementVersion;
    @XmlElement(type = OperationImpl.class, name = "dataInputOperation")
    private final List<Operation> dataInputOperations;
    @XmlJavaTypeAdapter(MapAdapterExpression.class)
    @XmlElement(name="contractInput")
    private final Map<String, Expression> contractInputs;
    @XmlElement(type = OperationImpl.class, name = "dataOutputOperation")
    private final List<Operation> dataOutputOperations;
    @XmlAttribute
    private CallableElementType callableElementType;

    public CallActivityDefinitionImpl(final String name) {
        super(name);
        dataInputOperations = new ArrayList<>(3);
        dataOutputOperations = new ArrayList<>(3);
        contractInputs = new HashMap<>();
    }

    public CallActivityDefinitionImpl(final long id, final String name) {
        super(id, name);
        dataInputOperations = new ArrayList<>(3);
        dataOutputOperations = new ArrayList<>(3);
        contractInputs = new HashMap<>();
    }

    public CallActivityDefinitionImpl() {
        super();
        dataInputOperations = new ArrayList<>(3);
        dataOutputOperations = new ArrayList<>(3);
        contractInputs = new HashMap<>();

    }
    @Override
    public Expression getCallableElement() {
        return callableElement;
    }

    public void setCallableElement(final Expression callableElement) {
        this.callableElement = callableElement;
    }

    @Override
    public Expression getCallableElementVersion() {
        return callableElementVersion;
    }

    public void setCallableElementVersion(final Expression callableElementVersion) {
        this.callableElementVersion = callableElementVersion;
    }

    @Override
    public List<Operation> getDataInputOperations() {
        return Collections.unmodifiableList(dataInputOperations);
    }

    public void addDataInputOperation(final Operation dataInputOperation) {
        dataInputOperations.add(dataInputOperation);
    }

    @Override
    public List<Operation> getDataOutputOperations() {
        return Collections.unmodifiableList(dataOutputOperations);
    }

    public void addDataOutputOperation(final Operation dataOutputOperation) {
        dataOutputOperations.add(dataOutputOperation);
    }

    @Override
    public CallableElementType getCallableElementType() {
        return callableElementType;
    }

    public void setCallableElementType(final CallableElementType callableElementType) {
        this.callableElementType = callableElementType;
    }

    public void addProcessStartContractInput(String inputName, Expression value) {
        contractInputs.put(inputName, value);
    }

    @Override
    public Map<String, Expression> getProcessStartContractInputs() {
        return contractInputs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("callableElement", callableElement)
                .append("callableElementVersion", callableElementVersion)
                .append("dataInputOperations", dataInputOperations)
                .append("contractInputs", contractInputs)
                .append("dataOutputOperations", dataOutputOperations)
                .append("callableElementType", callableElementType)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CallActivityDefinitionImpl that = (CallActivityDefinitionImpl) o;
        return Objects.equals(callableElement, that.callableElement) &&
                Objects.equals(callableElementVersion, that.callableElementVersion) &&
                Objects.equals(dataInputOperations, that.dataInputOperations) &&
                Objects.equals(contractInputs, that.contractInputs) &&
                Objects.equals(dataOutputOperations, that.dataOutputOperations) &&
                Objects.equals(callableElementType, that.callableElementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), callableElement, callableElementVersion, dataInputOperations, contractInputs, dataOutputOperations, callableElementType);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
