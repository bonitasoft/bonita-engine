/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
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
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class SDocumentMappingNotFoundException extends SDocumentMappingException {

    private static final long serialVersionUID = 5143299844735860984L;

    public SDocumentMappingNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SDocumentMappingNotFoundException(final Throwable cause) {
        super(cause);
    }

    public SDocumentMappingNotFoundException(final String message) {
        super(message);
    }

    public SDocumentMappingNotFoundException(final long archivedDocumentId) {
        super("Can't find the archived document with id = <" + archivedDocumentId + ">");
    }

    public SDocumentMappingNotFoundException(final long archivedDocumentId, final Throwable cause) {
        super("Can't find the archived document with id = <" + archivedDocumentId + ">", cause);
    }

}
