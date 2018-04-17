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
 * Describes a custom user information definition that will be available for all {@link User}s in the organization.
 *
 * @author Vincent Elcrin
 * @see User
 * @since 6.3
 */
public interface CustomUserInfoDefinition extends BonitaObject {

    /**
     * @return the {@code CustomUserInfoDefinition} identifier
     * @since 6.3
     */
    long getId();

    /**
     * @return the {@code CustomUserInfoDefinition} name
     * @since 6.3
     */
    String getName();

    /**
     * @return the {@code CustomUserInfoDefinition} description
     * @since 6.3
     */
    String getDescription();
}
