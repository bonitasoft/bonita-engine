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

import org.bonitasoft.engine.commons.JavaMethodInvoker;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;

/**
 * @author Zhang Bole
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class JavaMethodOperationExecutorStrategy implements OperationExecutorStrategy {

    public static final String TYPE_JAVA_METHOD = "JAVA_METHOD";

    public JavaMethodOperationExecutorStrategy() {
    }

    @Override
    public Object computeNewValueForLeftOperand(final SOperation operation, final Object valueToSetObjectWith, final SExpressionContext expressionContext,
            final boolean shouldPersistValue)
            throws SOperationExecutionException {
        final Object objectToInvokeJavaMethodOn;
        objectToInvokeJavaMethodOn = extractObjectToInvokeFromContext(operation, expressionContext);
        final String methodName = extractMethodName(operation);
        final String operatorType = extractParameterType(operation);
        try {
            return new JavaMethodInvoker().invokeJavaMethod(operation.getRightOperand().getReturnType(), valueToSetObjectWith, objectToInvokeJavaMethodOn,
                    methodName, operatorType);
        } catch (final Exception e) {
            throw new SOperationExecutionException("Unable to evaluate operation " + operation, e);
        }
    }

    protected String extractParameterType(final SOperation operation) {
        final String[] split = operation.getOperator().split(":", 2);
        if (split.length > 1) {
            return split[1];
        }
        return null;
    }

    protected String extractMethodName(final SOperation operation) {
        final String[] split = operation.getOperator().split(":", 2);
        return split[0];
    }

    protected Object extractObjectToInvokeFromContext(final SOperation operation, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        final Object objectToInvokeJavaMethodOn;
        final String dataToSet = operation.getLeftOperand().getName();
        objectToInvokeJavaMethodOn = expressionContext.getInputValues().get(dataToSet);
        if (objectToInvokeJavaMethodOn == null) {
            throw new SOperationExecutionException("data " + dataToSet + " does not exist");
        }
        return objectToInvokeJavaMethodOn;
    }

    @Override
    public String getOperationType() {
        return TYPE_JAVA_METHOD;
    }

}
