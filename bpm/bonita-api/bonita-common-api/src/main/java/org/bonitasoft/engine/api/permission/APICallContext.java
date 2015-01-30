/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.permission;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context of a call made on a REST API
 *
 * @author Baptiste Mesta
 */
public class APICallContext implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String FILTER_KEY = "f";
    public static final String SEARCH_TERM_KEY = "s";
    public static final String DELIMITER = "&";

    /*
     * The http method
     */
    private String method;

    /*
     * the api name
     */
    private String apiName;

    /*
     * the resource name
     */
    private String resourceName;

    /*
     * id of the resource, can be multiple
     */
    private String resourceId;

    /*
     * query string of the api call
     */
    private String queryString;

    /*
     * body of the api call
     */
    private String body;
    private Map<String, String> filters = new HashMap<String, String>();
    private String searchTerm;

    /**
     * @param method
     *        the HTTP method
     * @param apiName
     *        the name of the api
     * @param resourceName
     *        the name of the resource
     * @param resourceId
     *        the id (or multiple id) of the resource if specified
     */
    public APICallContext(String method, String apiName, String resourceName, String resourceId) {
        this(method, apiName, resourceName, resourceId, null, null);
    }

    /**
     * @param method
     *        the HTTP method
     * @param apiName
     *        the name of the api
     * @param resourceName
     *        the name of the resource
     * @param resourceId
     *        the id (or multiple id) of the resource if specified
     * @param queryString
     *        the query string of the api context if specified
     * @param body
     *        the body string of the api context if specified
     */
    public APICallContext(String method, String apiName, String resourceName, String resourceId, String queryString, String body) {
        this.method = method;
        this.apiName = apiName;
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.queryString = queryString;
        parseQueryString(queryString);
        this.body = body;
    }

    private void parseQueryString(String queryString) {
        this.filters = new HashMap<String, String>();
        if (queryString == null) {
            return;
        }
        for (String element : queryString.split(DELIMITER)) {
            int indexOfEquals = element.indexOf("=");
            if (indexOfEquals > 0 && indexOfEquals + 1 < element.length()) {
                String key = element.substring(0, indexOfEquals);
                String value = element.substring(indexOfEquals + 1, element.length());
                if (FILTER_KEY.equals(key)) {
                    addFilter(value);
                } else if (SEARCH_TERM_KEY.equals(key)) {
                    searchTerm = value;
                }
            }
        }
    }

    private void addFilter(String value) {
        int indexOfEncodedEquals = Math.max(value.indexOf("%3d"), value.indexOf("%3D"));
        if (indexOfEncodedEquals > 0 && indexOfEncodedEquals + 3 < value.length()) {
            filters.put(value.substring(0, indexOfEncodedEquals), value.substring(indexOfEncodedEquals + 3, value.length()));
        }
    }

    /**
     * empty constructor
     */
    public APICallContext() {
    }

    public String getMethod() {
        return method;
    }

    /**
     * @return true if method is GET
     */
    public boolean isGET() {
        return "GET".equals(method);
    }

    /**
     * @return true if method is PUT
     */
    public boolean isPUT() {
        return "PUT".equals(method);
    }

    /**
     * @return true if method is POST
     */
    public boolean isPOST() {
        return "POST".equals(method);
    }

    /**
     * @return true if method is DELETE
     */
    public boolean isDELETE() {
        return "DELETE".equals(method);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public List<String> getCompoundResourceId() {
        return resourceId == null ? Collections.<String> emptyList() : Arrays.asList(resourceId.split("/"));
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
        parseQueryString(queryString);
    }

    public String getBody() {
        return body;
    }


    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    @Override
    public String toString() {
        return "APICallContext{" +
                "method='" + method + '\'' +
                ", apiName='" + apiName + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", queryString='" + queryString + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        APICallContext that = (APICallContext) o;

        if (apiName != null ? !apiName.equals(that.apiName) : that.apiName != null)
            return false;
        if (body != null ? !body.equals(that.body) : that.body != null)
            return false;
        if (filters != null ? !filters.equals(that.filters) : that.filters != null)
            return false;
        if (method != null ? !method.equals(that.method) : that.method != null)
            return false;
        if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null)
            return false;
        if (resourceId != null ? !resourceId.equals(that.resourceId) : that.resourceId != null)
            return false;
        if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null)
            return false;
        if (searchTerm != null ? !searchTerm.equals(that.searchTerm) : that.searchTerm != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (apiName != null ? apiName.hashCode() : 0);
        result = 31 * result + (resourceName != null ? resourceName.hashCode() : 0);
        result = 31 * result + (resourceId != null ? resourceId.hashCode() : 0);
        result = 31 * result + (queryString != null ? queryString.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (filters != null ? filters.hashCode() : 0);
        result = 31 * result + (searchTerm != null ? searchTerm.hashCode() : 0);
        return result;
    }
}
