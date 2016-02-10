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
 * lists the available {@link User} sort orders
 *
 * @author Matthieu Chaffotte
 * @see User
 * @since 6.0.0
 */
public enum UserCriterion {
    /**
     * First name ascending order
     */
    FIRST_NAME_ASC,
    /**
     * Last name ascending order
     */
    LAST_NAME_ASC,
    /**
     * User name ascending order
     */
    USER_NAME_ASC,
    /**
     * First name descending order
     */
    FIRST_NAME_DESC,
    /**
     * Last name descending order
     */
    LAST_NAME_DESC,
    /**
     * user name descending order
     */
    USER_NAME_DESC;

}
