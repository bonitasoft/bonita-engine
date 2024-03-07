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

/**
 * @author Séverin Moussel
 */
public class APIFilterMandatoryException extends APIFilterException {

    private static final long serialVersionUID = 7067237932975183746L;

    public APIFilterMandatoryException(final String filterName, final String message, final Throwable cause) {
        super(filterName, message, cause);
    }

    public APIFilterMandatoryException(final String filterName, final String message) {
        super(filterName, message);
    }

    public APIFilterMandatoryException(final String filterName, final Throwable cause) {
        super(filterName, cause);
    }

    public APIFilterMandatoryException(final String filterName) {
        super(filterName);
    }

    @Override
    protected String defaultMessage() {
        return "Filter " + getFilterName() + " is mandatory";
    }

}
