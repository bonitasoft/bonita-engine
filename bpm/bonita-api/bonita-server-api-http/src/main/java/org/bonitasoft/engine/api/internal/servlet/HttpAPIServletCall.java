/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thoughtworks.xstream.security.ForbiddenClassException;
import org.apache.commons.fileupload.FileUploadException;
import org.bonitasoft.engine.api.impl.ServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.api.internal.servlet.impl.XmlConverter;
import org.bonitasoft.engine.exception.StackTraceTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien Mege
 * @author Matthieu Chaffotte
 */
public class HttpAPIServletCall extends ServletCall {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpAPIServletCall.class);

    // JEP 290 filter rejection message — part of JDK internal format since JDK 9
    // (see java.io.ObjectInputStream); may need updating if a future JDK changes this wording.
    private static final String JEP290_FILTER_REJECTED_MSG = "filter status: REJECTED";

    private static final String SLASH = "/";

    private static final String ARRAY = "[]";

    private static final String NULL = "null";

    private static final String BYTE_ARRAY = "==ByteArray==";

    private static final String CLASS_NAME_PARAMETERS = "classNameParameters";

    private static final String PARAMETERS_VALUES = "parametersValues";

    private static final String OPTIONS = "options";

    private XmlConverter xmlConverter;

    public HttpAPIServletCall(final HttpServletRequest request, final HttpServletResponse response)
            throws FileUploadException, IOException {
        super(request, response);
        xmlConverter = new XmlConverter();
    }

    @Override
    protected String getResponseContentType() {
        return "application/xml;charset=UTF-8";
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

            Map<String, Serializable> myOptions = new HashMap<>();
            if (isNotBlank(options)) {
                myOptions = xmlConverter.fromXML(options);
            }
            List<String> myClassNameParameters = new ArrayList<>();
            if (parametersClasses != null && !parametersClasses.isEmpty() && !parametersClasses.equals(ARRAY)) {
                myClassNameParameters = xmlConverter.fromXML(parametersClasses);
            }
            Object[] myParametersValues = new Object[0];
            if (parametersValues != null && !parametersValues.isEmpty() && !parametersValues.equals(NULL)) {
                myParametersValues = xmlConverter.fromXML(parametersValues);
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

            final Object invokeMethod;
            try {
                invokeMethod = getServerAPI().invokeMethod(myOptions, apiInterfaceName, methodName,
                        myClassNameParameters, myParametersValues);
            } catch (ServerWrappedException e) {
                // merge stack trace of the server exception
                throw StackTraceTransformer.mergeStackTraces(e);
            }

            String invokeMethodSerialized = null;
            if (invokeMethod != null) {
                invokeMethodSerialized = xmlConverter.toXML(invokeMethod);
            }

            // add charset avoid encoding problems
            output(invokeMethodSerialized);
        } catch (final Exception e) {
            error(toResponse(e), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // Visible for testing
    ServerAPI getServerAPI() {
        return ServerAPIFactory.getServerAPI();
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
        Throwable result = exception;
        if (exception instanceof ServerWrappedException) {
            result = exception.getCause();
        }
        if (isDeserializationSecurityException(result)) {
            LOGGER.warn("Blocked suspected deserialization attack on {}", getRequestURL(), result);
            // Serialize a plain String rather than a BonitaRuntimeException to avoid
            // XStream's ThrowableConverter hitting Java module access restrictions
            // when walking exception class hierarchies (e.g. IOException.serialVersionUID).
            return xmlConverter.toXML("Invalid request");
        }
        return xmlConverter.toXML(result);
    }

    private boolean isDeserializationSecurityException(Throwable t) {
        // Check message to distinguish JEP 290 filter rejections from normal
        // InvalidClassException causes (e.g. serialVersionUID mismatch)
        return Stream.iterate(t, Objects::nonNull, Throwable::getCause)
                .anyMatch(cause -> cause instanceof ForbiddenClassException
                        || (cause instanceof InvalidClassException
                                && cause.getMessage() != null
                                && cause.getMessage().contains(JEP290_FILTER_REJECTED_MSG)));
    }

}
