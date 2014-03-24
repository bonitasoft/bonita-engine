/**
 * Copyright (C) 2013-2014 BonitaSoft S.A.
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

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class SBonitaException extends Exception {

    private static final long serialVersionUID = -500856379312027085L;

    private final String exceptionId;

    private final Object[] arguments;

    private final Map<SContext, Serializable> context;

    /**
     * Default constructor
     */
    public SBonitaException() {
        this((Object[]) null);
    }

    /**
     * @param arguments
     */
    public SBonitaException(final Object... arguments) {
        super();
        exceptionId = this.getClass().getName();
        this.arguments = arguments;
        context = new TreeMap<SContext, Serializable>();
    }

    /**
     * @param message
     */
    public SBonitaException(final String message) {
        super(message);
        exceptionId = this.getClass().getName();
        arguments = null;
        context = new TreeMap<SContext, Serializable>();
    }

    /**
     * @param message
     * @param cause
     */
    public SBonitaException(final String message, final Throwable cause) {
        super(message, cause);
        exceptionId = this.getClass().getName();
        arguments = null;
        context = new TreeMap<SContext, Serializable>();
    }

    /**
     * @param cause
     */
    public SBonitaException(final Throwable cause) {
        this(cause, (Object[]) null);
    }

    /**
     * @param cause
     * @param arguments
     */
    public SBonitaException(final Throwable cause, final Object... arguments) {
        super(cause);
        exceptionId = this.getClass().getName();
        this.arguments = arguments;
        context = new TreeMap<SContext, Serializable>();
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
    public Map<SContext, Serializable> getContext() {
        return context;
    }

    /**
     * @param id
     *            The identifier of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionIdOnContext(final Long id) {
        context.put(SContext.PROCESS_DEFINITION_ID, id);
    }

    /**
     * @param name
     *            The name of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionNameOnContext(final String name) {
        context.put(SContext.PROCESS_NAME, name);
    }

    /**
     * @param version
     *            The version of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionVersionOnContext(final String version) {
        context.put(SContext.PROCESS_VERSION, version);
    }

    /**
     * @param id
     *            The identifier of the process instance to set
     * @since 6.3
     */
    public void setProcessInstanceIdOnContext(final Long id) {
        context.put(SContext.PROCESS_INSTANCE_ID, id);
    }

    /**
     * @param id
     *            The identifier of the root process instance to set
     * @since 6.3
     */
    public void setRootProcessInstanceIdOnContext(final Long id) {
        context.put(SContext.ROOT_PROCESS_INSTANCE_ID, id);
    }

    /**
     * @param id
     *            The identifier of the connector definition
     * @since 6.3
     */
    public void setConnectorDefinitionIdOnContext(final String id) {
        context.put(SContext.CONNECTOR_DEFINITION_ID, id);
    }

    /**
     * @param name
     *            The class name of the implementation of the connector definition to set
     * @since 6.3
     */
    public void setConnectorDefinitionImplementationClassNameOnContext(final String name) {
        context.put(SContext.CONNECTOR_DEFINITION_IMPLEMENTATION_CLASS_NAME, name);
    }

    /**
     * @param version
     *            The version of the connector definition
     * @since 6.3
     */
    public void setConnectorDefinitionVersionOnContext(final String version) {
        context.put(SContext.CONNECTOR_DEFINITION_VERSION, version);
    }

    /**
     * @param activationEvent
     *            The event which activates the connector to set
     * @since 6.3
     */
    public void setConnectorActivationEventOnContext(final String activationEvent) {
        context.put(SContext.CONNECTOR_ACTIVATION_EVENT, activationEvent);
    }

    /**
     * @param id
     *            The identifier of the connector instance to set
     * @since 6.3
     */
    public void setConnectorInstanceIdOnContext(final long id) {
        context.put(SContext.CONNECTOR_INSTANCE_ID, id);
    }

    /**
     * @param id
     *            The identifier of the flow node definition to set
     * @since 6.3
     */
    public void setFlowNodeDefinitionIdOnContext(final long id) {
        context.put(SContext.FLOW_NODE_DEFINITION_ID, id);
    }

    /**
     * @param id
     *            The identifier of the flow node instance to set
     * @since 6.3
     */
    public void setFlowNodeInstanceIdOnContext(final long id) {
        context.put(SContext.FLOW_NODE_INSTANCE_ID, id);
    }

    /**
     * @param name
     *            The name of the flow node to set
     * @since 6.3
     */
    public void setFlowNodeNameOnContext(final String name) {
        context.put(SContext.FLOW_NODE_NAME, name);
    }

    /**
     * @param name
     *            The name of the message instance to set
     * @since 6.3
     */
    public void setMessageInstanceNameOnContext(final String name) {
        context.put(SContext.MESSAGE_INSTANCE_NAME, name);
    }

    /**
     * @param name
     *            The target process name of the message instance to set
     * @since 6.3
     */
    public void setMessageInstanceTargetProcessOnContext(final String name) {
        context.put(SContext.MESSAGE_INSTANCE_TARGET_PROCESS_NAME, name);
    }

    /**
     * @param name
     *            The target flow node name of the message instance to set
     * @since 6.3
     */
    public void setMessageInstanceTargetFlowNodeOnContext(final String name) {
        context.put(SContext.MESSAGE_INSTANCE_TARGET_FLOW_NODE_NAME, name);
    }

    /**
     * @param eventType
     *            The event type of the waiting message instance to set
     * @since 6.3
     */
    public void setWaitingMessageEventTypeOnContext(final String eventType) {
        context.put(SContext.WAITING_MESSAGE_INSTANCE_TYPE, eventType);
    }

    /**
     * @param userId
     *            The identifier of the user
     * @since 6.3
     */
    public void setUserIdOnContext(final long userId) {
        context.put(SContext.USER_ID, userId);
    }

    /**
     * @param threadId
     *            The thread id to set
     * @since 6.3
     */
    public void setThreadId(final long threadId) {
        context.put(SContext.THREAD_ID, threadId);
    }

    /**
     * @param hostname
     *            The hostname to set
     * @since 6.3
     */
    public void setHostname(final String hostname) {
        context.put(SContext.HOSTNAME, hostname);
    }

    /**
     * @param tenantId
     *            The tenant id to set
     * @since 6.3
     */
    public void setTenantID(final long tenantId) {
        context.put(SContext.TENANT_ID, tenantId);
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
        if (message != null && !message.trim().equals("")) {
            stringBuilder.append(message);
        }
    }

    private void appendContextMessage(final StringBuilder stringBuilder) {
        if (!context.isEmpty()) {
            for (final Entry<SContext, Serializable> entry : context.entrySet()) {
                stringBuilder.append(entry.getKey() + "=" + entry.getValue() + " | ");
            }
        }
    }

}
