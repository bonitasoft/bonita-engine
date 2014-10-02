/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.bpm.document;

import org.bonitasoft.engine.exception.ExecutionException;

/**
 * Thrown when it's not possible to attach a document to a process.
 * 
 * The class DocumentAttachmentException is a form of Throwable that indicates conditions that a reasonable application might want to catch.
 * The class DocumentAttachmentException that is not also subclasses of {@link RuntimeException} are checked exceptions.
 * Checked exceptions need to be declared in a method or constructor's {@literal throws} clause if they can be thrown by the execution of the method or
 * constructor and propagate outside the method or constructor boundary.
 * 
 * @author Nicolas Chabanoles
 * @author Celine Souchet
 */
public class DocumentAttachmentException extends ExecutionException {

    private static final long serialVersionUID = -3376881143165731702L;

    /**
     * Constructs a new exception with the specified detail cause.
     * 
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *            cause is nonexistent or unknown.)
     */
    public DocumentAttachmentException(final Throwable cause) {
        super(cause);
    }

    public DocumentAttachmentException(String s) {
        super(s);
    }
}
