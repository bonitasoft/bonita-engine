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
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SBonitaRuntimeException extends RuntimeException implements ExceptionContext, ScopedException {

    private static final long serialVersionUID = 7268935639620676043L;

    private final Map<SExceptionContext, Serializable> context = new TreeMap<>();
    private String scope = ScopedException.UNKNOWN_SCOPE;

    public SBonitaRuntimeException(final Throwable cause) {
        super(cause);
        if (cause instanceof SBonitaException e && e.getContext() != null) {
            this.context.putAll(e.getContext());
        }
        if (cause instanceof ScopedException e) {
            this.scope = e.getScope();
        }
    }

    public SBonitaRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
        if (cause instanceof SBonitaException e && e.getContext() != null) {
            this.context.putAll(e.getContext());
        }
        if (cause instanceof ScopedException e) {
            this.scope = e.getScope();
        }
    }

    public SBonitaRuntimeException(final String message, String scope, final Throwable cause) {
        super(message, cause);
        this.scope = scope;
        if (cause instanceof SBonitaException e && e.getContext() != null) {
            this.context.putAll(e.getContext());
        }
    }

    public SBonitaRuntimeException(final String message) {
        super(message);
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    /**
     * @return The context of the exception
     * @since 6.3
     */
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

    @Override
    public void setConnectorImplementationClassNameOnContext(String name) {
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

    @Override
    public void setDocumentIdOnContext(long id) {
        context.put(SExceptionContext.DOCUMENT_ID, id);
    }

    @Override
    public void setUserIdOnContext(long userId) {
        context.put(SExceptionContext.USER_ID, userId);
    }

    @Override
    public void setGroupIdOnContext(long groupId) {
        context.put(SExceptionContext.GROUP_ID, groupId);
    }

    @Override
    public void setRoleIdOnContext(long roleId) {
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
        if (!context.isEmpty()) {
            for (final Entry<SExceptionContext, Serializable> entry : context.entrySet()) {
                stringBuilder.append(entry.getKey());
                stringBuilder.append("=");
                stringBuilder.append(entry.getValue());
                stringBuilder.append(" | ");
            }
        }
    }

}
