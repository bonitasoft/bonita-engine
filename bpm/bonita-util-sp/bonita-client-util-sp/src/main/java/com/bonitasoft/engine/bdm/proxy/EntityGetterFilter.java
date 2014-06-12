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
 * Filter methods which returns class implementing {@link Entity} or Collection of class implementing {@link Entity}
 *
 * @author Colin Puy
 */
public class EntityGetterFilter implements MethodFilter {

    @Override
    public boolean isHandled(Method method) {
        return returnsListOfEntity(method) || returnsEntity(method);
    }

    private boolean returnsListOfEntity(Method method) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
            return implementsEntity((Class<?>) typeArguments[0]);
        }
        return false;
    }

    private boolean returnsEntity(Method method) {
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
