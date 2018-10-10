/**
 * Copyright (C) 2015-2018 BonitaSoft S.A.
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

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.List;
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
     * Describes types that can be be converted later to other types.<br>
     *     <li>key: the type which can be converted to
     *     <li>values: the input types that can be converted to the <code>key</code> type
     */
    private static final Map<String, List<String>> CONVERTIBLE_TYPES = new HashMap<>();
    static {
        CONVERTIBLE_TYPES.put("org.bonitasoft.engine.bpm.document.Document",
                asList("org.bonitasoft.engine.bpm.contract.FileInputValue"));
        CONVERTIBLE_TYPES.put("org.bonitasoft.engine.bpm.document.DocumentValue",
                asList("org.bonitasoft.engine.bpm.contract.FileInputValue"));
    }

    /**
     * Check if the declared return type is compatible with the real Expression evaluation return type. If the result of
     * the Expression evaluation is null, then this method returns <code>true</code>.
     *
     * @param expression
     *        the evaluated expression
     * @param result
     *        the expression result to check
     * @throws SExpressionEvaluationException
     *         if the condition is not fulfilled, does nothing otherwise
     */
    public void checkReturnType(final SExpression expression, final Object result, final Map<String, Object> context) throws SExpressionEvaluationException {
        if (result == null) {
            return;
        }
        final String declaredReturnType = expression.getReturnType();
        final String evaluatedClassName = result.getClass().getName();
        if (!evaluatedClassName.equals(declaredReturnType)) {
            if (isConvertible(declaredReturnType, evaluatedClassName)) {
                return;
            }

            try {
                final String expressionName = expression.getName();
                try {
                    final Class<?> declaredReturnedClass = getClazz(declaredReturnType);
                    final Class<?> evaluatedClass = result.getClass();
                    if (!declaredReturnedClass.isAssignableFrom(evaluatedClass)) {
                        throw new SExpressionEvaluationException(format(
                                "Declared return type %s is not compatible with evaluated type %s for expression %s",
                                declaredReturnedClass, evaluatedClass, expressionName), expressionName);
                    }
                } catch (final ClassNotFoundException e) {
                    throw new SExpressionEvaluationException(
                            format("Unknown declared return type %s for expression %s", declaredReturnType,
                                    expressionName),
                            e, expressionName);
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

    private static Class<?> getClazz(String type) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(type);
    }

    private static Boolean isConvertible(String targetType, String sourceType) {
        return ofNullable(CONVERTIBLE_TYPES.get(targetType)).map(targets -> targets.contains(sourceType)).orElse(false);
    }

    private static boolean isContextOnProcess(final Map<String, Object> context) {
        return context.containsKey(CONTAINER_TYPE) && PROCESS_INSTANCE_SCOPE.equals(context.get(CONTAINER_TYPE));
    }

    private static boolean isContextOnActivity(final Map<String, Object> context) {
        return context.containsKey(CONTAINER_TYPE) && ACTIVITY_INSTANCE_SCOPE.equals(context.get(CONTAINER_TYPE));
    }

}
