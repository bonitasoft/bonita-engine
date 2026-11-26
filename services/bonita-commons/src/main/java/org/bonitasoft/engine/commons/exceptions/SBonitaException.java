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
package org.bonitasoft.engine.commons.exceptions;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class SBonitaException extends Exception implements ExceptionContext, ScopedException {

    private static final long serialVersionUID = -500856379312027085L;

    private final String exceptionId;

    private final Object[] arguments;

    private String scope = ScopedException.UNKNOWN_SCOPE;

    private final Map<SExceptionContext, Serializable> context = new TreeMap<SExceptionContext, Serializable>();

    /**
     * Default constructor
     */
    protected SBonitaException() {
        this((Object[]) null);
    }

    /**
     * @param arguments
     */
    protected SBonitaException(final Object... arguments) {
        super();
        exceptionId = this.getClass().getName();
        this.arguments = arguments;
    }

    /**
     * @param message
     */
    protected SBonitaException(final String message) {
        super(message);
        exceptionId = this.getClass().getName();
        arguments = null;
    }

    protected SBonitaException(final String message, String scope) {
        super(message);
        this.scope = scope;
        exceptionId = this.getClass().getName();
        arguments = null;
    }

    /**
     * @param message
     * @param cause
     */
    protected SBonitaException(final String message, final Throwable cause) {
        super(message, cause);
        exceptionId = this.getClass().getName();
        arguments = null;
        if (cause instanceof SBonitaException e && e.getContext() != null) {
            this.context.putAll(e.getContext());
        }
        if (cause instanceof ScopedException e) {
            this.scope = e.getScope();
        }
    }

    protected SBonitaException(final String message, String scope, final Throwable cause) {
        super(message, cause);
        exceptionId = this.getClass().getName();
        arguments = null;
        this.scope = scope;
        if (cause instanceof SBonitaException e && e.getContext() != null) {
            this.context.putAll(e.getContext());
        }
    }

    /**
     * @param cause
     */
    protected SBonitaException(final Throwable cause) {
        this(cause, (Object[]) null);
        if (cause instanceof ScopedException e) {
            this.scope = e.getScope();
        }
    }

    /**
     * @param cause
     * @param arguments
     */
    protected SBonitaException(final Throwable cause, final Object... arguments) {
        super(cause);
        exceptionId = this.getClass().getName();
        this.arguments = arguments;
        if (cause instanceof SBonitaException e && e.getContext() != null) {
            this.context.putAll(e.getContext());
        }
        if (cause instanceof ScopedException e) {
            this.scope = e.getScope();
        }
    }

    @Override
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * This exception id is used to find potential causes
     *
     * @return the Id of the exception
     */
    public String getExceptionId() {
        return exceptionId;
    }

    /**
     * @return
     */
    public Object[] getParameters() {
        return arguments;
    }

    /**
     * @return The context of the exception
     * @since 6.3
     */
    @Override
    public Map<SExceptionContext, Serializable> getContext() {
        return context;
    }

    /**
     * @param id
     *        The identifier of the process definition to set
     * @since 6.3
     */
    @Override
    public void setProcessDefinitionIdOnContext(final long id) {
        context.put(SExceptionContext.PROCESS_DEFINITION_ID, id);
    }

    /**
     * @param name
     *        The name of the process definition to set
     * @since 6.3
     */
    @Override
    public void setProcessDefinitionNameOnContext(final String name) {
        context.put(SExceptionContext.PROCESS_NAME, name);
    }

    /**
     * @param version
     *        The version of the process definition to set
     * @since 6.3
     */
    @Override
    public void setProcessDefinitionVersionOnContext(final String version) {
        context.put(SExceptionContext.PROCESS_VERSION, version);
    }

    /**
     * @param id
     *        The identifier of the process instance to set
     * @since 6.3
     */
    @Override
    public void setProcessInstanceIdOnContext(final long id) {
        context.put(SExceptionContext.PROCESS_INSTANCE_ID, id);
    }

    /**
     * @param id
     *        The identifier of the root process instance to set
     * @since 6.3
     */
    @Override
    public void setRootProcessInstanceIdOnContext(final long id) {
        context.put(SExceptionContext.ROOT_PROCESS_INSTANCE_ID, id);
    }

    /**
     * @param id
     *        The identifier of the connector definition
     * @since 6.3
     */
    @Override
    public void setConnectorDefinitionIdOnContext(final String id) {
        context.put(SExceptionContext.CONNECTOR_DEFINITION_ID, id);
    }

    /**
     * @param name
     *        The class name of the implementation of the connector definition to set
     * @since 6.3
     */
    @Override
    public void setConnectorImplementationClassNameOnContext(final String name) {
        context.put(SExceptionContext.CONNECTOR_IMPLEMENTATION_CLASS_NAME, name);
    }

    /**
     * @param version
     *        The version of the connector definition
     * @since 6.3
     */
    @Override
    public void setConnectorDefinitionVersionOnContext(final String version) {
        context.put(SExceptionContext.CONNECTOR_DEFINITION_VERSION, version);
    }

    /**
     * @param activationEvent
     *        The event which activates the connector to set
     * @since 6.3
     */
    @Override
    public void setConnectorActivationEventOnContext(final String activationEvent) {
        context.put(SExceptionContext.CONNECTOR_ACTIVATION_EVENT, activationEvent);
    }

    /**
     * @param id
     *        The identifier of the connector instance to set
     * @since 6.3
     */
    @Override
    public void setConnectorInstanceIdOnContext(final long id) {
        context.put(SExceptionContext.CONNECTOR_INSTANCE_ID, id);
    }

    /**
     * @param id
     *        The identifier of the flow node definition to set
     * @since 6.3
     */
    @Override
    public void setFlowNodeDefinitionIdOnContext(final long id) {
        context.put(SExceptionContext.FLOW_NODE_DEFINITION_ID, id);
    }

    /**
     * @param id
     *        The identifier of the flow node instance to set
     * @since 6.3
     */
    @Override
    public void setFlowNodeInstanceIdOnContext(final long id) {
        context.put(SExceptionContext.FLOW_NODE_INSTANCE_ID, id);
    }

    /**
     * @param name
     *        The name of the flow node to set
     * @since 6.3
     */
    @Override
    public void setFlowNodeNameOnContext(final String name) {
        context.put(SExceptionContext.FLOW_NODE_NAME, name);
    }

    /**
     * @param name
     *        The name of the message instance to set
     * @since 6.3
     */
    @Override
    public void setMessageInstanceNameOnContext(final String name) {
        context.put(SExceptionContext.MESSAGE_INSTANCE_NAME, name);
    }

    /**
     * @param name
     *        The target process name of the message instance to set
     * @since 6.3
     */
    @Override
    public void setMessageInstanceTargetProcessOnContext(final String name) {
        context.put(SExceptionContext.MESSAGE_INSTANCE_TARGET_PROCESS_NAME, name);
    }

    /**
     * @param name
     *        The target flow node name of the message instance to set
     * @since 6.3
     */
    @Override
    public void setMessageInstanceTargetFlowNodeOnContext(final String name) {
        context.put(SExceptionContext.MESSAGE_INSTANCE_TARGET_FLOW_NODE_NAME, name);
    }

    /**
     * @param eventType
     *        The event type of the waiting message instance to set
     * @since 6.3
     */
    @Override
    public void setWaitingMessageEventTypeOnContext(final String eventType) {
        context.put(SExceptionContext.WAITING_MESSAGE_INSTANCE_TYPE, eventType);
    }

    /**
     * @param id
     *        The identifier of the document
     * @since 6.3
     */
    @Override
    public void setDocumentIdOnContext(final long id) {
        context.put(SExceptionContext.DOCUMENT_ID, id);
    }

    /**
     * @param userId
     *        The identifier of the user
     * @since 6.3
     */
    @Override
    public void setUserIdOnContext(final long userId) {
        context.put(SExceptionContext.USER_ID, userId);
    }

    /**
     * @param groupId
     *        The identifier of the group
     * @since 6.3
     */
    @Override
    public void setGroupIdOnContext(final long groupId) {
        context.put(SExceptionContext.GROUP_ID, groupId);
    }

    /**
     * @param roleId
     *        The identifier of the role
     * @since 6.3
     */
    @Override
    public void setRoleIdOnContext(final long roleId) {
        context.put(SExceptionContext.ROLE_ID, roleId);
    }

    @Override
    public void setConnectorInputOnContext(String inputName) {
        context.put(SExceptionContext.CONNECTOR_INPUT_NAME, inputName);
    }

    @Override
    public void setConnectorNameOnContext(String name) {
        context.put(SExceptionContext.CONNECTOR_NAME, name);
    }

    @Override
    public String getMessage() {
        final StringBuilder stringBuilder = new StringBuilder();
        appendContextMessage(stringBuilder);
        appendCauseMessage(stringBuilder);
        return stringBuilder.toString();
    }

    private void appendCauseMessage(final StringBuilder stringBuilder) {
        String message = super.getMessage();
        if (message != null && message.isEmpty() && getCause() != null) {
            message = getCause().getMessage();
        }
        if (message != null && !message.trim().isEmpty()) {
            stringBuilder.append(message);
        }
    }

    private void appendContextMessage(final StringBuilder stringBuilder) {
        if (context != null && !context.isEmpty()) {
            for (final Entry<SExceptionContext, Serializable> entry : context.entrySet()) {
                stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append(" | ");
            }
        }
    }

}
