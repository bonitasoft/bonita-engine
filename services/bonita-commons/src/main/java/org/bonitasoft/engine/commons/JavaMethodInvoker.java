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
package org.bonitasoft.engine.commons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;

/**
 * Invokes a method on a Java Object.
 *
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class JavaMethodInvoker {

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

    protected Class<?> getClassOrPrimitiveClass(final String type) throws ClassNotFoundException {
        if (primitiveTypes.containsKey(type)) {
            return primitiveTypes.get(type);
        }
        return Thread.currentThread().getContextClassLoader().loadClass(type);
    }

    public Object invokeJavaMethod(final String typeOfValueToSet, final Object valueToSetObjectWith, final Object objectToInvokeJavaMethodOn,
            final String operator, final String operatorParameterClassName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final Class<?> expressionResultType = getClassOrPrimitiveClass(typeOfValueToSet);
        final Class<?> dataType = Thread.currentThread().getContextClassLoader().loadClass(objectToInvokeJavaMethodOn.getClass().getName());
        final Method method = MethodUtils.getMatchingAccessibleMethod(dataType, operator, new Class[] { getClassOrPrimitiveClass(operatorParameterClassName) });
        final Object o = dataType.cast(objectToInvokeJavaMethodOn);
        method.invoke(o, expressionResultType.cast(valueToSetObjectWith));
        return o;
    }

}
