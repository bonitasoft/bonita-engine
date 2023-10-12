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
package org.bonitasoft.engine.bpm.process;

/**
 * Search descriptors are used to filter / sort results of a generic search. <br>
 * ProcessInstanceSearchDescriptor defines the fields that can be used as filters or sort fields on
 * <code>List&lt;ProcessInstance&gt;</code> returning
 * methods.
 *
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @see org.bonitasoft.engine.api.ProcessRuntimeAPI
 * @see ProcessInstance
 * @version 6.3.5
 * @since 6.0.0
 */
public class ProcessInstanceSearchDescriptor {

    /**
     * The field corresponding to the identifier of the process definition.
     */
    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    /**
     * The field corresponding to the name of the process.
     */
    public static final String NAME = "name";

    /**
     * The field corresponding to the identifier of the process instance.
     */
    public static final String ID = "id";

    /**
     * The field corresponding to the identifier of the user assignee to a user task of the process instance.
     */
    public static final String ASSIGNEE_ID = "assigneeId";

    /**
     * The field corresponding to the identifier of the user who started the process instance.
     */
    public static final String STARTED_BY = "startedBy";

    /**
     * The field corresponding to the identifier of the user who started the process instance for the user in
     * {@link ProcessInstance#getStartedBy()}.
     */
    public static final String STARTED_BY_SUBSTITUTE = "startedBySubstitute";

    /**
     * The field corresponding to the date when the process instance is started.
     */
    public static final String START_DATE = "startDate";

    /**
     * The field corresponding to the date when the process instance completed.
     */
    public static final String END_DATE = "endDate";

    /**
     * The field corresponding to the last date of the updating of the process instance.
     */
    public static final String LAST_UPDATE = "lastUpdate";

    /**
     * The field corresponding to the identifier of the user who supervised the process instance.
     */
    public static final String PROCESS_SUPERVISOR_USER_ID = "userId";

    /**
     * The field corresponding to the identifier of the group who supervised the process instance.
     */
    public static final String PROCESS_SUPERVISOR_GROUP_ID = "groupId";

    /**
     * The field corresponding to the identifier of the role who supervised the process instance.
     */
    public static final String PROCESS_SUPERVISOR_ROLE_ID = "roleId";

    /**
     * The field corresponding to the identifier of the state of the process instance.
     */
    public static final String STATE_ID = "stateId";

    /**
     * The field corresponding to the name of the state of the process instance.
     * The value can be of the type String or ProcessInstanceState.
     *
     * @see ProcessInstanceState
     */
    public static final String STATE_NAME = "stateName";

    /**
     * The field corresponding to the identifier of the flow node that starts the process instance.
     */
    public static final String CALLER_ID = "callerId";

    /**
     * The field corresponding to the first search key of the process instance.
     */
    public static final String STRING_INDEX_1 = "index1";

    /**
     * The field corresponding to the second search key of the process instance.
     */
    public static final String STRING_INDEX_2 = "index2";

    /**
     * The field corresponding to the third search key of the process instance.
     */
    public static final String STRING_INDEX_3 = "index3";

    /**
     * The field corresponding to the fourth search key of the process instance.
     */
    public static final String STRING_INDEX_4 = "index4";

    /**
     * The field corresponding to the fifth search key of the process instance.
     */
    public static final String STRING_INDEX_5 = "index5";

}
