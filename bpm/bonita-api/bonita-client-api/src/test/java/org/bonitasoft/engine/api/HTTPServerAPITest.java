/**
 * Copyright (C) 2015-2018 BonitaSoft S.A.
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
package org.bonitasoft.engine.api;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.io.IOUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Celine Souchet
 */
public class HTTPServerAPITest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private HTTPServerAPI httpServerAPI;

    @Before
    public void initialize() {
        HashMap<String, String> map = new HashMap<>();
        map.put(HTTPServerAPI.SERVER_URL, "localhost:8080");
        map.put(HTTPServerAPI.APPLICATION_NAME, "bonita");
        httpServerAPI = new HTTPServerAPI(map);
    }

    @Test
    public void should_invoke_method_catch_and_wrap_UndeclaredThrowableException() throws Throwable {
        //given:
        final Map<String, Serializable> options = new HashMap<>();
        final String apiInterfaceName = "apiInterfaceName";
        final String methodName = "methodName";
        final List<String> classNameParameters = new ArrayList<>();
        final Object[] parametersValues = null;

        final HTTPServerAPI httpServerAPI = mock(HTTPServerAPI.class);
        final String response = "response";
        doReturn(response).when(httpServerAPI).executeHttpPost(eq(options), eq(apiInterfaceName), eq(methodName),
                eq(classNameParameters),
                eq(parametersValues));
        doThrow(new UndeclaredThrowableException(new BonitaException("Bonita exception"), "Exception plop"))
                .when(httpServerAPI).checkInvokeMethodReturn(eq(response));

        // Let's call it for real:
        doCallRealMethod().when(httpServerAPI).invokeMethod(options, apiInterfaceName, methodName, classNameParameters,
                parametersValues);

        //when:
        Throwable thrown = catchThrowable(
                () -> httpServerAPI.invokeMethod(options, apiInterfaceName, methodName, classNameParameters,
                        parametersValues));

        //then:
        assertThat(thrown).as("Thrown exception").isInstanceOf(ServerWrappedException.class)
                .hasMessageContaining("java.lang.reflect.UndeclaredThrowableException: Exception plop")
                .hasRootCauseInstanceOf(BonitaException.class);
    }

    @Test
    public void should_build_entity_serialize_simple_parameters() throws Exception {
        HttpEntity entity = httpServerAPI.buildEntity(emptyMap(),
                asList("param1", "param2"),
                new Object[] {"Välue1", singletonMap("key", "välue") });
        String content = IOUtil.read(entity.getContent());
        String decodedContent = URLDecoder.decode(content, "UTF-8");
        assertThat(decodedContent).as("Decoded content").contains("välue", "Välue1");
    }

    @Test
    public void should_build_entity_serialize_byte_array_parameters() throws Exception {
        HttpEntity entity = httpServerAPI.buildEntity(emptyMap(),
                asList(String.class.getName(), Map.class.getName(), byte[].class.getName()),
                new Object[] {"Välue36", singletonMap("key", "välue"), new byte[] {} }
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entity.writeTo(outputStream);
        byte[] content = outputStream.toByteArray();
        String contentAsString = new String(content, Charset.forName("UTF-8"));
        assertThat(contentAsString).as("Content").contains("välue", "Välue36");
    }

}
