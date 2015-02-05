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
package org.bonitasoft.engine.api.internal.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.bonitasoft.engine.api.impl.ServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.StackTraceTransformer;

import com.thoughtworks.xstream.XStream;

/**
 * @author Julien Mege
 * @author Matthieu Chaffotte
 */
public class HttpAPIServletCall extends ServletCall {

    private static final String SLASH = "/";

    private static final String ARRAY = "[]";

    private static final String NULL = "null";

    private static final String BYTE_ARRAY = "==ByteArray==";

    private static final String CLASS_NAME_PARAMETERS = "classNameParameters";

    private static final String PARAMETERS_VALUES = "parametersValues";

    private static final String OPTIONS = "options";

    private static final XStream XSTREAM = new XStream();

    public HttpAPIServletCall(final HttpServletRequest request, final HttpServletResponse response) throws FileUploadException, IOException {
        super(request, response);
    }

    @Override
    public void doGet() {
        error("GET method forbidden", HttpServletResponse.SC_FORBIDDEN);

    }

    @Override
    public void doPost() {
        try {
            String apiInterfaceName = null;
            String methodName = null;
            final String[] pathParams = getRequestURL().split(SLASH);
            if (pathParams != null && pathParams.length >= 2) {
                apiInterfaceName = pathParams[pathParams.length - 2];
                methodName = pathParams[pathParams.length - 1];
            }
            final String options = this.getParameter(OPTIONS);
            final String parametersValues = this.getParameter(PARAMETERS_VALUES);
            final String parametersClasses = this.getParameter(CLASS_NAME_PARAMETERS);

            Map<String, Serializable> myOptions = new HashMap<String, Serializable>();
            if (options != null && !options.isEmpty()) {
                myOptions = fromXML(options, XSTREAM);
            }
            List<String> myClassNameParameters = new ArrayList<String>();
            if (parametersClasses != null && !parametersClasses.isEmpty() && !parametersClasses.equals(ARRAY)) {
                myClassNameParameters = fromXML(parametersClasses, XSTREAM);
            }
            Object[] myParametersValues = new Object[0];
            if (parametersValues != null && !parametersValues.isEmpty() && !parametersValues.equals(NULL)) {
                myParametersValues = fromXML(parametersValues, XSTREAM);
                if (myParametersValues != null && !(myParametersValues.length == 0)) {
                    final Iterator<byte[]> binaryParameters = getBinaryParameters().iterator();
                    for (int i = 0; i < myParametersValues.length; i++) {
                        final Object parameter = myParametersValues[i];
                        if (BYTE_ARRAY.equals(parameter)) {
                            myParametersValues[i] = deserialize(binaryParameters.next());
                        }
                    }
                }
            }

            final ServerAPI serverAPI = ServerAPIFactory.getServerAPI();

            final Object invokeMethod;
            try {
                invokeMethod = serverAPI.invokeMethod(myOptions, apiInterfaceName, methodName, myClassNameParameters, myParametersValues);
            } catch (ServerWrappedException e) {
                // merge stack trace of the server exception
                throw StackTraceTransformer.mergeStackTraces(e);
            }

            String invokeMethodSerialized = null;
            if (invokeMethod != null) {
                invokeMethodSerialized = toXML(invokeMethod, XSTREAM);
            }

            // add charset avoid encoding problems
            this.output(invokeMethodSerialized);
        } catch (final Exception e) {
            error(toResponse(e), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T fromXML(final String object, final XStream xstream) {
        final StringReader xmlReader = new StringReader(object);
        ObjectInputStream in = null;

        try {
            in = xstream.createObjectInputStream(xmlReader);
            try {
                return (T) in.readObject();
            } catch (final IOException e) {
                throw new BonitaRuntimeException("unable to deserialize object " + object, e);
            } catch (final ClassNotFoundException e) {
                throw new BonitaRuntimeException("unable to deserialize object " + object, e);
            } finally {
                in.close();
                xmlReader.close();
            }
        } catch (final IOException e) {
            throw new BonitaRuntimeException("unable to deserialize object " + object, e);
        }
    }

    @Override
    public void doPut() {
        error("PUT method forbidden", HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    public void doDelete() {
        error("DELETE method forbidden", HttpServletResponse.SC_FORBIDDEN);
    }

    private String toResponse(final Exception exception) {
        Throwable result = null;
        if (exception instanceof ServerWrappedException) {
            result = exception.getCause();
        } else {
            result = exception;
        }
        // ignore fields suppressedExceptions and stackTrance causing exceptions in some cases
        XSTREAM.omitField(Throwable.class, "suppressedExceptions");
        // xstream.omitField(Throwable.class, "stackTrace");
        return toXML(result, XSTREAM);
    }

    private String toXML(final Object object, final XStream xstream) {
        final StringWriter stringWriter = new StringWriter();
        ObjectOutputStream out;
        try {
            out = xstream.createObjectOutputStream(stringWriter);
            try {
                out.writeObject(object);
            } catch (final IOException e) {
                throw new BonitaRuntimeException("unable to serialize object " + object.toString(), e);
            } finally {
                stringWriter.close();
                out.close();
            }
        } catch (final IOException e1) {
            throw new BonitaRuntimeException("unable to serialize object " + object.toString(), e1);
        }
        return stringWriter.toString();
    }

}
