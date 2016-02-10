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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class APICallContextTest {


    @Test
    public void getFilters() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3d104&d=processId");

        final Map<String, String> filters = apiCallContext.getFilters();

        assertThat(filters).containsOnly(entry("user_id", "104"), entry("state", "ready"));
    }

    @Test
    public void getFilters_with_UpperCase_in_separator() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3D104&d=processId");

        final Map<String, String> filters = apiCallContext.getFilters();

        assertThat(filters).containsOnly(entry("user_id", "104"), entry("state", "ready"));
    }

    @Test
    public void getCompoundResourceId() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setResourceId("1/2/3");

        final List<String> compoundResourceId = apiCallContext.getCompoundResourceId();

        assertThat(compoundResourceId).containsExactly("1", "2", "3");
    }

    @Test
    public void getCompoundResourceId_when_single_id() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setResourceId("1");

        final List<String> compoundResourceId = apiCallContext.getCompoundResourceId();

        assertThat(compoundResourceId).containsExactly("1");
    }

    @Test
    public void getCompoundResourceId_when_no_resource_id() {
        final APICallContext apiCallContext = new APICallContext();

        final List<String> compoundResourceId = apiCallContext.getCompoundResourceId();

        assertThat(compoundResourceId).isEmpty();
    }

    @Test
    public void getFilters_with_corrupt_filters() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3d&d=processId");

        final Map<String, String> filters = apiCallContext.getFilters();

        assertThat(filters).containsOnly(entry("state", "ready"));
    }

    @Test
    public void getSearchTerm() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3d104&d=processId&s=toto");

        final String searchTerm = apiCallContext.getSearchTerm();

        assertThat(searchTerm).isEqualTo("toto");
    }

    @Test
    public void getSearchTerm_with_corrupt_earch_term() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3d104&d=processId&s=");

        final String searchTerm = apiCallContext.getSearchTerm();

        assertThat(searchTerm).isEqualTo(null);
    }

    @Test
    public void getSearchTerm_with_corrupt_query() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3d104&d=processId&s");

        final String searchTerm = apiCallContext.getSearchTerm();

        assertThat(searchTerm).isEqualTo(null);
    }

    @Test
    public void construct_with_null_body_and_query() {
        final APICallContext apiCallContext = new APICallContext("GET", "identity", "user", "1");

        final String searchTerm = apiCallContext.getSearchTerm();
        final Map<String, String> filters = apiCallContext.getFilters();
        final String body = apiCallContext.getBody();

        assertThat(searchTerm).isEqualTo(null);
        assertThat(filters).isEmpty();
        assertThat(body).isEqualTo(null);
    }

    @Test
    public void should_isGETMethod_return_true() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setMethod("GET");

        final boolean getMethod = apiCallContext.isGET();

        assertThat(getMethod).isTrue();
    }

    @Test
    public void should_isGETMethod_return_false() {
        final APICallContext apiCallContext = new APICallContext();

        final boolean getMethod = apiCallContext.isGET();

        assertThat(getMethod).isFalse();
    }

    @Test
    public void should_isPOSTMethod_return_true() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setMethod("POST");

        final boolean getMethod = apiCallContext.isPOST();

        assertThat(getMethod).isTrue();
    }

    @Test
    public void should_isPOSTMethod_return_false() {
        final APICallContext apiCallContext = new APICallContext();

        final boolean getMethod = apiCallContext.isPOST();

        assertThat(getMethod).isFalse();
    }

    @Test
    public void should_isPUTMethod_return_true() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setMethod("PUT");

        final boolean getMethod = apiCallContext.isPUT();

        assertThat(getMethod).isTrue();
    }

    @Test
    public void should_isPUTMethod_return_false() {
        final APICallContext apiCallContext = new APICallContext();

        final boolean getMethod = apiCallContext.isPUT();

        assertThat(getMethod).isFalse();
    }

    @Test
    public void should_isDELETEMethod_return_true() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setMethod("DELETE");

        final boolean getMethod = apiCallContext.isDELETE();

        assertThat(getMethod).isTrue();
    }

    @Test
    public void should_isDELETEMethod_return_false() {
        final APICallContext apiCallContext = new APICallContext();

        final boolean getMethod = apiCallContext.isDELETE();

        assertThat(getMethod).isFalse();
    }

    @Test
    public void should_equals_works() {
        final APICallContext apiCallContext = new APICallContext();
        apiCallContext.setMethod("GET");
        apiCallContext.setApiName("apiName");
        apiCallContext.setResourceName("myResource");
        apiCallContext.setResourceId("125");

        assertThat(apiCallContext).isEqualTo(new APICallContext("GET", "apiName", "myResource", "125"));
        assertThat(apiCallContext.hashCode()).isEqualTo(new APICallContext("GET", "apiName", "myResource", "125").hashCode());
        assertThat(apiCallContext.toString()).isEqualTo(new APICallContext("GET", "apiName", "myResource", "125").toString());
    }

}
