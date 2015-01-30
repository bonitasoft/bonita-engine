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

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SBonitaRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 7268935639620676043L;

    private final Map<SExceptionContext, Serializable> context;

    public SBonitaRuntimeException(final Throwable cause) {
        super(cause);
        context = new TreeMap<SExceptionContext, Serializable>();
    }

    public SBonitaRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
        context = new TreeMap<SExceptionContext, Serializable>();
    }

    public SBonitaRuntimeException(final String message) {
        super(message);
        context = new TreeMap<SExceptionContext, Serializable>();
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
     *            The identifier of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionIdOnContext(final Long id) {
        context.put(SExceptionContext.PROCESS_DEFINITION_ID, id);
    }

    /**
     * @param name
     *            The name of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionNameOnContext(final String name) {
        context.put(SExceptionContext.PROCESS_NAME, name);
    }

    /**
     * @param version
     *            The version of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionVersionOnContext(final String version) {
        context.put(SExceptionContext.PROCESS_VERSION, version);
    }

    /**
     * @param id
     *            The identifier of the process instance to set
     * @since 6.3
     */
    public void setProcessInstanceIdOnContext(final Long id) {
        context.put(SExceptionContext.PROCESS_INSTANCE_ID, id);
    }

    /**
     * @param id
     *            The identifier of the root process instance to set
     * @since 6.3
     */
    public void setRootProcessInstanceIdOnContext(final Long id) {
        context.put(SExceptionContext.ROOT_PROCESS_INSTANCE_ID, id);
    }

    /**
     * @param id
     *            The identifier of the connector definition
     * @since 6.3
     */
    public void setConnectorDefinitionIdOnContext(final String id) {
        context.put(SExceptionContext.CONNECTOR_DEFINITION_ID, id);
    }

    /**
     * @param name
     *            The class name of the implementation of the connector definition to set
     * @since 6.3
     */
    public void setConnectorDefinitionImplementationClassNameOnContext(final String name) {
        context.put(SExceptionContext.CONNECTOR_DEFINITION_IMPLEMENTATION_CLASS_NAME, name);
    }

    /**
     * @param version
     *            The version of the connector definition
     * @since 6.3
     */
    public void setConnectorDefinitionVersionOnContext(final String version) {
        context.put(SExceptionContext.CONNECTOR_DEFINITION_VERSION, version);
    }

    /**
     * @param activationEvent
     *            The event which activates the connector to set
     * @since 6.3
     */
    public void setConnectorActivationEventOnContext(final String activationEvent) {
        context.put(SExceptionContext.CONNECTOR_ACTIVATION_EVENT, activationEvent);
    }

    /**
     * @param id
     *            The identifier of the connector instance to set
     * @since 6.3
     */
    public void setConnectorInstanceIdOnContext(final long id) {
        context.put(SExceptionContext.CONNECTOR_INSTANCE_ID, id);
    }

    /**
     * @param id
     *            The identifier of the flow node definition to set
     * @since 6.3
     */
    public void setFlowNodeDefinitionIdOnContext(final long id) {
        context.put(SExceptionContext.FLOW_NODE_DEFINITION_ID, id);
    }

    /**
     * @param id
     *            The identifier of the flow node instance to set
     * @since 6.3
     */
    public void setFlowNodeInstanceIdOnContext(final long id) {
        context.put(SExceptionContext.FLOW_NODE_INSTANCE_ID, id);
    }

    /**
     * @param name
     *            The name of the flow node to set
     * @since 6.3
     */
    public void setFlowNodeNameOnContext(final String name) {
        context.put(SExceptionContext.FLOW_NODE_NAME, name);
    }

    /**
     * @param name
     *            The name of the message instance to set
     * @since 6.3
     */
    public void setMessageInstanceNameOnContext(final String name) {
        context.put(SExceptionContext.MESSAGE_INSTANCE_NAME, name);
    }

    /**
     * @param name
     *            The target process name of the message instance to set
     * @since 6.3
     */
    public void setMessageInstanceTargetProcessOnContext(final String name) {
        context.put(SExceptionContext.MESSAGE_INSTANCE_TARGET_PROCESS_NAME, name);
    }

    /**
     * @param name
     *            The target flow node name of the message instance to set
     * @since 6.3
     */
    public void setMessageInstanceTargetFlowNodeOnContext(final String name) {
        context.put(SExceptionContext.MESSAGE_INSTANCE_TARGET_FLOW_NODE_NAME, name);
    }

    /**
     * @param eventType
     *            The event type of the waiting message instance to set
     * @since 6.3
     */
    public void setWaitingMessageEventTypeOnContext(final String eventType) {
        context.put(SExceptionContext.WAITING_MESSAGE_INSTANCE_TYPE, eventType);
    }

    /**
     * @param userId
     *            The identifier of the user
     * @since 6.3
     */
    public void setUserIdOnContext(final long userId) {
        context.put(SExceptionContext.USER_ID, userId);
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
            for (final Entry<SExceptionContext, Serializable> entry : context.entrySet()) {
                stringBuilder.append(entry.getKey() + "=" + entry.getValue() + " | ");
            }
        }
    }

}
