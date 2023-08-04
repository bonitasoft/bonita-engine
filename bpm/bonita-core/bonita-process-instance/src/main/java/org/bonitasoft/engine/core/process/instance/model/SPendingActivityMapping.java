/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pending_mapping")
@IdClass(PersistentObjectId.class)
public class SPendingActivityMapping implements PersistentObject {

    public static final String ACTOR_ID = "actorId";
    public static final String ACTIVITY_ID = "activityId";
    public static final String USER_ID = "userId";
    @Id
    private long id;
    @Id
    private long tenantId;
    /**
     * the id of the activity
     */
    private long activityId;
    /**
     * the id of the actor or -1 if the mapping is on user
     */
    @Builder.Default
    private long actorId = -1;
    /**
     * the id of the user or -1 if the mapping is on actor
     */
    @Builder.Default
    private long userId = -1;

}
