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
 * holds constants about {@link User} search filters.
 *
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @see User
 * @since 6.0.0
 */
public final class UserSearchDescriptor {

    /** filter search on User's username */
    public static final String USER_NAME = "userName";

    /** filter search on User's firstname */
    public static final String FIRST_NAME = "firstName";

    /** filter search on User's lastname */
    public static final String LAST_NAME = "lastName";

    /** filter search on User's group id */
    public static final String GROUP_ID = "groupId";

    /** filter search on User's role id */
    public static final String ROLE_ID = "roleId";

    /** filter search on the User's manager user id */
    public static final String MANAGER_USER_ID = "managerUserId";

    /** filter search on User's activation */
    public static final String ENABLED = "enabled";

    /** filter search on User's last connection date */
    public static final String LAST_CONNECTION = "lastConnection";

}
