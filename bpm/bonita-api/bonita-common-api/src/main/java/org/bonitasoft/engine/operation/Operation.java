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
package org.bonitasoft.engine.operation;

import java.io.Serializable;

import org.bonitasoft.engine.expression.Expression;

/**
 * An <code>Operation</code> has a <code>LeftOperand</code> (typically, for assignment operations, left operand is the part of the operation receiving the
 * value), an <code>OperatorType</code> which defines the type of operation to execute
 * and a right operand, represented by an <code>Expression</code>.
 * 
 * @see LeftOperand
 * @see Expression
 * @see OperatorType
 * @author Zhang Bole
 * @author Emmanuel Duchastenier
 */
public interface Operation extends Serializable {

    /**
     * @return the <code>LeftOperand</code> of this <code>Operation</code>, representing what entity will be set after execution.
     */
    LeftOperand getLeftOperand();

    /**
     * @deprecated As of 6.0 replaced by {@link #getLeftOperand()}
     */
    @Deprecated
    LeftOperand getVariableToSet();

    /**
     * @return the operator type of this <code>Operation</code>.
     */
    OperatorType getType();

    /**
     * @return the operator of this <code>Operation</code>,as a String.
     */
    String getOperator();

    /**
     * @return the right operand <code>Expression</code> to be evaluated before executing the operation
     */
    Expression getRightOperand();

}
