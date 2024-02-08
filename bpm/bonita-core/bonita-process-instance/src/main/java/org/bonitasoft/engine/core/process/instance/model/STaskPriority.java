/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model;

import org.bonitasoft.engine.commons.EnumToObjectConvertible;

/**
 * @author Emmanuel Duchastenier
 */
public enum STaskPriority implements EnumToObjectConvertible {

    LOWEST, UNDER_NORMAL, NORMAL, ABOVE_NORMAL, HIGHEST;

    @Override
    public int fromEnum() {
        return ordinal();
    }

    public static STaskPriority fromOrdinal(int n) {
        for (STaskPriority enumValue : values()) {
            if (enumValue.ordinal() == n) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("Invalid ordinal value for STaskPriority");
    }

}
