/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.thoughtworks.xstream.XStream;

/**
 * @author Celine Souchet
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ HTTPServerAPI.class })
public class HTTPServerAPITest {

    @Mock
    private Map<String, String> parameters;

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public final void invokeMethod() throws Exception {
        // TODO : Not yet implemented
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
                    eq(parametersValues), Mockito.any(XStream.class));
            doThrow(new UndeclaredThrowableException(new BonitaException("Bonita exception"), "Exception plop")).when(httpServerAPI, "checkInvokeMethodReturn",
                    eq(response), Mockito.any(XStream.class));

            // Let's call it for real:
            doCallRealMethod().when(httpServerAPI).invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
            httpServerAPI.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
        } finally {
            System.setErr(printStream);
            final String logs = myOut.toString();
            System.out.println(logs);
            assertTrue("should have written in logs an exception", logs.contains("java.lang.reflect.UndeclaredThrowableException"));
            assertTrue("should have written in logs an exception", logs.contains("BonitaException"));
            assertTrue("should have written in logs an exception", logs.contains("Exception plop"));
        }
    }

    @Test
    public final void serialize() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void deserialize() throws Exception {
        // TODO : Not yet implemented
    }

}
