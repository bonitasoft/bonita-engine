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
package org.bonitasoft.web.toolkit.client.common.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class JSonSerializerTest {

    @Test
    public void should_serialize_an_exception() throws Exception {
        Exception exception = new Exception("an exception");

        String serialize = JSonSerializer.serialize(exception);

        assertThat(serialize).isEqualTo("{\"exception\":\"class java.lang.Exception\",\"message\":\"an exception\"}");
    }

    @Test
    public void should_serialize_only_first_cause_of_an_exception() throws Exception {
        Exception exception = new Exception("first one", new Exception("second one", new Exception("third one")));

        String serialize = JSonSerializer.serialize(exception);

        assertThat(serialize).isEqualTo(
                "{\"exception\":\"class java.lang.Exception\","
                        + "\"message\":\"first one\","
                        + "\"cause\":{"
                        + "\"exception\":\"class java.lang.Exception\","
                        + "\"message\":\"second one\""
                        + "}"
                        + "}");
    }
}
