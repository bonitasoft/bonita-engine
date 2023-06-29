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
package org.bonitasoft.engine.expression;

/**
 * @author Baptiste Mesta
 */
public enum ExpressionConstants {

    /**
     * active instances on amulti instance activity
     */
    NUMBER_OF_ACTIVE_INSTANCES("numberOfActiveInstances", Integer.class.getName()),

    NUMBER_OF_COMPLETED_INSTANCES("numberOfCompletedInstances", Integer.class.getName()),

    NUMBER_OF_TERMINATED_INSTANCES("numberOfTerminatedInstances", Integer.class.getName()),

    NUMBER_OF_INSTANCES("numberOfInstances", Integer.class.getName()),

    // inside loop
    LOOP_COUNTER("loopCounter", Integer.class.getName()),

    API_ACCESSOR("apiAccessor", "org.bonitasoft.engine.api.APIAccessor"),

    CONNECTOR_API_ACCESSOR("connectorApiAccessor", "org.bonitasoft.engine.api.APIAccessor"),

    ENGINE_EXECUTION_CONTEXT("engineExecutionContext", "org.bonitasoft.engine.connector.EngineExecutionContext"),

    PROCESS_DEFINITION_ID("processDefinitionId", Long.class.getName()),

    ACTIVITY_INSTANCE_ID("activityInstanceId", Long.class.getName()),

    PROCESS_INSTANCE_ID("processInstanceId", Long.class.getName()),

    ROOT_PROCESS_INSTANCE_ID("rootProcessInstanceId", Long.class.getName()),

    LOGGED_USER_ID("loggedUserId", Long.class.getName()),

    TASK_ASSIGNEE_ID("taskAssigneeId", Long.class.getName());

    private final String engineConstantName;

    private final String returnType;

    ExpressionConstants(final String engineName, final String returnType) {
        engineConstantName = engineName;
        this.returnType = returnType;
        ExpressionConstantsResolver.initMap(this);
    }

    public String getEngineConstantName() {
        return engineConstantName;
    }

    public String getReturnType() {
        return returnType;
    }

}
