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

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.http.JsonExceptionSerializer;
import org.bonitasoft.web.toolkit.client.ui.utils.ListUtils;

/**
 * @author Séverin Moussel
 */
public class APIAttributesException extends APIException {

    private static final long serialVersionUID = 7808353918962735013L;

    private final List<String> attributesNames;

    public APIAttributesException(final List<String> attributesNames) {
        super((Exception) null);
        this.attributesNames = attributesNames;
    }

    public APIAttributesException(final String... attributesNames) {
        super((Exception) null);
        this.attributesNames = Arrays.asList(attributesNames);
    }

    public APIAttributesException(final List<String> attributesNames, final String message, final Throwable cause) {
        super(message, cause);
        this.attributesNames = attributesNames;
    }

    public APIAttributesException(final List<String> attributesNames, final String message) {
        super(message);
        this.attributesNames = attributesNames;
    }

    public APIAttributesException(final List<String> attributesNames, final Throwable cause) {
        super(cause);
        this.attributesNames = attributesNames;
    }

    /**
     * @return the attributesNames
     */
    public List<String> getAttributesNames() {
        return this.attributesNames;
    }

    @Override
    protected JsonExceptionSerializer buildJson() {
        return super.buildJson()
                .appendAttribute("attributesNames", getAttributesNames());
    }

    @Override
    protected String defaultMessage() {
        return "Malformed attributes : " +
                ListUtils.join(this.attributesNames, ", ");
    }

}
