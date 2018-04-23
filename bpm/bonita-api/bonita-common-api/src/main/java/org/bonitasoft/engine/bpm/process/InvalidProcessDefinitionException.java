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
package org.bonitasoft.engine.bpm.process;

import java.util.List;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * <p>Thrown when the process definition is invalid.</p>
 * <p>The class InvalidProcessDefinitionException is a form of Throwable that indicates conditions that a reasonable application might want to catch.<br>
 * The class InvalidProcessDefinitionException that is not also subclasses of {@link RuntimeException} are checked exceptions.<br>
 * Checked exceptions need to be declared in a method or constructor's {@literal throws} clause if they can be thrown by the execution of the method or
 * constructor and propagate outside the method or constructor boundary.</p>
 *
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 */
public class InvalidProcessDefinitionException extends BonitaException {

    private static final String INVALID_MESSAGE = "The design of your process is invalid";

    private static final long serialVersionUID = -6871937645264318098L;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public InvalidProcessDefinitionException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param errors
     *        The list of errors included in the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public InvalidProcessDefinitionException(final List<String> errors) {
        super(getErrorMessage(errors));
    }

    private static String getErrorMessage(final List<String> errors) {
        final StringBuilder stringBuilder = new StringBuilder(INVALID_MESSAGE);
        stringBuilder.append('\n');
        stringBuilder.append("Errors: ");
        for (final String error : errors) {
            stringBuilder.append(error);
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

}
