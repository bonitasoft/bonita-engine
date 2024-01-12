/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.model.bpm.flownode;

import java.util.Date;

import org.bonitasoft.web.rest.model.bpm.process.ActorItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public interface IHumanTaskItem extends ITaskItem {

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES NAMES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String ATTRIBUTE_ASSIGNED_USER_ID = "assigned_id";

    String ATTRIBUTE_ASSIGNED_DATE = "assigned_date";

    String ATTRIBUTE_PRIORITY = "priority";

    String ATTRIBUTE_DUE_DATE = "dueDate";

    String ATTRIBUTE_ACTOR_ID = "actorId";

    /*
     * Same as ATTRIBUTE_PARENT_CONTAINER_ID
     * Should be in manual task but lives there because of deploy restrictions
     */
    String ATTRIBUTE_PARENT_TASK_ID = "parentTaskId";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES VALUES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String VALUE_PRIORITY_HIGHEST = "highest";

    String VALUE_PRIORITY_ABOVE_NORMAL = "above_normal";

    String VALUE_PRIORITY_NORMAL = "normal";

    String VALUE_PRIORITY_UNDER_NORMAL = "under_normal";

    String VALUE_PRIORITY_LOWEST = "lowest";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COUNTS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String COUNT_ATTACHMENT_NUMBER = "nb_of_attachment";

    String COUNT_ACTOR_USER_NUMBER = "nb_of_actor_user";

    String COUNT_COMMENT_NUMBER = "nb_of_comment";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String FILTER_TEAM_MANAGER_ID = "team_manager_id";

    String FILTER_USER_ID = "user_id";

    String FILTER_HIDDEN_TO_USER_ID = "hidden_user_id";

    String FILTER_IS_ASSIGNED = "is_claimed";

    String FILTER_SHOW_ASSIGNED_TO_OTHERS = "show_assigned_to_others";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // SETTERS

    void setAssignedId(final String id);

    void setAssignedId(final APIID id);

    void setAssignedId(final Long id);

    void setAssignedDate(final String date);

    void setAssignedDate(final Date date);

    void setPriority(final String priority);

    void setDueDate(final String date);

    void setDueDate(final Date date);

    void setActorId(final APIID id);

    void setActorId(final String actorId);

    void setActorId(final Long actorId);

    // Counters
    void setNbOfAttachment(final String count);

    void setNbOfAttachment(final int count);

    void setNbOfComment(final String count);

    void setNbOfComment(final int count);

    void setNbOfActorUser(final String count);

    void setNbOfActorUser(final int count);

    // GETTERS

    APIID getActorId();

    String getPriority();

    String getDueDate();

    APIID getAssignedId();

    String getAssignedDate();

    // Counters
    Integer getNbOfAttachment();

    Integer getNbOfComment();

    Integer getNbOfActorUser();

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPLOYS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    UserItem getAssignedUser();

    ActorItem getActor();

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    boolean isAssigned();

    boolean isUnassigned();

}
