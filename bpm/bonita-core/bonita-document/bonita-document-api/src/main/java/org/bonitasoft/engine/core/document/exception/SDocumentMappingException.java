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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class SDocumentMappingException extends SBonitaException {

    private static final long serialVersionUID = -7279379575300389907L;

    public SDocumentMappingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SDocumentMappingException(final String message) {
        super(message);
    }

    public SDocumentMappingException(final Throwable cause) {
        super(cause);
    }

    public SDocumentMappingException(final long archivedDocumentId, final Throwable cause) {
        super("Unable to archive the document with id = <" + archivedDocumentId + ">", cause);
    }

}
