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
package org.bonitasoft.engine.core.operation.model.impl;

import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 */
public class SOperationImpl implements SOperation {

    private static final long serialVersionUID = 1L;

    private SLeftOperand leftOperand;

    private SOperatorType type;

    private String operator;

    private SExpression rightOperand;

    public void setLeftOperand(final SLeftOperand leftOperand) {
        this.leftOperand = leftOperand;
    }

    public void setOperator(final String operator) {
        this.operator = operator;
    }

    public void setType(final SOperatorType type) {
        this.type = type;
    }

    public void setRightOperand(final SExpression rightOperand) {
        this.rightOperand = rightOperand;
    }

    @Override
    public SLeftOperand getLeftOperand() {
        return leftOperand;
    }

    @Override
    public SOperatorType getType() {
        return type;
    }

    @Override
    public String getOperator() {
        return operator;
    }

    @Override
    public SExpression getRightOperand() {
        return rightOperand;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (leftOperand == null ? 0 : leftOperand.hashCode());
        result = prime * result + (operator == null ? 0 : operator.hashCode());
        result = prime * result + (rightOperand == null ? 0 : rightOperand.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
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
        final SOperationImpl other = (SOperationImpl) obj;
        if (leftOperand == null) {
            if (other.leftOperand != null) {
                return false;
            }
        } else if (!leftOperand.equals(other.leftOperand)) {
            return false;
        }
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
        return true;
    }

    @Override
    public String toString() {
        return "SOperationImpl [leftOperand=" + leftOperand + ", type=" + type + ", operator=" + operator + ", rightOperand=" + rightOperand + "]";
    }

}
