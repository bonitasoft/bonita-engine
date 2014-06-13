/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javassist.util.proxy.MethodFilter;

import com.bonitasoft.engine.bdm.Entity;

/**
 * Filter getter and setter methods which get or set class implementing {@link Entity} or Collection of class implementing {@link Entity}
 *
 * @author Colin Puy
 */
public class EntityGetterAndSetterFilter implements MethodFilter {

    @Override
    public boolean isHandled(Method method) {
        if (method.getName().startsWith("get")) {
            return isGetterThatReturnsListOfEntity(method) || isGetterThatReturnsAnEntity(method);
        }
        if (method.getName().startsWith("set")) {
            return isSetterForListOfEntities(method) || isSetterForAnEntity(method);
        }
        return false;
    }

    private boolean isSetterForAnEntity(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 0) {
            return implementsEntity(parameterTypes[0]);
        }
        return false;
    }

    private boolean isSetterForListOfEntities(Method method) {
        Type[] parametersType = method.getGenericParameterTypes();
        if (parametersType.length > 0) {
            return isCollectionOfEntities(parametersType[0]);
        }
        return false;
    }

    private boolean isGetterThatReturnsListOfEntity(Method method) {
        Type returnType = method.getGenericReturnType();
        return isCollectionOfEntities(returnType);
    }

    private boolean isCollectionOfEntities(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            return implementsEntity((Class<?>) typeArguments[0]);
        }
        return false;
    }

    private boolean isGetterThatReturnsAnEntity(Method method) {
        return implementsEntity(method.getReturnType());
    }

    private boolean implementsEntity(Class<?> implementation) {
        for (Class<?> inte : implementation.getInterfaces()) {
            if (inte.equals(Entity.class)) {
                return true;
            }
        }
        return false;
    }

}
