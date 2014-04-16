/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class ProcessInstanceSearchDescriptor {

    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    public static final String NAME = "name";

    public static final String ID = "id";

    public static final String ASSIGNEE_ID = "assigneeId";

    public static final String STARTED_BY = "startedBy";

    public static final String STARTED_BY_SUBSTITUTE = "startedBySubstitute";

    public static final String START_DATE = "startDate";

    public static final String LAST_UPDATE = "lastUpdate";

    public static final String USER_ID = "userId";

    public static final String GROUP_ID = "groupId";

    public static final String ROLE_ID = "roleId";

    public static final String STATE_ID = "stateId";

    public static final String CALLER_ID = "callerId";

}
