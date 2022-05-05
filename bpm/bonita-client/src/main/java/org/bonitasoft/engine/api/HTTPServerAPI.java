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
package org.bonitasoft.engine.api;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.bonitasoft.engine.api.impl.XmlConverter;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.digest.DigestUtils;
import org.bonitasoft.engine.exception.StackTraceTransformer;

/**
 * Call the remote engine using HTTP post
 * That class serialize api call parameters in a XML body and post it to the serverAPI servlet like this
 * /serverAPI/[api interface name]/[method name]
 */
public class HTTPServerAPI implements ServerAPI {

    private static final long serialVersionUID = -3375874140999200702L;

    private static final ContentType XML_UTF_8 = ContentType.create("application/xml", UTF_8);

    private static final String CLASS_NAME_PARAMETERS = "classNameParameters";

    private static final String OPTIONS = "options";

    private static final String PARAMETERS_VALUES = "parametersValues";

    private static final String BINARY_PARAMETER = "binaryParameter";

    private static final String BYTE_ARRAY = "==ByteArray==";

    private static final char SLASH = '/';

    private static final String SERVER_API = "/serverAPI/";

    // package-private for testing purpose
    static final String SERVER_URL = "server.url";

    private static final String BASIC_AUTHENTICATION_ACTIVE = "basicAuthentication.active";

    private static final String BASIC_AUTHENTICATION_USERNAME = "basicAuthentication.username";

    private static final String BASIC_AUTHENTICATION_PASSWORD = "basicAuthentication.password";

    // package-private for testing purpose
    static final String APPLICATION_NAME = "application.name";
    static final String CONNECTIONS_MAX = "connections.max";

    private final String serverUrl;

    private final String applicationName;

    private final boolean basicAuthenticationActive;

    private final String basicAuthenticationUserName;

    private final String basicAuthenticationPassword;

    private static HttpClient httpclient;

    private static final ResponseHandler<String> RESPONSE_HANDLER = new BasicResponseHandler();

    private final XmlConverter xmlConverter;

    public HTTPServerAPI(final Map<String, String> parameters) {
        xmlConverter = new XmlConverter();
        // initialize httpclient in the constructor to avoid incompatibility when running tests:
        // java.security.NoSuchAlgorithmException: class configured for SSLContext: sun.security.ssl.SSLContextImpl$TLS10Context not a SSLContext
        if (httpclient == null) {
            httpclient = createHttpClient(parameters);
        }
        serverUrl = parameters.get(SERVER_URL);
        applicationName = parameters.get(APPLICATION_NAME);
        basicAuthenticationActive = "true".equalsIgnoreCase(parameters.get(BASIC_AUTHENTICATION_ACTIVE));
        basicAuthenticationUserName = parameters.get(BASIC_AUTHENTICATION_USERNAME);
        basicAuthenticationPassword = parameters.get(BASIC_AUTHENTICATION_PASSWORD);
    }

    private HttpClient createHttpClient(final Map<String, String> parameters) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        try {
            int connectionsMax = Integer.parseInt(parameters.getOrDefault(CONNECTIONS_MAX, "20"));
            // we have only one route, use same number for connection max and max per route
            builder.setMaxConnPerRoute(connectionsMax);
            builder.setMaxConnTotal(connectionsMax);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Client connection pool size '" + CONNECTIONS_MAX + "' must be set to a number");
        }
        return builder.build();
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName,
            final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException {
        String response = null;
        try {
            response = executeHttpPost(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
            return checkInvokeMethodReturn(response);
        } catch (final UndeclaredThrowableException e) {
            throw new ServerWrappedException(e);
        } catch (final Throwable e) {
            final StackTraceElement[] stackTrace = new Exception().getStackTrace();
            StackTraceTransformer.addStackTo(e, stackTrace);
            throw new ServerWrappedException(e.getMessage() + " / response: " + response, e);
        }
    }

    // package-private for testing purpose
    Object checkInvokeMethodReturn(final String response) throws Throwable {
        Object invokeMethodReturn = null;
        if (response != null && !response.isEmpty() && !"null".equals(response)) {
            invokeMethodReturn = xmlConverter.fromXML(response);
            if (invokeMethodReturn instanceof Throwable) {
                throw (Throwable) invokeMethodReturn;
            }
        }
        return invokeMethodReturn;
    }

    // package-private for testing purpose
    String executeHttpPost(final Map<String, Serializable> options, final String apiInterfaceName,
            final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws IOException {
        final HttpPost httpost = createHttpPost(options, apiInterfaceName, methodName, classNameParameters,
                parametersValues);
        try {
            return httpclient.execute(httpost, RESPONSE_HANDLER);
        } catch (final ClientProtocolException e) {
            String httpCodeMessage = "";
            // required as the http code is not included in the exception message
            if (e instanceof HttpResponseException) {
                final int statusCode = ((HttpResponseException) e).getStatusCode();
                httpCodeMessage = format(" (http code: %s)", statusCode);
            }
            throw new IOException("Error while executing POST request" + httpCodeMessage + " <" + httpost + ">", e);
        }
    }

    private final HttpPost createHttpPost(final Map<String, Serializable> options, final String apiInterfaceName,
            final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws IOException {
        final HttpEntity httpEntity = buildEntity(options, classNameParameters, parametersValues);
        final StringBuilder sBuilder = new StringBuilder(serverUrl);
        sBuilder.append(SLASH).append(applicationName).append(SERVER_API).append(apiInterfaceName).append(SLASH)
                .append(methodName);
        final HttpPost httpPost = new HttpPost(sBuilder.toString());
        httpPost.setEntity(httpEntity);

        // Basic authentication
        if (basicAuthenticationActive) {
            final StringBuilder credentials = new StringBuilder();
            credentials.append(basicAuthenticationUserName).append(":").append(basicAuthenticationPassword);
            final String encodedCredentials = DigestUtils
                    .encodeBase64AsUtf8String(credentials.toString().getBytes(UTF_8));
            httpPost.setHeader("Authorization", "Basic " + encodedCredentials);
        }

        return httpPost;
    }

    // package-private for testing purpose
    final HttpEntity buildEntity(final Map<String, Serializable> options, final List<String> classNameParameters,
            final Object[] parametersValues) throws IOException {
        final HttpEntity httpEntity;
        // if we have a business archive we use multipart to have the business archive attached as a binary content (it can be big)
        if (classNameParameters.contains(BusinessArchive.class.getName())
                || classNameParameters.contains(byte[].class.getName())) {
            final List<Object> bytearrayParameters = new ArrayList<>();

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setBoundary(null).setCharset(UTF_8);
            entityBuilder.addPart(OPTIONS, new StringBody(xmlConverter.toXML(options), XML_UTF_8));
            entityBuilder.addPart(CLASS_NAME_PARAMETERS,
                    new StringBody(xmlConverter.toXML(classNameParameters), XML_UTF_8));
            for (int i = 0; i < parametersValues.length; i++) {
                final Object parameterValue = parametersValues[i];
                if (parameterValue instanceof BusinessArchive || parameterValue instanceof byte[]) {
                    parametersValues[i] = BYTE_ARRAY;
                    bytearrayParameters.add(parameterValue);
                }
            }
            entityBuilder.addPart(PARAMETERS_VALUES, new StringBody(xmlConverter.toXML(parametersValues), XML_UTF_8));
            int i = 0;
            for (final Object object : bytearrayParameters) {
                entityBuilder.addPart(BINARY_PARAMETER + i, new ByteArrayBody(serialize(object), BINARY_PARAMETER + i));
                i++;
            }
            httpEntity = entityBuilder.build();
        } else {
            final List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair(OPTIONS, xmlConverter.toXML(options)));
            nvps.add(new BasicNameValuePair(CLASS_NAME_PARAMETERS, xmlConverter.toXML(classNameParameters)));
            nvps.add(new BasicNameValuePair(PARAMETERS_VALUES, xmlConverter.toXML(parametersValues)));
            httpEntity = new UrlEncodedFormEntity(nvps, UTF_8.name());
        }
        return httpEntity;
    }

    private static byte[] serialize(final Object obj) throws IOException {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

}
