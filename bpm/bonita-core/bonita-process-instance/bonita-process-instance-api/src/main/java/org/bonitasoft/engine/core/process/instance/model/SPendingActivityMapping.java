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
package org.bonitasoft.engine.core.process.instance.model;

import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * Used to get pending activities of a user.
 * Link activity to
 * - an actor if the activity is not filtered
 * - a user if the activity is filtered
 * When the activity is created we insert one or more of this object:
 * - if the activity is not filtered we insert one of them for each actor of the activity with inside the actorId
 * - if the activity is filtered we execute those filter and insert a one of them for each user that is filtered
 * 
 * @author Baptiste Mesta
 */
public interface SPendingActivityMapping extends PersistentObject {

    /**
     * the id of the activity
     */
    long getActivityId();

    /**
     * the id of the actor or -1 if the mapping is on user
     */
    long getActorId();

    /**
     * the id of the user or -1 if the mapping is on actor
     */
    long getUserId();
}
