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
package org.bonitasoft.engine.bpm.process;

/**
 * Search descriptors are used to filter / sort results of a generic search. <br>
 * ArchivedProcessInstancesSearchDescriptor defines the fields that can be used as filters or sort fields on <code>List&lt;ArchivedProcessInstance&gt;</code> returning methods.
 * 
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @see org.bonitasoft.engine.api.ProcessRuntimeAPI
 * @see ArchivedProcessInstance
 * @version 6.3.5
 * @since 6.0.0
 */
public class ArchivedProcessInstancesSearchDescriptor {

    /**
     * The field corresponding to the date of the archiving of the process instance.
     */
    public static final String ARCHIVE_DATE = "archiveDate";

    /**
     * The field corresponding to the identifier of the archived process instance.
     */
    public static final String ID = "id";

    /**
     * The field corresponding to the name of the process.
     */
    public static final String NAME = "name";

    /**
     * The field corresponding to the identifier of the process definition.
     */
    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    /**
     * The field corresponding to the identifier of the state of the archived process instance.
     */
    public static final String STATE_ID = "stateId";

    /**
     * The field corresponding to the identifier of the running process instance.
     */
    public static final String SOURCE_OBJECT_ID = "sourceObjectId";

    /**
     * The field corresponding to the date of the end of the process instance.
     */
    public static final String END_DATE = "endDate";

    /**
     * The field corresponding to the identifier of the user who started the process instance.
     */
    public static final String STARTED_BY = "startedBy";

    /**
     * The field corresponding to the identifier of the user who started the process instance for the user in {@link ProcessInstance#getStartedBy()}.
     */
    public static final String STARTED_BY_SUBSTITUTE = "startedBySubstitute";

    /**
     * The field corresponding to the date when the process instance is started.
     */
    public static final String START_DATE = "startDate";

    /**
     * The field corresponding to the last date of the updating of the process instance.
     */
    public static final String LAST_UPDATE = "lastUpdate";

    /**
     * The field corresponding to the identifier of the user who supervised the process instance.
     */
    public static final String USER_ID = "userId";

    /**
     * The field corresponding to the identifier of the group who supervised the process instance.
     */
    public static final String GROUP_ID = "groupId";

    /**
     * The field corresponding to the identifier of the role who supervised the process instance.
     */
    public static final String ROLE_ID = "roleId";

    /**
     * The field corresponding to the identifier of the user assignee to a user task of the process instance.
     */
    public static final String ASSIGNEE_ID = "assigneeId";

    /**
     * The field corresponding to the identifier of the flow node that starts the process instance.
     */
    public static final String CALLER_ID = "callerId";

}
