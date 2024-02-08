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
package org.bonitasoft.web.rest.server.framework.utils;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIMalformedUrlException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * Simple parser that extract parameters from a call to the REST api
 *
 * @author Baptiste Mesta
 */
public class RestRequestParser {

    //This constant should match the servlet name of DispatcherServlet as declared in servlet context
    public static final String SPRING_REST_SERVLET_NAME = "SpringRest";

    private final HttpServletRequest request;
    private String apiName;
    private String resourceName;
    private APIID resourceQualifiers;

    public RestRequestParser(final HttpServletRequest request) {
        this.request = request;
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

    public RestRequestParser invoke() {
        if (SPRING_REST_SERVLET_NAME.equals(request.getHttpServletMapping().getServletName())) {
            return parseSpringMVCAPIRequest();
        } else {
            return parseLegacyAPIRequest();
        }
    }

    protected RestRequestParser parseLegacyAPIRequest() {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.split("/").length < 3) {
            // it's not an URL like API/bpm/...
            pathInfo = request.getServletPath();
        }
        final String[] path = pathInfo.split("/");
        //ignoring the first segment corresponding to the empty string before the first /
        return parseRequest(path, 1);
    }

    protected RestRequestParser parseSpringMVCAPIRequest() {
        String apiPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        if (!StringUtils.isEmpty(pathInfo)) {
            // there is an Id in the URL
            apiPath = apiPath + pathInfo;
        }
        final String[] path = apiPath.split("/");
        //ignoring the 2 first segments corresponding to the /API/ part
        return parseRequest(path, 2);
    }

    protected RestRequestParser parseRequest(String[] path, int indexOfAPINameSegment) {
        int minimalNumberOfPathSegments = indexOfAPINameSegment + 2;
        if (path.length < minimalNumberOfPathSegments) {
            throw new APIMalformedUrlException("Missing API or resource name [" + request.getRequestURL() + "]");
        }
        apiName = path[indexOfAPINameSegment];
        resourceName = path[indexOfAPINameSegment + 1];
        // Read id (if defined)
        if (path.length > minimalNumberOfPathSegments) {
            final List<String> pathList = Arrays.asList(path);
            resourceQualifiers = APIID.makeAPIID(pathList.subList(minimalNumberOfPathSegments, pathList.size()));
        } else {
            resourceQualifiers = null;
        }
        return this;
    }
}
