/*
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.api.permission;

import org.bonitasoft.engine.session.APISession;

/**
 *
 * Context of a call made on a REST API
 *
 * @author Baptiste Mesta
 */
public class APICallContext {

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


    private String queryString;

    private String body;

    public APICallContext(String method, String apiName, String resourceName, String resourceId, String queryString, String body) {
        this.method = method;
        this.apiName = apiName;
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.queryString = queryString;
        this.body = body;
    }

    public String getMethod() {
        return method;
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
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        APICallContext that = (APICallContext) o;

        if (apiName != null ? !apiName.equals(that.apiName) : that.apiName != null) return false;
        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null) return false;
        if (resourceId != null ? !resourceId.equals(that.resourceId) : that.resourceId != null) return false;
        if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null) return false;

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
        return result;
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
}
