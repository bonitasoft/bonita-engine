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
package org.bonitasoft.web.rest.server.framework.exception;

import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.http.JsonExceptionSerializer;

/**
 * @author Séverin Moussel
 */
public class APIFilterException extends APIException {

    private static final long serialVersionUID = -3674362051034713685L;

    private final String filterName;

    public APIFilterException(final String filterName) {
        super();
        this.filterName = filterName;
    }

    public APIFilterException(final String filterName, final String message, final Throwable cause) {
        super(message, cause);
        this.filterName = filterName;
    }

    public APIFilterException(final String filterName, final String message) {
        super(message);
        this.filterName = filterName;
    }

    public APIFilterException(final String filterName, final Throwable cause) {
        super(cause);
        this.filterName = filterName;
    }

    /**
     * @return the filterName
     */
    public String getFilterName() {
        return this.filterName;
    }

    @Override
    protected JsonExceptionSerializer buildJson() {
        return super.buildJson()
                .appendAttribute("filterName", getFilterName());
    }

    @Override
    protected String defaultMessage() {
        return "Error on filter : " + getFilterName();
    }

}
