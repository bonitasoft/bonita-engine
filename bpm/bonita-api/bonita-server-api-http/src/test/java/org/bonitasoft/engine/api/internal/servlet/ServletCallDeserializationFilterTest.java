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
package org.bonitasoft.engine.api.internal.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ServletCallDeserializationFilterTest {

    private ServletCall servletCall;

    @Before
    public void setUp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        servletCall = new HttpAPIServletCall(request, response);
    }

    @Test
    public void should_deserialize_byte_array() throws Exception {
        byte[] original = new byte[] { 1, 2, 3, 4, 5 };
        byte[] serialized = serializeObject(original);

        Object result = servletCall.deserialize(serialized);

        assertThat(result).isInstanceOf(byte[].class);
        assertThat((byte[]) result).isEqualTo(original);
    }

    @Test
    public void should_deserialize_java_util_types() throws Exception {
        HashMap<String, String> original = new HashMap<>();
        original.put("key", "value");
        byte[] serialized = serializeObject(original);

        Object result = servletCall.deserialize(serialized);

        assertThat(result).isInstanceOf(HashMap.class);
        assertThat(((HashMap<?, ?>) result).get("key")).isEqualTo("value");
    }

    @Test
    public void should_deserialize_bonita_engine_types() throws Exception {
        org.bonitasoft.engine.exception.CreationException original = new org.bonitasoft.engine.exception.CreationException(
                "test error");
        byte[] serialized = serializeObject(original);

        Object result = servletCall.deserialize(serialized);

        assertThat(result).isInstanceOf(org.bonitasoft.engine.exception.CreationException.class);
        assertThat(((Exception) result).getMessage()).isEqualTo("test error");
    }

    @Test
    public void should_reject_java_net_URL() throws Exception {
        HashMap<String, Object> malicious = new HashMap<>();
        malicious.put("url", new URL("http://example.com"));
        byte[] serialized = serializeObject(malicious);

        assertThatThrownBy(() -> servletCall.deserialize(serialized))
                .isInstanceOf(InvalidClassException.class)
                .hasMessageContaining("REJECTED");
    }

    @Test
    public void should_reject_java_net_InetAddress() throws Exception {
        java.net.InetAddress malicious = java.net.InetAddress.getByName("127.0.0.1");
        byte[] serialized = serializeObject(malicious);

        assertThatThrownBy(() -> servletCall.deserialize(serialized))
                .isInstanceOf(InvalidClassException.class)
                .hasMessageContaining("REJECTED");
    }

    private static byte[] serializeObject(Object obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        }
        return bos.toByteArray();
    }
}
