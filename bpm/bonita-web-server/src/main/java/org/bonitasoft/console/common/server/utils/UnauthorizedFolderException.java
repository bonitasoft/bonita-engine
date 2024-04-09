/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.console.common.server.utils;

import java.io.IOException;

/**
 * Technical exception thrown when we try to read/write a file out of the authorized file system path.
 *
 * @author Julien Mege
 */
public class UnauthorizedFolderException extends IOException {

    private static final long serialVersionUID = 1071342750973031637L;

    public UnauthorizedFolderException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public UnauthorizedFolderException(final String message) {
        super(message);
    }

}
