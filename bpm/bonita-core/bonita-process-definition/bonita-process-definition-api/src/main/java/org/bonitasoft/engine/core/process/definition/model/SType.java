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
package org.bonitasoft.engine.core.process.definition.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.FileInputValue;

/**
 * @author Matthieu Chaffotte
 */
public enum SType {
    TEXT(String.class, Character.class),
    BOOLEAN(Boolean.class),
    DATE(Date.class),
    INTEGER(Integer.class, Long.class, BigInteger.class, Short.class, Byte.class),
    DECIMAL(Float.class, Double.class, BigDecimal.class, Integer.class, Long.class, BigInteger.class, Short.class, Byte.class),
    BYTE_ARRAY(byte[].class),
    FILE(FileInputValue.class);

    private final List<Class<?>> assignableTypes;

    SType(final Class<?>... assignableTypes) {
        this.assignableTypes = Arrays.asList(assignableTypes);
    }

    public boolean validate(final Object object) {
        if (object == null) {
            return true;
        }
        for (final Class<?> clazz : assignableTypes) {
            if (object.getClass().isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

}
