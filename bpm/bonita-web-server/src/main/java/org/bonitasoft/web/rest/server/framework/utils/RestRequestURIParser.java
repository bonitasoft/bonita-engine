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

import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIMalformedUrlException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * Simple parser that extract parameters from a call to the REST api
 *
 * @author Baptiste Mesta
 */
public class RestRequestURIParser {

    //This constant should match the servlet name of DispatcherServlet as declared in servlet context
    public static final String SPRING_REST_SERVLET_NAME = "SpringRest";

    private static final String API_SEGMENT = "API";
    private static final String API_TOOLKIT_SEGMENT = "APIToolkit";
    private static final String API_SPRING_INTERNAL_SEGMENT = AbstractRESTController.API_SPRING_INTERNAL;

    private final HttpServletRequest request;

    public RestRequestURIParser(final HttpServletRequest request) {
        this.request = request;
    }

    public ParsedRestRequestURI parse() {
        if (isSpringMvcServlet() || isToolkitServlet()) {
            return parseSpringMvcOrToolkitRequest();
        }
        if (request.getPathInfo() != null) {
            // Non-Spring, non-toolkit wildcard servlet: parse from servletPath + pathInfo,
            // with special handling to preserve the first path segment as apiName.
            return parseWildcardServletRequest();
        }
        return parseDirectPathRequest();
    }

    private boolean isToolkitServlet() {
        String servletPath = request.getServletPath();
        return ("/" + API_TOOLKIT_SEGMENT).equals(servletPath);
    }

    private boolean isSpringMvcServlet() {
        return SPRING_REST_SERVLET_NAME.equals(request.getHttpServletMapping().getServletName());
    }

    /**
     * For Spring MVC DispatcherServlet (servletName == "SpringRest").
     * Handles both exact-match mappings (e.g. /API/system/maintenance where
     * servletPath = full path, pathInfo = null) and wildcard mappings
     * (e.g. /APISpringInternal/* where servletPath = /APISpringInternal,
     * pathInfo = /bpm/case/42; or /API/bpm/activityVariable/* where
     * servletPath = /API/bpm/activityVariable, pathInfo = /3/myVar).
     * Concatenating servletPath + pathInfo always produces the full request path.
     * <p>
     * For /APIToolkit/* (BonitaRestAPIServlet), we delegate to
     * parseUsingApiSegmentLookup which already recognises APIToolkit
     * via findApiSegmentIndex — same path as Spring MVC and direct-path requests.
     */
    private ParsedRestRequestURI parseSpringMvcOrToolkitRequest() {
        String pathInfo = request.getPathInfo();
        return parseUsingApiSegmentLookup(
                request.getServletPath() + (pathInfo != null ? pathInfo : ""));
    }

    /**
     * Non-Spring nor toolkit wildcard servlets (pathInfo != null, not Spring MVC, not toolkit).
     * <p>
     * Wildcard servlets (/API/avatars/*, /services/*, etc.) parse from
     * index 1 to preserve the first path segment as apiName, matching
     * permission entries like "GET|API/avatars".
     */
    private ParsedRestRequestURI parseWildcardServletRequest() {
        String fullPath = request.getServletPath() + request.getPathInfo();
        String[] path = fullPath.split("/");
        return parseRequest(path, 1);
    }

    /**
     * Exact-match servlets or default servlet (pathInfo is null, not Spring MVC).
     * Exact-match servlets: URLs like /API/documentDownload or /portal/imageUpload
     * that literally match a web.xml url-pattern. The container sets servletPath to
     * the full path and pathInfo to null.
     * Default servlet: URLs like /API/bpm/case/42 that don't match any specific
     * servlet mapping. The container routes these to the default servlet, which also
     * sets servletPath = full path and pathInfo = null. This case arises because
     * RestAPIAuthorizationFilter runs before UrlRewriteFilter in the filter chain
     * (see web.xml filter-mapping order): at auth time, the request still carries
     * its original URL. UrlRewriteFilter will later rewrite and forward to
     * /APIToolkit/* or /APISpringInternal/*, but this parser has already extracted
     * the API name and resource from the original URL.
     */
    private ParsedRestRequestURI parseDirectPathRequest() {
        return parseUsingApiSegmentLookup(request.getServletPath());
    }

    /**
     * Shared logic for Spring MVC, toolkit, and direct-path servlets: build the full path,
     * locate the API segment dynamically, then dispatch to parseRequest.
     */
    private ParsedRestRequestURI parseUsingApiSegmentLookup(String fullPath) {
        String[] path = fullPath.split("/");
        //find the API segment index to support multiple URL patterns (index 0 is always "" as fullPath starts with "/")
        int apiIndex = findApiSegmentIndex(Arrays.asList(path));

        if (apiIndex >= 0) {
            int segmentsAfterApi = path.length - apiIndex - 1;
            if (segmentsAfterApi >= 2) {
                // Deep URL: /API/bpm/case/42 → apiName=bpm, resource=case
                return parseRequest(path, apiIndex + 1);
            }
            if (segmentsAfterApi == 1) {
                // Flat URL: /API/documentDownload → apiName=API, resource=documentDownload
                return parseRequest(path, apiIndex);
            }
            throw new APIMalformedUrlException(request.getRequestURL().toString(),
                    "Missing API or resource name in request URL");
        }
        // No API segment: /portal/imageUpload, /services/something, etc.
        if (path.length < 3) {
            //all the URL we support always at least have 2 path segments and index 0 is ""
            throw new APIMalformedUrlException(request.getRequestURL().toString(),
                    "Missing API path segment in request URL");
        }
        return parseRequest(path, 1);
    }

    /**
     * Find the index of the API prefix segment in the URL path.
     * Handles /API/..., /APIToolkit/..., and /APISpringInternal/... URL patterns.
     */
    private int findApiSegmentIndex(List<String> segments) {
        int index = segments.indexOf(API_SEGMENT);
        if (index < 0) {
            index = segments.indexOf(API_TOOLKIT_SEGMENT);
        }
        if (index < 0) {
            // /API/... URLs rewritten to /APISpringInternal/... by UrlRewriteFilter
            index = segments.indexOf(API_SPRING_INTERNAL_SEGMENT);
        }
        return index;
    }

    protected ParsedRestRequestURI parseRequest(String[] path, int indexOfAPINameSegment) {
        int minimalNumberOfPathSegments = indexOfAPINameSegment + 2;
        if (path.length < minimalNumberOfPathSegments) {
            throw new APIMalformedUrlException(request.getRequestURL().toString(),
                    "Missing API or resource name in request URL");
        }
        String apiName = path[indexOfAPINameSegment];
        String resourceName = path[indexOfAPINameSegment + 1];
        // Read id (if defined)
        APIID resourceQualifiers;
        if (path.length > minimalNumberOfPathSegments) {
            final List<String> pathList = Arrays.asList(path);
            resourceQualifiers = APIID.makeAPIID(pathList.subList(minimalNumberOfPathSegments, pathList.size()));
        } else {
            resourceQualifiers = null;
        }
        return new ParsedRestRequestURI(apiName, resourceName, resourceQualifiers);
    }
}
