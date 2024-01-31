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
package org.bonitasoft.web.toolkit.client.common.exception.api;

import org.bonitasoft.web.toolkit.client.common.i18n.T_;

/**
 * @author Séverin Moussel
 */
public class APINotFoundException extends APIException {

    private static final long serialVersionUID = 7709846894799079466L;

    public APINotFoundException(final Throwable cause) {
        super(cause);
    }

    public APINotFoundException(final String api, final String resource) {
        super((Exception) null);
        setApi(api);
        setResource(resource);
    }

    public APINotFoundException(final T_ localizedMessage, final Throwable cause) {
        super(localizedMessage, cause);
    }

    public APINotFoundException(final T_ localizedMessage) {
        super(localizedMessage);
    }

    @Override
    protected String defaultMessage() {
        return "API " +
                getApi() +
                " or resource " +
                getResource() +
                " doesn't exist or is not declared";
    }

}
