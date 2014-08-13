/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.document.exception;

/**
 * @author Celine Souchet
 */
public class SDocumentDeletionException extends SDocumentException {

    private static final long serialVersionUID = 5303323058515016243L;

    /**
     * @param message
     */
    public SDocumentDeletionException(final String message) {
        super(message);
    }

    /**
     * @param message
     * @param t
     */
    public SDocumentDeletionException(final String message, final Throwable t) {
        super(message, t);
    }

    /**
     * @param t
     */
    public SDocumentDeletionException(final Throwable t) {
        super(t);
    }

}
