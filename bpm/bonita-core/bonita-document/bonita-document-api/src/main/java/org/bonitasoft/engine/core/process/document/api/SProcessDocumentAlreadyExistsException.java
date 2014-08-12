/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.document.api;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Emmanuel Duchastenier
 */
public class SProcessDocumentAlreadyExistsException extends SProcessDocumentException {

    private static final long serialVersionUID = -5246925639589489933L;

    public SProcessDocumentAlreadyExistsException(final Throwable e) {
        super(e);
    }

    /**
     * @param message
     * @param e
     */
    public SProcessDocumentAlreadyExistsException(final String message, final SBonitaException e) {
        super(message, e);
    }

}
