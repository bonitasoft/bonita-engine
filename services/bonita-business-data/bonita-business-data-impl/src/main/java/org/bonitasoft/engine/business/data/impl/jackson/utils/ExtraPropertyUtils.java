/**
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.business.data.impl.jackson.utils;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.type.CollectionType;

public class ExtraPropertyUtils {

    private static final String STRING_SUFFIX = "_string";

    private static final Set<String> numberTypes;

    static {
        numberTypes = new HashSet<>();
        numberTypes.add(Long.class.getCanonicalName());
        numberTypes.add(Float.class.getCanonicalName());
        numberTypes.add(Double.class.getCanonicalName());
    }

    public static String getExtraPropertyName(BeanPropertyWriter propertyWriter) {
        return propertyWriter.getName().concat(STRING_SUFFIX);
    }

    public static boolean shouldAddExtraProperty(BeanPropertyWriter writer) {
        return shouldAddExtraProperty(writer.getType());
    }

    private static boolean shouldAddExtraProperty(JavaType javaType) {
        if (javaType.getClass().isAssignableFrom(CollectionType.class)){
            CollectionType collectionType = (CollectionType) javaType;
            return shouldAddExtraProperty(collectionType.getContentType());
        }
        return numberTypes.contains(javaType.getRawClass().getCanonicalName());
    }

}
