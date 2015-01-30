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
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @see UserMembership
 * @since 6.0.0
 */
public enum UserMembershipCriterion {
    /**
     * roleName ascending order
     */
    ROLE_NAME_ASC,
    /**
     * groupName ascending order
     */
    GROUP_NAME_ASC,
    /**
     * roleName descending order
     */
    ROLE_NAME_DESC,
    /**
     * groupName descending order
     */
    GROUP_NAME_DESC,
    /**
     * assigned date ascending order
     */
    ASSIGNED_DATE_ASC,
    /**
     * assigned date descending order
     */
    ASSIGNED_DATE_DESC
}
