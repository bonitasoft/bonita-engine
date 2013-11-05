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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;

/**
 * @author Zhang Bole
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class JavaMethodOperationExecutorStrategy extends UpdateOperationExecutorStrategy {

    public static final String TYPE_JAVA_METHOD = "JAVA_METHOD";

    protected static Map<String, Class<?>> primitiveTypes;

    private static Map<String, Class<?>> autoboxableTypes;
    static {
        primitiveTypes = new HashMap<String, Class<?>>(8);
        primitiveTypes.put("char", char.class);
        primitiveTypes.put("byte", byte.class);
        primitiveTypes.put("long", long.class);
        primitiveTypes.put("int", int.class);
        primitiveTypes.put("float", float.class);
        primitiveTypes.put("double", double.class);
        primitiveTypes.put("short", short.class);
        primitiveTypes.put("boolean", boolean.class);

        autoboxableTypes = new HashMap<String, Class<?>>(8);
        autoboxableTypes.put(Character.class.getName(), char.class);
        autoboxableTypes.put(Byte.class.getName(), byte.class);
        autoboxableTypes.put(Long.class.getName(), long.class);
        autoboxableTypes.put(Integer.class.getName(), int.class);
        autoboxableTypes.put(Float.class.getName(), float.class);
        autoboxableTypes.put(Double.class.getName(), double.class);
        autoboxableTypes.put(Short.class.getName(), short.class);
        autoboxableTypes.put(Boolean.class.getName(), boolean.class);
    }

    public JavaMethodOperationExecutorStrategy(final DataInstanceService dataInstanceService) {
        super(dataInstanceService);
    }

    private Method getMethod(final SOperation operation, final Class<?> dataType) throws NoSuchMethodException, ClassNotFoundException {
        final String[] split = operation.getOperator().split(":", 2);
        final String operator = split[0];
        if (split.length > 1) {
            final String className = split[1];
            try {
                return dataType.getDeclaredMethod(operator, getClass(className));
            } catch (final NoSuchMethodException e) {
                if (autoboxableTypes.containsKey(className)) {
                    return dataType.getDeclaredMethod(operator, autoboxableTypes.get(className));
                }
                throw e;
            }
        }
        return dataType.getDeclaredMethod(operator);
    }

    protected Class<?> getClass(final String type) throws ClassNotFoundException {
        if (primitiveTypes.containsKey(type)) {
            return primitiveTypes.get(type);
        }
        return Class.forName(type);
    }

    @Override
    public Object getValue(final SOperation operation, final Object valueToSetObjectWith, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        final String dataToSet = operation.getLeftOperand().getName();
        final Object objectToInvokeJavaMethodOn = expressionContext.getInputValues().get(dataToSet);
        if (objectToInvokeJavaMethodOn == null) {
            throw new SOperationExecutionException("data " + dataToSet + " does not exist in the context " + containerId + " " + containerType);
        }
        Class<?> expressionResultType;
        try {
            expressionResultType = Thread.currentThread().getContextClassLoader().loadClass(operation.getRightOperand().getReturnType());
            final Class<?> dataType = Thread.currentThread().getContextClassLoader().loadClass(objectToInvokeJavaMethodOn.getClass().getName());
            final Method method = getMethod(operation, dataType);
            final Object o = dataType.cast(objectToInvokeJavaMethodOn);
            method.invoke(o, expressionResultType.cast(valueToSetObjectWith));
            return o;
        } catch (final Exception e) {
            throw new SOperationExecutionException("Unable to evaluate operation " + operation, e);
        }
    }

    @Override
    public String getOperationType() {
        return TYPE_JAVA_METHOD;
    }

}
