/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.framework.utils;

import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * Immutable result of parsing a REST API request URI.
 */
public class ParsedRestRequestURI {

    private final String apiName;
    private final String resourceName;
    private final APIID resourceQualifiers;

    public ParsedRestRequestURI(String apiName, String resourceName, APIID resourceQualifiers) {
        this.apiName = apiName;
        this.resourceName = resourceName;
        this.resourceQualifiers = resourceQualifiers;
    }

    public String getApiName() {
        return apiName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public APIID getResourceQualifiers() {
        return resourceQualifiers;
    }
}
