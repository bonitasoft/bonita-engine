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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class APICallContextTest {

    @Test
    public void getBodyAsJSon() throws JSONException {
        APICallContext apiCallContext = new APICallContext();
        apiCallContext.setBody("{\"a\":\"b\",\"c\":\"1\"}");

        JSONObject body = apiCallContext.getBodyAsJSON();

        assertThat(body.getString("a")).isEqualTo("b");
        assertThat(body.getLong("c")).isEqualTo(1l);
    }

    @Test
    public void getFilters() throws JSONException {
        APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3d104&d=processId");

        Map<String, String> filters = apiCallContext.getFilters();

        assertThat(filters).containsOnly(entry("user_id", "104"), entry("state", "ready"));
    }

    @Test
    public void getFilters_with_corrupt_filters() throws JSONException {
        APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3d&d=processId");

        Map<String, String> filters = apiCallContext.getFilters();

        assertThat(filters).containsOnly(entry("state", "ready"));
    }

    @Test
    public void getSearchTerm() throws JSONException {
        APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3d104&d=processId&s=toto");

        String searchTerm = apiCallContext.getSearchTerm();

        assertThat(searchTerm).isEqualTo("toto");
    }

    @Test
    public void getSearchTerm_with_corrupt_earch_term() throws JSONException {
        APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3d104&d=processId&s=");

        String searchTerm = apiCallContext.getSearchTerm();

        assertThat(searchTerm).isEqualTo(null);
    }

    @Test
    public void getSearchTerm_with_corrupt_query() throws JSONException {
        APICallContext apiCallContext = new APICallContext();
        apiCallContext.setQueryString("p=0&c=10&o=priority%20DESC&f=state%3dready&f=user_id%3d104&d=processId&s");

        String searchTerm = apiCallContext.getSearchTerm();

        assertThat(searchTerm).isEqualTo(null);
    }

    @Test
    public void construct_with_null_body_and_query() throws JSONException {
        APICallContext apiCallContext = new APICallContext("GET","identity","user","1");

        String searchTerm = apiCallContext.getSearchTerm();
        Map<String, String> filters = apiCallContext.getFilters();
        String body = apiCallContext.getBody();

        assertThat(searchTerm).isEqualTo(null);
        assertThat(filters).isEmpty();
        assertThat(body).isEqualTo(null);
    }

}
