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
package org.bonitasoft.engine.bpm.flownode;

/**
 * @author Julien Mege
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public final class HumanTaskInstanceSearchDescriptor {

    public static final String NAME = "name";

    public static final String PRIORITY = "priority";

    public static final String DUE_DATE = "dueDate";

    public static final String STATE_NAME = "state";

    public static final String SUPERVISOR_ID = "supervisorId";

    /**
     * User who supervises the process definition
     */
    public static final String USER_ID = "userId";

    /**
     * Group who supervises the process definition
     */
    public static final String GROUP_ID = "groupId";

    /**
     * Role who supervises the process definition
     */
    public static final String ROLE_ID = "roleId";

    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    public static final String PARENT_ACTIVITY_INSTANCE_ID = "parentActivityInstanceId";

    public static final String ASSIGNEE_ID = "assigneeId";

    public static final String PARENT_CONTAINER_ID = "parentContainerId";

    public static final String DISPLAY_NAME = "displayName";

    public static final String REACHED_STATE_DATE = "reachedStateDate";

}
