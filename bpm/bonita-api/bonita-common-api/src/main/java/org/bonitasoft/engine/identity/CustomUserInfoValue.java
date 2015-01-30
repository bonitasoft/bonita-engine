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
 * A {@code CustomUserInfoValue} defines the value of a {@link CustomUserInfoDefinition} for a given {@link User}
 *
 * @author Elias Ricken de Medeiros
 * @see CustomUserInfoDefinition
 * @see User
 * @since 6.3
 */
public interface CustomUserInfoValue extends BonitaObject {

    /**
     * Retrieves the {@link User} identifier
     * @return the {@code User} identifier
     * @since 6.3
     * @see org.bonitasoft.engine.identity.User
     */
    long getUserId();

    /**
     * Retrieves the related {@link CustomUserInfoDefinition} identifier
     * @return the related {@link CustomUserInfoDefinition} identifier
     * @since 6.3
     * @see org.bonitasoft.engine.identity.CustomUserInfoDefinition
     */
    long getDefinitionId();

    /**
     * Retrieves the {@code CustomUserInfoValue} value
     * @return the {@code CustomUserInfoValue} value
     * @since 6.3
     */
    String getValue();

}
