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

/**
 * Defines the fields that can be used as filters and soring by
 * {@link org.bonitasoft.engine.api.CustomUserInfoAPI#searchCustomUserInfoValues(org.bonitasoft.engine.search.SearchOptions)}.
 *
 * @author Vincent Elcrin
 * @since 6.3.1
 * @see org.bonitasoft.engine.api.CustomUserInfoAPI#searchCustomUserInfoValues(org.bonitasoft.engine.search.SearchOptions)
 */
public final class CustomUserInfoValueSearchDescriptor {

    /**
     * Refers to {@link org.bonitasoft.engine.identity.CustomUserInfoValue#getUserId()}
     * 
     * @see CustomUserInfoValue#getUserId()
     */
    public static final String USER_ID = "userId";

    /**
     * Refers to {@link org.bonitasoft.engine.identity.CustomUserInfoValue#getDefinitionId()}
     * 
     * @see CustomUserInfoValue#getDefinitionId()
     */
    public static final String DEFINITION_ID = "definitionId";

    /**
     * Refers to {@link org.bonitasoft.engine.identity.CustomUserInfoValue#getValue()}
     * 
     * @see CustomUserInfoValue#getValue()
     */
    public static final String VALUE = "value";

}
