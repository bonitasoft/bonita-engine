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


import org.bonitasoft.engine.business.data.BusinessDataService;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.impl.JavaMethodOperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;

/**
 * @author Laurent Leseigneur
 * @author Matthieu Chaffotte
 */
public class BusinessDataJavaMethodOperationExecutorStrategy extends JavaMethodOperationExecutorStrategy {

    private final BusinessDataService businessDataService;

    public BusinessDataJavaMethodOperationExecutorStrategy(final BusinessDataService businessDataService) {
        this.businessDataService = businessDataService;
    }

    @Override
    public Object computeNewValueForLeftOperand(final SOperation operation, final Object valueToSetObjectWith, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        if (isBusinessData(operation)) {
            return delegateBusinessValueForLeftOperand(operation, valueToSetObjectWith, expressionContext);
        }
        return computeJavaOperation(operation, valueToSetObjectWith, expressionContext);
    }

    protected Object computeJavaOperation(final SOperation operation, final Object valueToSetObjectWith, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        return super.computeNewValueForLeftOperand(operation, valueToSetObjectWith, expressionContext);
    }

    private Object delegateBusinessValueForLeftOperand(final SOperation operation, final Object valueToSetObjectWith, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        final Object businessObject = extractObjectToInvokeFromContext(operation, expressionContext);
        final String methodName = extractMethodName(operation);
        final String parameterType = extractParameterType(operation);
        try {
            return businessDataService.callJavaOperation(businessObject, valueToSetObjectWith, methodName, parameterType);
        } catch (final SBusinessDataNotFoundException e) {
            throw new SOperationExecutionException(e);
        } catch (final SBusinessDataRepositoryException e) {
            throw new SOperationExecutionException(e);
        }
    }

    private boolean isBusinessData(final SOperation operation) {
        return SLeftOperand.TYPE_BUSINESS_DATA.equals(operation.getLeftOperand().getType());
    }
}
