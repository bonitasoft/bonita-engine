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
package org.bonitasoft.engine.bdm.dao.client.resources.utils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.bonitasoft.engine.bdm.model.field.Field;

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
