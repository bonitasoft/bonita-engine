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
package org.bonitasoft.engine.connector;

import java.io.Serializable;

import org.bonitasoft.engine.expression.ExpressionConstants;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class EngineExecutionContext implements Serializable {

    private static final long serialVersionUID = -57239373625030379L;

    private long processDefinitionId = -1;

    private long activityInstanceId = -1;

    private long processInstanceId = -1;

    private long rootProcessInstanceId;

    private long taskAssigneeId = -1;

    /**
     * @return the current process definition ID
     */
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(final long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    /**
     * Refers to the directly including process instance.
     * 
     * @param processInstanceId
     *            the ID of the directly including process instance
     */
    public void setProcessInstanceId(final long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    /**
     * Refers to the directly including process instance.
     * 
     * @return the ID of the directly including process instance
     */
    public long getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(final long activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    /**
     * Refers to the top-level including process instance.
     * 
     * @param rootProcessInstanceId
     *            the ID of the top-level including process instance
     */
    public void setRootProcessInstanceId(final long rootProcessInstanceId) {
        this.rootProcessInstanceId = rootProcessInstanceId;
    }

    /**
     * Refers to the top-level including process instance.
     * 
     * @return
     *         the ID of the top-level including process instance
     */
    public long getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public Serializable getExpressionConstant(final ExpressionConstants constant) {
        switch (constant) {
            case ACTIVITY_INSTANCE_ID:
                return activityInstanceId;
            case PROCESS_INSTANCE_ID:
                return processInstanceId;
            case PROCESS_DEFINITION_ID:
                return processDefinitionId;
            case ROOT_PROCESS_INSTANCE_ID:
                return rootProcessInstanceId;
            case ENGINE_EXECUTION_CONTEXT:
                return this;
            case TASK_ASSIGNEE_ID:
                return taskAssigneeId;
            default:
                return -1;
        }
    }

    public long getTaskAssigneeId() {
        return taskAssigneeId;
    }

    public void setTaskAssigneeId(final long taskAssigneeId) {
        this.taskAssigneeId = taskAssigneeId;
    }

}
