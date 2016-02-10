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
package org.bonitasoft.engine.identity;

import java.io.Serializable;

/**
 * represents a helper for updating {@link CustomUserInfoValue}
 *
 * @author Vincent Elcrin
 * @see CustomUserInfoValue
 * @since 6.3.1
 */
public class CustomUserInfoValueUpdater implements Serializable {

    private static final long serialVersionUID = -2699448153857398426L;

    private final String value;

    /**
     * creates a new instance of {@link CustomUserInfoValueUpdater} with a value to update on a {@link CustomUserInfoValue}
     * 
     * @param value the value to update on a {@link CustomUserInfoValue}
     */
    public CustomUserInfoValueUpdater(final String value) {
        this.value = value;
    }

    /**
     * @return the value to update on a {@link CustomUserInfoValue}
     */
    public String getValue() {
        return value;
    }
}
