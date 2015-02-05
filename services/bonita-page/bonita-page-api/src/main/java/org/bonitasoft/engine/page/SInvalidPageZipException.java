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
package org.bonitasoft.engine.page;

import java.io.IOException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * 
 * content of the page is not valid
 * 
 * @author Baptiste Mesta
 * 
 */
public abstract class SInvalidPageZipException extends SBonitaException {

    private static final long serialVersionUID = -7263291210428082852L;

    public SInvalidPageZipException(final String message) {
        super(message);
    }

    public SInvalidPageZipException(final String string, final IOException e) {
        super(string, e);
    }

}
