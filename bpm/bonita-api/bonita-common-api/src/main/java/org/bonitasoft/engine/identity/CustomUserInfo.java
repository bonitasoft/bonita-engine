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

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * Aggregate information of {@link CustomUserInfoDefinition} and {@link CustomUserInfoValue}
 *
 * @author Vincent Elcrin
 * @see CustomUserInfoDefinition
 * @see CustomUserInfoValue
 * @since 6.3
 */
public class CustomUserInfo implements BonitaObject {

    private static final long serialVersionUID = 4376121609647215025L;

    private final CustomUserInfoDefinition definition;

    private String value;

    private final long userId;

    /**
     * Creates an instance of {@code CustomUserInfo} based given information
     *
     * @param userId the {@link User} identifier
     * @param definition the {@link CustomUserInfoDefinition}
     * @param value the {@link CustomUserInfoValue}
     * @see org.bonitasoft.engine.identity.User
     * @see org.bonitasoft.engine.identity.CustomUserInfoDefinition
     * @see org.bonitasoft.engine.identity.CustomUserInfoValue
     */
    public CustomUserInfo(final long userId, final CustomUserInfoDefinition definition, final CustomUserInfoValue value) {
        this.userId = userId;
        this.definition = definition;
        if (value != null) {
            this.value = value.getValue();
        }
    }

    /**
     * Retrieves the {@link org.bonitasoft.engine.identity.CustomUserInfoDefinition}
     * 
     * @return the {@code CustomUserInfoDefinition}
     * @since 6.3
     * @see org.bonitasoft.engine.identity.CustomUserInfoDefinition
     */
    public CustomUserInfoDefinition getDefinition() {
        return definition;
    }

    /**
     * Retrieves the {@link org.bonitasoft.engine.identity.User} identifier
     * 
     * @return the {@code User} identifier
     * @since 6.3
     * @see org.bonitasoft.engine.identity.User
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Retrieves a {@link String} representing the {@code Custom User Information} value. This is the value of
     * {@link org.bonitasoft.engine.identity.CustomUserInfoValue#getValue()} of
     * the {@code CustomUserInfoValue} related to this {@code CustomUserInfo}. If there is not related {@code CustomUserInfoValue}, this method will return
     * null.
     * 
     * @return a {@code String} representing the {@code Custom User Information} value.
     * @since 6.3
     */
    public String getValue() {
        return value;
    }
}
