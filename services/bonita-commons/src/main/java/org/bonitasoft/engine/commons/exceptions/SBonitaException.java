/**
 * Copyright (C) 2011, 2014 Bonitasoft S.A.
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
     * @param processDefinitionId
     *            The identifier of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionId(final Long processDefinitionId) {
        context.put(SContext.PROCESS_DEFINITION_ID, processDefinitionId);
    }

    /**
     * @param processDefinitionName
     *            The name of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionName(final String processDefinitionName) {
        context.put(SContext.PROCESS_NAME, processDefinitionName);
    }

    /**
     * @param processDefinitionVersion
     *            The version of the process definition to set
     * @since 6.3
     */
    public void setProcessDefinitionVersion(final String processDefinitionVersion) {
        context.put(SContext.PROCESS_VERSION, processDefinitionVersion);
    }

    /**
     * @param processInstanceId
     *            The identifier of the process instance to set
     * @since 6.3
     */
    public void setProcessInstanceId(final Long processInstanceId) {
        context.put(SContext.PROCESS_INSTANCE_ID, processInstanceId);
    }

    /**
     * @param rootProcessInstanceId
     *            The identifier of the root process instance to set
     * @since 6.3
     */
    public void setRootProcessInstanceId(final Long rootProcessInstanceId) {
        context.put(SContext.ROOT_PROCESS_INSTANCE_ID, rootProcessInstanceId);
    }

    @Override
    public String getMessage() {
        final StringBuilder stringBuilder = new StringBuilder();

        if (!context.isEmpty()) {
            for (final Entry<SContext, Serializable> entry : context.entrySet()) {
                stringBuilder.append(entry.getKey() + " = " + entry.getValue() + " | ");
            }
        }

        String message = super.getMessage();
        if (message != null && message.isEmpty() && getCause() != null) {
            message = getCause().getMessage();
        }
        if (message != null && !message.trim().equals("")) {
            stringBuilder.append(message);
        }
        return stringBuilder.toString();
    }

}
