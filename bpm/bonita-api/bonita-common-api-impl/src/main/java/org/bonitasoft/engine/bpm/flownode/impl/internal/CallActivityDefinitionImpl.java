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

import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CallableElementType;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.impl.OperationImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @XmlElementWrapper
    @XmlElement(type = OperationImpl.class, name = "dataInputOperations")
    private final List<Operation> dataInputOperations;
    @XmlJavaTypeAdapter(MapAdapterExpression.class)
    @XmlElement(name="contractInput")
    private final Map<String, Expression> contractInputs;
    @XmlElementWrapper
    @XmlElement(type = OperationImpl.class, name = "dataOutputOperations")
    private final List<Operation> dataOutputOperations;
    @XmlElement
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (callableElement == null ? 0 : callableElement.hashCode());
        result = prime * result + (callableElementType == null ? 0 : callableElementType.hashCode());
        result = prime * result + (callableElementVersion == null ? 0 : callableElementVersion.hashCode());
        result = prime * result + (dataInputOperations == null ? 0 : dataInputOperations.hashCode());
        result = prime * result + (dataOutputOperations == null ? 0 : dataOutputOperations.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CallActivityDefinitionImpl other = (CallActivityDefinitionImpl) obj;
        if (callableElement == null) {
            if (other.callableElement != null) {
                return false;
            }
        } else if (!callableElement.equals(other.callableElement)) {
            return false;
        }
        if (callableElementType != other.callableElementType) {
            return false;
        }
        if (callableElementVersion == null) {
            if (other.callableElementVersion != null) {
                return false;
            }
        } else if (!callableElementVersion.equals(other.callableElementVersion)) {
            return false;
        }
        if (dataInputOperations == null) {
            if (other.dataInputOperations != null) {
                return false;
            }
        } else if (!dataInputOperations.equals(other.dataInputOperations)) {
            return false;
        }
        if (dataOutputOperations == null) {
            if (other.dataOutputOperations != null) {
                return false;
            }
        } else if (!dataOutputOperations.equals(other.dataOutputOperations)) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
