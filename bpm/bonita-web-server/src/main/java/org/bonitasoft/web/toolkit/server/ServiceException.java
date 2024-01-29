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
package org.bonitasoft.web.toolkit.server;

import org.bonitasoft.web.toolkit.client.common.exception.http.JsonExceptionSerializer;
import org.bonitasoft.web.toolkit.client.common.exception.http.ServerException;

/**
 * @author Séverin Moussel
 */
public class ServiceException extends ServerException {

    private static final long serialVersionUID = 4014993729649254349L;

    protected final String path;

    public ServiceException(final String path) {
        super();
        this.path = path;
    }

    public ServiceException(final String path, final String message, final Throwable cause) {
        super(message, cause);
        this.path = path;
    }

    public ServiceException(final String path, final String message) {
        super(message);
        this.path = path;
    }

    public ServiceException(final String path, final Throwable cause) {
        super(cause);
        this.path = path;
    }

    /**
     * Get the path of the service called
     *
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    @Override
    protected String defaultMessage() {
        return "The service \"" + getPath() + "\" has encountered an unknown error";
    }

    @Override
    protected JsonExceptionSerializer buildJson() {
        return super.buildJson()
                .appendAttribute("path", getPath());
    }

}
