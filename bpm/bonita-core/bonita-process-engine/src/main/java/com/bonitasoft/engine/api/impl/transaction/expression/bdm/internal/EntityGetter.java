/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.expression.bdm.internal;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import com.bonitasoft.engine.bdm.model.field.Field;

/**
 * Wrapper over entity getter method
 * 
 * @author Colin Puy
 */
public class EntityGetter {

    private final Method method;

    public EntityGetter(Method method) {
        checkIsGetter(method);
        this.method = method;
    }

    private void checkIsGetter(Method method) {
        String methodName = method.getName();
        if (!methodName.startsWith("get") || methodName.length() <= 3) {
            throw new IllegalArgumentException(methodName + " is not a valid getter name.");
        }
    }

    public String getSourceEntityName() {
        return method.getDeclaringClass().getSimpleName();
    }

    public String getCapitalizedFieldName() {
        return method.getName().substring(3);
    }

    public String getReturnTypeClassName() {
        if (returnsList()) {
            return List.class.getName();
        }
        return method.getReturnType().getName();
    }

    public String getAssociatedNamedQuery() {
        String targetEntityName = getTargetEntityClass().getSimpleName();
        return targetEntityName + ".find" + getCapitalizedFieldName() + "By" + getSourceEntityName() + Capitalizer.capitalize(Field.PERSISTENCE_ID);
    }

    public boolean returnsList() {
        Class<?> returnTypeClass = method.getReturnType();
        return List.class.isAssignableFrom(returnTypeClass);
    }

    public Class<?> getTargetEntityClass() {
        if (returnsList()) {
            final ParameterizedType listType = (ParameterizedType) method.getGenericReturnType();
            final Class<?> type = (Class<?>) listType.getActualTypeArguments()[0];
            return type;
        }
        return method.getReturnType();
    }
}
