/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
 * It is used to map a actorInstance with the organisation.
 * It's possible to map a actorInstance with a user, or a group, or a role, or a group and a role.
 * 
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ActorMember extends BonitaObject {

    /**
     * @return The identifier of the actor member
     */
    long getId();

    /**
     * @return The identifier of the user
     */
    long getUserId();

    /**
     * @return The identifier of the group
     */
    long getGroupId();

    /**
     * @return The identifier of the role
     */
    long getRoleId();

}
