/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import java.util.Date;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * represents a relation between {@link User} and {@link Role} and {@link Group} inside the organization.
 * <p>
 * A {@link User} may be related to whether a {@link Group} whether a {@link Role} or both. Thus, if a {@link User} is linked to a {@link Group} only, the
 * {@link Role} methods of this class will return null or 0. On the other hand, if a user is a member of a {@link Role} only, the {@link Group} methods of this
 * class will return null or 0.
 *
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 * @see Group
 * @see Role
 * @see User
 * @since 6.0.0
 */
public interface UserMembership extends BonitaObject {

    /**
     * @return the user membership's id
     */
    long getId();

    /**
     * @return the user membership's user id
     */
    long getUserId();

    /**
     * @return the user membership's role id or 0 if the membership is not linked to a {@link Role}
     */
    long getRoleId();

    /**
     * @return the user membership's group id or 0 if the membership is not linked to a {@link Group}
     */
    long getGroupId();

    /**
     * @return the user's id that assigned this user membership
     */
    long getAssignedBy();

    /**
     * @return the user membership's assignation date
     */
    Date getAssignedDate();

    /**
     * @return the user membership's group name or null if the membership is not linked to a {@link Group}
     */
    String getGroupName();

    /**
     * @return the user membership's role name or null if the membership is not linked to a {@link Group}
     */
    String getRoleName();

    /**
     * @return the user membership's username
     */
    String getUsername();

    /**
     * @return the user's name that created this membership's
     */
    String getAssignedByName();

    /**
     * @return the user membership's parent group path if any
     */
    String getGroupParentPath();

}
