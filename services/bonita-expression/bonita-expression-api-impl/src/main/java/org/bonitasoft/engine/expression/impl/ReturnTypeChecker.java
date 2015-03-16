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
package org.bonitasoft.engine.expression.impl;

import java.util.Map;

import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Emmanuel Duchastenier
 */
public class ReturnTypeChecker {

    private static final String CONTAINER_ID = "containerId";

    private static final String CONTAINER_TYPE = "containerType";

    private static final String ACTIVITY_INSTANCE_SCOPE = "ACTIVITY_INSTANCE";

    private static final String PROCESS_INSTANCE_SCOPE = "PROCESS_INSTANCE";

    /**
     * Check if the declared return type is compatible with the real Expression evaluation return type. If the result of the Expression evaluation is null, then
     * this method returns true.
     *
     * @param expression
     *        the evaluated expression
     * @param result
     *        the expression result to check
     * @throws SExpressionEvaluationException
     *         if the condition is not fulfilled, does nothing otherwise
     */
    public void checkReturnType(final SExpression expression, final Object result, final Map<String, Object> context) throws SExpressionEvaluationException {
        if (result != null && !result.getClass().getName().equals(expression.getReturnType())) {
            try {
                try {
                    final Class<?> declaredReturnedType = Thread.currentThread().getContextClassLoader().loadClass(expression.getReturnType());
                    final Class<?> evaluatedReturnedType = result.getClass();
                    if (!declaredReturnedType.isAssignableFrom(evaluatedReturnedType)) {
                        throw new SExpressionEvaluationException("Declared return type " + declaredReturnedType + " is not compatible with evaluated type "
                                + evaluatedReturnedType + " for expression " + expression.getName(), expression.getName());
                    }
                } catch (final ClassNotFoundException e) {
                    throw new SExpressionEvaluationException("Declared return type unknown : " + expression.getReturnType() + " for expression "
                            + expression.getName(), e, expression.getName());
                }
            } catch (final SExpressionEvaluationException e) {
                if (isContextOnActivity(context)) {
                    e.setFlowNodeInstanceIdOnContext((Long) context.get(CONTAINER_ID));
                }
                if (isContextOnProcess(context)) {
                    e.setProcessInstanceIdOnContext((Long) context.get(CONTAINER_ID));
                }
                throw e;
            }
        }
    }

    // process instance
    // activity name

    protected boolean isContextOnProcess(final Map<String, Object> context) {
        return context.containsKey(CONTAINER_TYPE) && PROCESS_INSTANCE_SCOPE.equals(context.get(CONTAINER_TYPE));
    }

    protected boolean isContextOnActivity(final Map<String, Object> context) {
        return context.containsKey(CONTAINER_TYPE) && ACTIVITY_INSTANCE_SCOPE.equals(context.get(CONTAINER_TYPE));
    }
}
