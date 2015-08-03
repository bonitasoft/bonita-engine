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
package org.bonitasoft.engine.operation.impl;

import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperatorType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class OperationImpl implements Operation {

    private static final long serialVersionUID = -7255854432856258651L;
    @XmlElement(type = LeftOperandImpl.class, name = "leftOperand")
    private LeftOperand leftOperand;
    @XmlAttribute(name = "operatorType")
    private OperatorType type;
    @XmlAttribute
    private String operator;
    @XmlElement(type = ExpressionImpl.class, name = "rightOperand")
    private Expression rightOperand;



    public void OperationImpl(){

    }
    public void setLeftOperand(final LeftOperand leftOperand) {
        this.leftOperand = leftOperand;
    }

    /**
     * @deprecated As of 6.0 replaced by {@link #setLeftOperand(LeftOperand)}
     */
    @Deprecated
    public void setVariableToSet(final LeftOperand variableToSet) {
        leftOperand = variableToSet;
    }

    public void setOperator(final String operator) {
        this.operator = operator;
    }

    public void setOperatorInputType(final String operatorInputType) {
        if (null != operatorInputType) {
            if (operator == null) {
                throw new IllegalArgumentException("Please set 'operator' before setting 'operator input type'");
            }
            operator = operator + ":" + operatorInputType;
        }
    }

    public void setType(final OperatorType type) {
        this.type = type;
    }

    public void setRightOperand(final Expression rightOperand) {
        this.rightOperand = rightOperand;
    }

    @Override
    public LeftOperand getLeftOperand() {
        return leftOperand;
    }

    /**
     * @deprecated As of 6.0 replaced by {@link #getLeftOperand()}
     */
    @Deprecated
    @Override
    public LeftOperand getVariableToSet() {
        return leftOperand;
    }

    @Override
    public OperatorType getType() {
        return type;
    }

    @Override
    public String getOperator() {
        return operator;
    }

    @Override
    public Expression getRightOperand() {
        return rightOperand;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (operator == null ? 0 : operator.hashCode());
        result = prime * result + (rightOperand == null ? 0 : rightOperand.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        result = prime * result + (leftOperand == null ? 0 : leftOperand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OperationImpl other = (OperationImpl) obj;
        if (operator == null) {
            if (other.operator != null) {
                return false;
            }
        } else if (!operator.equals(other.operator)) {
            return false;
        }
        if (rightOperand == null) {
            if (other.rightOperand != null) {
                return false;
            }
        } else if (!rightOperand.equals(other.rightOperand)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        if (leftOperand == null) {
            if (other.leftOperand != null) {
                return false;
            }
        } else if (!leftOperand.equals(other.leftOperand)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Operation [ set " + leftOperand + " using " + type + " " + operator + " with" + rightOperand + "]";
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

}
