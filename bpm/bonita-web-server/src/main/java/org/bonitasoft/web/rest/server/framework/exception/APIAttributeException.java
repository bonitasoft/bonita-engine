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
public class APIAttributeException extends APIException {

    private static final long serialVersionUID = -4611825491187153300L;

    private final String attributeName;

    public APIAttributeException(final String attributeName) {
        super((Exception) null);
        this.attributeName = attributeName;
    }

    public APIAttributeException(final String attributeName, final String message) {
        super(message);
        this.attributeName = attributeName;
    }

    /**
     * @return the attributeName
     */
    public String getAttributeName() {
        return this.attributeName;
    }

    @Override
    protected JsonExceptionSerializer buildJson() {
        return super.buildJson()
                .appendAttribute("attributeName", getAttributeName());
    }

    @Override
    protected String defaultMessage() {
        return "Malformed attribute : " + this.attributeName;
    }

}
