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
package org.bonitasoft.engine.bpm.actor;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * It is used to map an {@link ActorInstance} with the organization.
 * It's possible to map an {@link ActorInstance} with a user, or a group, or a role, or a group and a role.
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @since 6.0.0
 * @version 6.4.1
 */
public interface ActorMember extends BonitaObject {

    /**
     * Get the identifier of the actor member.
     *
     * @return The identifier of the actor member.
     */
    long getId();

    /**
     * Get the identifier of the user.
     *
     * @return The identifier of the user.
     */
    long getUserId();

    /**
     * Get the identifier of the group.
     *
     * @return The identifier of the group.
     */
    long getGroupId();

    /**
     * Get the identifier of the role.
     * 
     * @return The identifier of the role.
     */
    long getRoleId();

}
