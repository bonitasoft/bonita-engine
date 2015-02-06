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
package org.bonitasoft.engine.api;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.io.IOUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.thoughtworks.xstream.XStream;

/**
 * @author Celine Souchet
 */
/*
 * ignore the ssl because it causes java.security.NoSuchAlgorithmException: class configured for SSLContext: sun.security.ssl.SSLContextImpl not a SSLContext
 * see http://mathieuhicauber-java.blogspot.fr/2013/07/powermock-and-ssl-context.html
 */
@PowerMockIgnore("javax.net.ssl.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ HTTPServerAPI.class })
public class HTTPServerAPITest {

    private XStream xstream;

    private HTTPServerAPI httpServerAPI;

    @Before
    public void initialize() {
        xstream = new XStream();
        xstream.registerConverter(new BonitaStackTraceElementConverter(), XStream.PRIORITY_VERY_HIGH);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(HTTPServerAPI.SERVER_URL, "localhost:8080");
        map.put(HTTPServerAPI.APPLICATION_NAME, "bonita");
        httpServerAPI = new HTTPServerAPI(map);

    }

    @Test(expected = ServerWrappedException.class)
    public void invokeMethodCatchUndeclaredThrowableException() throws Exception {
        final PrintStream printStream = System.err;
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setErr(new PrintStream(myOut));
        final Logger logger = Logger.getLogger(HTTPServerAPI.class.getName());
        logger.setLevel(Level.FINE);
        final ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        logger.addHandler(ch);

        try {
            final Map<String, Serializable> options = new HashMap<String, Serializable>();
            final String apiInterfaceName = "apiInterfaceName";
            final String methodName = "methodName";
            final List<String> classNameParameters = new ArrayList<String>();
            final Object[] parametersValues = null;

            final HTTPServerAPI httpServerAPI = mock(HTTPServerAPI.class);
            final String response = "response";
            doReturn(response).when(httpServerAPI, "executeHttpPost", eq(options), eq(apiInterfaceName), eq(methodName), eq(classNameParameters),
                    eq(parametersValues), Matchers.any(XStream.class));
            doThrow(new UndeclaredThrowableException(new BonitaException("Bonita exception"), "Exception plop")).when(httpServerAPI, "checkInvokeMethodReturn",
                    eq(response), Matchers.any(XStream.class));

            // Let's call it for real:
            doCallRealMethod().when(httpServerAPI).invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
            httpServerAPI.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
        } finally {
            System.setErr(printStream);
            final String logs = myOut.toString();
            assertTrue("should have written in logs an exception", logs.contains("java.lang.reflect.UndeclaredThrowableException"));
            assertTrue("should have written in logs an exception", logs.contains("BonitaException"));
            assertTrue("should have written in logs an exception", logs.contains("Exception plop"));
        }
    }

    @Test
    public void serializeSimpleParameters() throws Exception {

        HttpEntity entity = httpServerAPI.buildEntity(Collections.<String, Serializable> emptyMap(), Arrays.asList("param1", "param2"), new Object[] {
                "Välue1", Collections.singletonMap("key", "välue") }, xstream);
        String content = IOUtil.read(entity.getContent());
        String decodedContent = URLDecoder.decode(content, "UTF-8");
        assertTrue(decodedContent.contains("välue"));
        assertTrue(decodedContent.contains("Välue1"));

    }

    @Test
    public void serializeByteArrayParameters() throws Exception {
        MultipartEntity entity = (MultipartEntity) httpServerAPI.buildEntity(Collections.<String, Serializable> emptyMap(),
                Arrays.asList(String.class.getName(), "java.util.Map", byte[].class.getName()),
                new Object[] {
                        "Välue1", Collections.singletonMap("key", "välue"), new byte[] {} }, xstream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entity.writeTo(outputStream);
        byte[] content = outputStream.toByteArray();
        String contentAsString = new String(content, Charset.forName("UTF-8"));
        assertTrue(contentAsString.contains("välue"));
        assertTrue(contentAsString.contains("Välue1"));

    }

}
