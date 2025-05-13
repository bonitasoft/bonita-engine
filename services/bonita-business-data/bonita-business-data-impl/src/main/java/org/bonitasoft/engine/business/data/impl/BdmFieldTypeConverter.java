/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.impl;

/**
 * Utility class to convert a value to a specific type if compatible.
 * Notably, number types are converted to the requested type.
 * This class is used by the BDM insert / update methods called by the REST API, so types are converted from JSON,
 * which need specific treatment.
 *
 * @author Emmanuel Duchastenier
 */
public class BdmFieldTypeConverter {

    @SuppressWarnings("unchecked")
    public static <T> T convert(final Object value, final Class<T> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }
        if (Number.class.isAssignableFrom(targetType) && value instanceof Number numberValue) {
            double doubleValue = numberValue.doubleValue();
            if (targetType == Integer.class) {
                return (T) Integer.valueOf((int) doubleValue);
            } else if (targetType == Long.class) {
                return (T) Long.valueOf((long) doubleValue);
            } else if (targetType == Float.class) {
                return (T) Float.valueOf((float) doubleValue);
            } else if (targetType == Double.class) {
                if (numberValue instanceof Float) {
                    // to avoid having extra unwanted precision:
                    return (T) Double.valueOf(numberValue.toString());
                }
                return (T) Double.valueOf(doubleValue);
            } else if (targetType == Short.class) {
                return (T) Short.valueOf((short) doubleValue);
            } else if (targetType == Byte.class) {
                return (T) Byte.valueOf((byte) doubleValue);
            }
        }
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to " + targetType);
    }
}
