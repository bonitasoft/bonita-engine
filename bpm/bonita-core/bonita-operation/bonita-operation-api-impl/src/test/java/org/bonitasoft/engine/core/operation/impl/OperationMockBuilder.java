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

package org.bonitasoft.engine.core.operation.impl;

import java.util.List;

import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.operation.model.impl.SOperationImpl;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class OperationMockBuilder {

    public static SExpressionImpl buildExpression(final String content, final ExpressionType type, final List<SExpression> dependencies) {
        return new SExpressionImpl(content, content, type.name(), "", null, dependencies);
    }

    public static SOperationImpl buildMockOperation(final String leftOperandType, final SExpressionImpl rightOperand) {
        SOperationImpl operation = buildMockOperation(leftOperandType);
        operation.setRightOperand(rightOperand);
        return operation;
    }

    public static SOperationImpl buildMockOperation(final String leftOperandType) {
        SOperationImpl operation = new SOperationImpl();
        SLeftOperandImpl leftOperand = buildMockLeftOperand(leftOperandType);
        operation.setLeftOperand(leftOperand);
        return operation;
    }

    public static SLeftOperandImpl buildMockLeftOperand(final String leftOperandType) {
        SLeftOperandImpl leftOperand = new SLeftOperandImpl();
        leftOperand.setType(leftOperandType);
        return leftOperand;
    }

    public static SOperationImpl buildMockOperation(final String leftOperandType, String leftOperandName) {
        SLeftOperandImpl leftOperand = buildMockLeftOperand(leftOperandType, leftOperandName);
        return buildMockOperation(leftOperand);
    }

    public static SOperationImpl buildMockOperation(final SLeftOperandImpl leftOperand) {
        SOperationImpl operation = new SOperationImpl();
        operation.setLeftOperand(leftOperand);
        return operation;
    }

    public static SLeftOperandImpl buildMockLeftOperand(final String leftOperandType, final String leftOperandName) {
        SLeftOperandImpl leftOperand = buildMockLeftOperand(leftOperandType);
        leftOperand.setName(leftOperandName);
        return leftOperand;
    }

}
