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
package org.bonitasoft.engine.exception;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * The class BonitaException and its subclasses are a form of Throwable that indicates conditions that a reasonable application might want to catch.
 * The class BonitaException and its subclasses that are not also subclasses of {@link RuntimeException} are checked exceptions.
 * Checked exceptions need to be declared in a method or constructor's {@literal throws} clause if they can be thrown by the execution of the method or
 * constructor and propagate outside the method or constructor boundary.
 *
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Aurelien Pupier
 */
public class BonitaException extends Exception implements BonitaContextException {

    private static final long serialVersionUID = -5413586694735909486L;

    private final Map<ExceptionContext, Serializable> context = new TreeMap<ExceptionContext, Serializable>();

    private String userName = "";

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param cause
     *        The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *        cause is nonexistent or unknown.)
     */
    public BonitaException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public BonitaException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail cause.
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *        cause is nonexistent or unknown.)
     */
    public BonitaException(final Throwable cause) {
        super(cause);
    }

    /**
     * @see org.bonitasoft.engine.exception.BonitaContextException#getUserName()
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * @see org.bonitasoft.engine.exception.BonitaContextException#setUserName(java.lang.String)
     */
    @Override
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    /**
     * @return The context of the exception
     * @since 6.3
     */
    public Map<ExceptionContext, Serializable> getContext() {
        return context;
    }

    /**
     * @param id
     *        The identifier of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionIdOnContext(final Long id) {
        context.put(ExceptionContext.PROCESS_DEFINITION_ID, id);
    }

    /**
     * @param name
     *        The name of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionNameOnContext(final String name) {
        context.put(ExceptionContext.PROCESS_NAME, name);
    }

    /**
     * @param version
     *        The version of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionVersionOnContext(final String version) {
        context.put(ExceptionContext.PROCESS_VERSION, version);
    }

    /**
     * @param id
     *        The identifier of the process instance to set
     * @since 6.3
     */
    public void setProcessInstanceIdOnContext(final Long id) {
        context.put(ExceptionContext.PROCESS_INSTANCE_ID, id);
    }

    /**
     * @param id
     *        The identifier of the root process instance to set
     * @since 6.3
     */
    public void setRootProcessInstanceIdOnContext(final Long id) {
        context.put(ExceptionContext.ROOT_PROCESS_INSTANCE_ID, id);
    }

    /**
     * @param id
     *        The identifier of the connector definition
     * @since 6.3
     */
    public void setConnectorDefinitionIdOnContext(final String id) {
        context.put(ExceptionContext.CONNECTOR_DEFINITION_ID, id);
    }

    /**
     * @param name
     *        The class name of the implementation of the connector definition to set
     * @since 6.3
     */
    public void setConnectorDefinitionImplementationClassNameOnContext(final String name) {
        context.put(ExceptionContext.CONNECTOR_DEFINITION_IMPLEMENTATION_CLASS_NAME, name);
    }

    /**
     * @param version
     *        The version of the connector definition
     * @since 6.3
     */
    public void setConnectorDefinitionVersionOnContext(final String version) {
        context.put(ExceptionContext.CONNECTOR_DEFINITION_VERSION, version);
    }

    /**
     * @param activationEvent
     *        The event which activates the connector to set
     * @since 6.3
     */
    public void setConnectorActivationEventOnContext(final String activationEvent) {
        context.put(ExceptionContext.CONNECTOR_ACTIVATION_EVENT, activationEvent);
    }

    /**
     * @param id
     *        The identifier of the connector instance to set
     * @since 6.3
     */
    public void setConnectorInstanceIdOnContext(final long id) {
        context.put(ExceptionContext.CONNECTOR_INSTANCE_ID, id);
    }

    /**
     * @param id
     *        The identifier of the flow node definition to set
     * @since 6.3
     */
    public void setFlowNodeDefinitionIdOnContext(final long id) {
        context.put(ExceptionContext.FLOW_NODE_DEFINITION_ID, id);
    }

    /**
     * @param id
     *        The identifier of the flow node instance to set
     * @since 6.3
     */
    public void setFlowNodeInstanceIdOnContext(final long id) {
        context.put(ExceptionContext.FLOW_NODE_INSTANCE_ID, id);
    }

    /**
     * @param name
     *        The name of the flow node to set
     * @since 6.3
     */
    public void setFlowNodeNameOnContext(final String name) {
        context.put(ExceptionContext.FLOW_NODE_NAME, name);
    }

    /**
     * @param name
     *        The name of the message instance to set
     * @since 6.3
     */
    public void setMessageInstanceNameOnContext(final String name) {
        context.put(ExceptionContext.MESSAGE_INSTANCE_NAME, name);
    }

    /**
     * @param name
     *        The target process name of the message instance to set
     * @since 6.3
     */
    public void setMessageInstanceTargetProcessOnContext(final String name) {
        context.put(ExceptionContext.MESSAGE_INSTANCE_TARGET_PROCESS_NAME, name);
    }

    /**
     * @param name
     *        The target flow node name of the message instance to set
     * @since 6.3
     */
    public void setMessageInstanceTargetFlowNodeOnContext(final String name) {
        context.put(ExceptionContext.MESSAGE_INSTANCE_TARGET_FLOW_NODE_NAME, name);
    }

    /**
     * @param eventType
     *        The event type of the waiting message instance to set
     * @since 6.3
     */
    public void setWaitingMessageEventTypeOnContext(final String eventType) {
        context.put(ExceptionContext.WAITING_MESSAGE_INSTANCE_TYPE, eventType);
    }

    /**
     * @param id
     *        The identifier of the document
     * @since 6.3
     */
    public void setDocumentIdOnContext(final long id) {
        context.put(ExceptionContext.DOCUMENT_ID, id);
    }

    /**
     * @param userId
     *        The identifier of the user
     * @since 6.3
     */
    public void setUserIdOnContext(final Long userId) {
        context.put(ExceptionContext.USER_ID, userId);
    }

    /**
     * @param groupId
     *        The identifier of the group
     * @since 6.3
     */
    public void setGroupIdOnContext(final Long groupId) {
        context.put(ExceptionContext.GROUP_ID, groupId);
    }

    /**
     * @param roleId
     *        The identifier of the role
     * @since 6.3
     */
    public void setRoleIdOnContext(final Long roleId) {
        context.put(ExceptionContext.ROLE_ID, roleId);
    }

    /**
     * @param name
     *        The name of the data
     * @since 6.4.2
     */
    public void setDataName(final String name) {
        context.put(ExceptionContext.DATA_NAME, name);
    }

    /**
     * @param dataClassName
     *        The class name of the data
     * @since 6.4.2
     */
    public void setDataClassName(final String dataClassName) {
        context.put(ExceptionContext.DATA_CLASS_NAME, dataClassName);
    }

    @Override
    public String getMessage() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getUserNameMessage());
        appendContextMessage(stringBuilder);
        stringBuilder.append(super.getMessage());
        return stringBuilder.toString();
    }

    private void appendContextMessage(final StringBuilder stringBuilder) {
        if (!context.isEmpty()) {
            for (final Entry<ExceptionContext, Serializable> entry : context.entrySet()) {
                stringBuilder.append(entry.getKey() + "=" + entry.getValue() + " | ");
            }
        }
    }

    private String getUserNameMessage() {
        return userName != null && !userName.isEmpty() ? "USERNAME=" + userName + " | " : "";
    }

}
