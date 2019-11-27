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
package org.bonitasoft.engine.bpm.flownode;

/**
 * @author Emmanuel Duchastenier
 * @author Elias Ricken de Medeiros
 */
public class ArchivedFlowNodeInstanceSearchDescriptor {

    public static final String NAME = "name";

    public static final String STATE_NAME = "state";

    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    public static final String PARENT_PROCESS_INSTANCE_ID = "parentProcessInstanceId";

    public static final String PARENT_ACTIVITY_INSTANCE_ID = "parentActivityInstanceId";

    public static final String ROOT_PROCESS_INSTANCE_ID = "rootProcessInsanceId";

    public static final String DISPLAY_NAME = "displayName";

    public static final String FLOW_NODE_TYPE = "kind";

    public static final String ORIGINAL_FLOW_NODE_ID = "sourceObjectId";

    public static final String TERMINAL = "terminal";

    public static final String REACHED_STATE_DATE = "reachedStateDate";

    public static final String ARCHIVE_DATE = "archiveDate";

    public static final String STATE_ID = "stateId";

}
