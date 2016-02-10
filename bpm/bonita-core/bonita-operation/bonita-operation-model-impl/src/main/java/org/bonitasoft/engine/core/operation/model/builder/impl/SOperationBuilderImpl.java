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
package org.bonitasoft.engine.core.operation.model.builder.impl;

import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilder;
import org.bonitasoft.engine.core.operation.model.impl.SOperationImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhang Bole
 */
public class SOperationBuilderImpl implements SOperationBuilder {

    private final SOperationImpl operation;
    
    public SOperationBuilderImpl(final SOperationImpl operation) {
        super();
        this.operation = operation;
    }

    @Override
    public SOperationBuilder setLeftOperand(final SLeftOperand leftOperand) {
        operation.setLeftOperand(leftOperand);
        return this;
    }

    @Override
    public SOperationBuilder setType(final SOperatorType operatorType) {
        operation.setType(operatorType);
        return this;
    }

    @Override
    public SOperationBuilder setOperator(final String operator) {
        operation.setOperator(operator);
        return this;
    }

    @Override
    public SOperationBuilder setRightOperand(final SExpression rightOperand) {
        operation.setRightOperand(rightOperand);
        return this;
    }

    @Override
    public SOperation done() {
        return operation;
    }

}
