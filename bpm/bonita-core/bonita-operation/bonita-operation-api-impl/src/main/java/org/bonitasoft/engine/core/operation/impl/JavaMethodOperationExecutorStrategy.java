/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.commons.JavaMethodInvoker;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;

/**
 * @author Zhang Bole
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class JavaMethodOperationExecutorStrategy extends UpdateDataOperationExecutorStrategy {

    public static final String TYPE_JAVA_METHOD = "JAVA_METHOD";

    public JavaMethodOperationExecutorStrategy(final DataInstanceService dataInstanceService) {
        super(dataInstanceService);
    }

    @Override
    public Object getValue(final SOperation operation, final Object valueToSetObjectWith, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        final String dataToSet = operation.getLeftOperand().getName();
        final Object objectToInvokeJavaMethodOn = expressionContext.getInputValues().get(dataToSet);
        if (objectToInvokeJavaMethodOn == null) {
            throw new SOperationExecutionException("data " + dataToSet + " does not exist in the context " + containerId + " " + containerType);
        }
        final String[] split = operation.getOperator().split(":", 2);
        final String operator = split[0];
        String className = null;
        if (split.length > 1) {
            className = split[1];
        }
        try {
            return new JavaMethodInvoker().invokeJavaMethod(operation.getRightOperand().getReturnType(), valueToSetObjectWith, objectToInvokeJavaMethodOn,
                    operator, className);
        } catch (final Exception e) {
            throw new SOperationExecutionException("Unable to evaluate operation " + operation, e);
        }
    }

    @Override
    public String getOperationType() {
        return TYPE_JAVA_METHOD;
    }

}
