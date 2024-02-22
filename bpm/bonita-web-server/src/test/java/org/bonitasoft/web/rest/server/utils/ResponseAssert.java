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
package org.bonitasoft.web.rest.server.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Objects;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.Status;

public class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {

    protected ResponseAssert(Response actual) {
        super(actual, ResponseAssert.class);
    }

    public static ResponseAssert assertThat(Response actual) {
        return new ResponseAssert(actual);
    }

    public ResponseAssert hasJsonEntityEqualTo(String json) {
        info.description("Response entity is not matching. WARNING This might introduce an API break");
        Objects.instance().assertEqual(info, getJson(actual.getEntityAsText()), getJson(json));
        return this;
    }

    private JsonNode getJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public ResponseAssert hasStatus(Status status) {
        Objects.instance().assertEqual(info, actual.getStatus(), status);
        return this;
    }

    public ResponseAssert hasHeader(Header header) {
        Objects.instance().assertIsIn(info, header, actual.getHeaders());
        return this;
    }
}
