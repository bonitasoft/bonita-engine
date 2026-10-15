/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.mdc;

import java.util.Map;
import java.util.Optional;

public class FlowNodeInstanceMDC extends AbstractMDC implements AutoCloseable, MDCConstants {

    /**
     * Build a flow node execution context when no user is involved (e.g. service task).
     *
     * @param flowNodeInstanceId the id of the flow node instance
     * @param processDefinitionId the id of the process definition
     * @param processInstanceId the id of the process instance / case
     * @param rootProcessInstanceId the id of the root process instance / case
     */
    public FlowNodeInstanceMDC(long flowNodeInstanceId, long processDefinitionId, long processInstanceId,
            long rootProcessInstanceId) {
        this(flowNodeInstanceId, Optional.empty(), Optional.empty(), processDefinitionId, processInstanceId,
                rootProcessInstanceId);
    }

    /**
     * Build a flow node execution context when a user is authenticated.
     *
     * @param flowNodeInstanceId the id of the flow node instance
     * @param executerId the id of the user executing the flow node
     * @param substituteUserId the id of the admin user activating the flow node for the executer
     * @param processDefinitionId the id of the process definition
     * @param processInstanceId the id of the process instance / case
     * @param rootProcessInstanceId the id of the root process instance / case
     */
    public FlowNodeInstanceMDC(long flowNodeInstanceId, long executerId, long substituteUserId,
            long processDefinitionId, long processInstanceId, long rootProcessInstanceId) {
        this(flowNodeInstanceId, Optional.of(executerId), Optional.of(substituteUserId), processDefinitionId,
                processInstanceId, rootProcessInstanceId);
    }

    /**
     * Build a flow node execution context.
     *
     * @param flowNodeInstanceId the id of the flow node instance
     * @param executerId the eventual id of the user executing the flow node
     * @param substituteUserId the eventual id of the admin user activating the flow node for the executer
     * @param processDefinitionId the id of the process definition
     * @param processInstanceId the id of the process instance / case
     * @param rootProcessInstanceId the id of the root process instance / case
     */
    public FlowNodeInstanceMDC(long flowNodeInstanceId, Optional<Long> executerId, Optional<Long> substituteUserId,
            long processDefinitionId, long processInstanceId, long rootProcessInstanceId) {
        super(buildContextMap(flowNodeInstanceId, executerId, substituteUserId, processDefinitionId, processInstanceId,
                rootProcessInstanceId));
    }

    private static Map<String, String> buildContextMap(long flowNodeInstanceId, Optional<Long> executerId,
            Optional<Long> substituteUserId, long processDefinitionId, long processInstanceId,
            long rootProcessInstanceId) {
        if (!substituteUserId.equals(executerId)) {
            // executed by substituteUserId for executerId
            return Map.of(
                    FLOW_NODE_INSTANCE_ID, String.valueOf(flowNodeInstanceId),
                    USER_ID, executerId.map(String::valueOf).orElse(null),
                    SUBSTITUTE_USER_ID, substituteUserId.map(String::valueOf).orElse(null),
                    PROCESS_DEFINITION_ID, String.valueOf(processDefinitionId),
                    PROCESS_INSTANCE_ID, String.valueOf(processInstanceId),
                    ROOT_PROCESS_INSTANCE_ID, String.valueOf(rootProcessInstanceId));
        } else if (executerId.isPresent()) {
            // no need for SUBSTITUTE_USER_ID
            return Map.of(
                    FLOW_NODE_INSTANCE_ID, String.valueOf(flowNodeInstanceId),
                    USER_ID, executerId.map(String::valueOf).orElse(null),
                    PROCESS_DEFINITION_ID, String.valueOf(processDefinitionId),
                    PROCESS_INSTANCE_ID, String.valueOf(processInstanceId),
                    ROOT_PROCESS_INSTANCE_ID, String.valueOf(rootProcessInstanceId));
        } else {
            // no authenticated user
            return Map.of(
                    FLOW_NODE_INSTANCE_ID, String.valueOf(flowNodeInstanceId),
                    PROCESS_DEFINITION_ID, String.valueOf(processDefinitionId),
                    PROCESS_INSTANCE_ID, String.valueOf(processInstanceId),
                    ROOT_PROCESS_INSTANCE_ID, String.valueOf(rootProcessInstanceId));
        }
    }
}
