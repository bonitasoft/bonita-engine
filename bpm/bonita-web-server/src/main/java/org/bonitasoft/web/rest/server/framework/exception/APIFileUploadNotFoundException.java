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

import org.bonitasoft.web.toolkit.client.common.exception.http.JsonExceptionSerializer;

/**
 * @author Séverin Moussel
 */
public class APIFileUploadNotFoundException extends APIAttributeException {

    private static final long serialVersionUID = -5738651518696086103L;

    private final String filePath;

    public APIFileUploadNotFoundException(final String attributeName, final String filePath) {
        super(attributeName);
        this.filePath = filePath;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return this.filePath;
    }

    @Override
    protected String defaultMessage() {
        return "Uploaded file " + getAttributeName() + "(" + getFilePath() + ") not found for API \"" + getApi() + "#"
                + getResource() + "\"";
    }

    @Override
    protected JsonExceptionSerializer buildJson() {
        return super.buildJson()
                .appendAttribute("filepath", getFilePath());
    }

}
