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
package org.bonitasoft.engine.business.data.impl.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Laurent Leseigneur
 */
public class JsonNumberSerializerHelper {

    private final Set<String> numberTypes;

    public JsonNumberSerializerHelper() {
        numberTypes = new HashSet<>();
        numberTypes.add(Long.class.getCanonicalName());
        numberTypes.add(Float.class.getCanonicalName());
        numberTypes.add(Double.class.getCanonicalName());
    }

    public boolean shouldAddStringRepresentationForNumber(Field field) {
        return numberTypes.contains(field.getType().getCanonicalName());
    }

    public boolean shouldAddStringRepresentationForNumberList(Field field) {
        if (field.getType().equals(List.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type type = parameterizedType.getActualTypeArguments()[0];
            return numberTypes.contains(((Class) type).getCanonicalName());
        }
        return false;
    }

    public List<String> convertToStringList(List numberList) {
        ArrayList<String> strings = new ArrayList<>();
        if (numberList != null) {
            for (Object item : numberList) {
                strings.add(item.toString());
            }
        }
        return strings;
    }

    public String convertToString(Object invoke) {
        if (invoke == null) {
            return null;
        }
        return invoke.toString();
    }


}
