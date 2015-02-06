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
package org.bonitasoft.engine.commons.exceptions;

/**
 * To define the context of an exception in the message.
 * 
 * @author Celine Souchet
 */
public enum SExceptionContext {
    /**
     * Corresponding to the identifier of the process definition
     */
    PROCESS_DEFINITION_ID,
    /**
     * Corresponding to the name of the process definition
     */
    PROCESS_NAME,
    /**
     * Corresponding to the version of the process definition
     */
    PROCESS_VERSION,
    /**
     * Corresponding to the identifier of the process instance
     */
    PROCESS_INSTANCE_ID,
    /**
     * Corresponding to the identifier of the root process instance
     */
    ROOT_PROCESS_INSTANCE_ID,
    /**
     * Corresponding to the identifier of the flow node definition
     */
    FLOW_NODE_DEFINITION_ID,
    /**
     * Corresponding to the identifier of the flow node instance
     */
    FLOW_NODE_INSTANCE_ID,
    /**
     * Corresponding to the name of the flow node
     */
    FLOW_NODE_NAME,
    /**
     * Corresponding to the name of the Message Instance
     */
    MESSAGE_INSTANCE_NAME,
    /**
     * Corresponding to the target process name of the Message Instance
     */
    MESSAGE_INSTANCE_TARGET_PROCESS_NAME,
    /**
     * Corresponding to the target flow node name of the Message Instance
     */
    MESSAGE_INSTANCE_TARGET_FLOW_NODE_NAME,
    /**
     * Corresponding to the event type of the Waiting Message Instance
     */
    WAITING_MESSAGE_INSTANCE_TYPE,
    /**
     * Corresponding to the identifier of the connector definition
     */
    CONNECTOR_DEFINITION_ID,
    /**
     * Corresponding to the class name of the implementation of the connector definition
     */
    CONNECTOR_DEFINITION_IMPLEMENTATION_CLASS_NAME,
    /**
     * Corresponding to the version of the connector definition
     */
    CONNECTOR_DEFINITION_VERSION,
    /**
     * Corresponding to the event which activates the connector
     */
    CONNECTOR_ACTIVATION_EVENT,
    /**
     * Corresponding to the identifier of the connector instance
     */
    CONNECTOR_INSTANCE_ID,
    /**
     * Corresponding to the identifier of the user
     */
    USER_ID,
    /**
     * Corresponding to the identifier of the group
     */
    GROUP_ID,
    /**
     * Corresponding to the identifier of the role
     */
    ROLE_ID,
    /**
     * Corresponding to the identifier of the document
     */
    DOCUMENT_ID;

}
