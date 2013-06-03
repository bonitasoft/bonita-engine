/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class SBonitaException extends Exception {

    private static final long serialVersionUID = -500856379312027085L;

    private final String exceptionId;

    private final Object[] arguments;

    public SBonitaException() {
        super();
        exceptionId = this.getClass().getName();
        arguments = null;
    }

    public SBonitaException(final String message) {
        super(message);
        exceptionId = this.getClass().getName();
        arguments = null;
    }

    public SBonitaException(final String message, final Throwable cause) {
        super(message, cause);
        exceptionId = this.getClass().getName();
        arguments = null;
    }

    public SBonitaException(final Object... arguments) {
        super();
        exceptionId = this.getClass().getName();
        this.arguments = arguments;
    }

    public SBonitaException(final Throwable cause, final Object... arguments) {
        super(cause);
        exceptionId = this.getClass().getName();
        this.arguments = arguments;
    }

    public SBonitaException(final Throwable cause) {
        super(cause);
        exceptionId = this.getClass().getName();
        arguments = null;
    }

    /**
     * This exception id is used to find potential causes
     * 
     * @return the Id of the exception
     */
    public String getExceptionId() {
        return exceptionId;
    }

    public Object[] getParameters() {
        return arguments;
    }

    /**
     * Override this method to define a default message.<br />
     * Default message will be the getMessage() result if no other message is defined.
     * 
     * @return This method returns the default message of the exception.
     */
    protected String getDefaultMessage() {
        return "";
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (message != null && message.isEmpty() && getCause() != null) {
            message = getCause().getMessage();
        }
        if (message != null && message.isEmpty()) {
            message = getDefaultMessage();
        }
        return message;
    }

}
