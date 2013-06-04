/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Zhang Bole
 * @author Baptiste Mesta
 */
public class JavaMethodOperationExecutorStrategy implements OperationExecutorStrategy {

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

    private final DataInstanceService dataInstanceService;

    private final SDataInstanceBuilders sDataInstanceBuilders;

    public JavaMethodOperationExecutorStrategy(final DataInstanceService dataInstanceService, final SDataInstanceBuilders sDataInstanceBuilders) {
        this.dataInstanceService = dataInstanceService;
        this.sDataInstanceBuilders = sDataInstanceBuilders;
    }

    @Override
    public void execute(final SOperation operation, final Object value, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        final String dataInstanceName = operation.getLeftOperand().getName();
        final SExpression sExpression = operation.getRightOperand();
        updateVariableFromResult(operation, containerId, containerType, dataInstanceName, sExpression, value, expressionContext.getInputValues());
    }

    private void invokeJavaMethod(final Map<String, Object> map, final SOperation operation, final Object expressionResult) throws SOperationExecutionException {
        final String dataName = operation.getLeftOperand().getName();
        final String[] split = operation.getOperator().split(":", 2);
        final String operator = split[0];
        final Serializable objectToUpdate = (Serializable) map.get(dataName);
        if (objectToUpdate == null) {
            throw new SOperationExecutionException("Unknown data with name " + dataName);
        }
        try {
            Method method;
            if (split.length > 1) {
                method = objectToUpdate.getClass().getDeclaredMethod(operator, Class.forName(split[1]));
            } else {
                method = objectToUpdate.getClass().getDeclaredMethod(operator);
            }

            method.invoke(objectToUpdate, expressionResult);
        } catch (final Exception e) {
            throw new SOperationExecutionException("Problem invoking Java method " + operator + " on object " + dataName, e);
        }
    }

    private void updateDataInstance(final SOperation operation, final SDataInstance sDataInstance, final SExpression sExpression, final Object expressionResult)
            throws SDataInstanceException {
        try {
            final Class<?> expressionResultType = Thread.currentThread().getContextClassLoader().loadClass(sExpression.getReturnType());
            final Class<?> dataType = Thread.currentThread().getContextClassLoader().loadClass(sDataInstance.getClassName());
            final Method method = getMethod(operation, dataType);
            final Object o = dataType.cast(sDataInstance.getValue());
            method.invoke(o, expressionResultType.cast(expressionResult));
            final EntityUpdateDescriptor updateDescriptor = getUpdateDescriptor((Serializable) o);
            dataInstanceService.updateDataInstance(sDataInstance, updateDescriptor);
        } catch (final Exception e) {
            throw new SDataInstanceException(e);
        }
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

    private void updateVariableFromResult(final SOperation operation, final long containerId, final String containerType, final String dataInstanceName,
            final SExpression sExpression, final Object expressionResult, final Map<String, Object> map) throws SOperationExecutionException,
            SOperationExecutionException {
        try {

            // Let's update the value if the variable to set is not external:
            if (!operation.getLeftOperand().isExternal()) {
                final SDataInstance sDataInstance = dataInstanceService.getDataInstance(dataInstanceName, containerId, containerType);
                updateDataInstance(operation, sDataInstance, sExpression, expressionResult);
            } else {
                // set the new value of the external data in the list of input values:
                invokeJavaMethod(map, operation, expressionResult);
            }
        } catch (final SDataInstanceException e) {
            throw new SOperationExecutionException(e);
        }
    }

    private EntityUpdateDescriptor getUpdateDescriptor(final Serializable newValue) {
        // update data instance value
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        final SDataInstanceBuilder sDataInstanceBuilder = sDataInstanceBuilders.getDataInstanceBuilder();
        updateDescriptor.addField(sDataInstanceBuilder.getValueKey(), newValue);
        return updateDescriptor;
    }

    @Override
    public String getOperationType() {
        return TYPE_JAVA_METHOD;
    }

}
