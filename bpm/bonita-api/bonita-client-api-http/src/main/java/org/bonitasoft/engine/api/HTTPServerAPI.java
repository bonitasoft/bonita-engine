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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.StackTraceTransformer;
import org.bonitasoft.engine.http.BonitaResponseHandler;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

/**
 * @author Baptiste Mesta
 * @author Julien Mege
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class HTTPServerAPI implements ServerAPI {

    private static final long serialVersionUID = -3375874140999200702L;

    private static final String UTF_8 = "UTF-8";

    private static final String CLASS_NAME_PARAMETERS = "classNameParameters";

    private static final String OPTIONS = "options";

    private static final String PARAMETERS_VALUES = "parametersValues";

    private static final String BINARY_PARAMETER = "binaryParameter";

    private static final String BYTE_ARRAY = "==ByteArray==";

    private static final char SLASH = '/';

    private static final String SERVER_API = "/serverAPI/";

    static final String SERVER_URL = "server.url";

    private static final String BASIC_AUTHENTICATION_ACTIVE = "basicAuthentication.active";

    private static final String BASIC_AUTHENTICATION_USERNAME = "basicAuthentication.username";

    private static final String BASIC_AUTHENTICATION_PASSWORD = "basicAuthentication.password";

    static final String APPLICATION_NAME = "application.name";

    private static final Logger LOGGER = Logger.getLogger(HTTPServerAPI.class.getName());

    private String serverUrl = null;

    private String applicationName = null;

    private boolean basicAuthenticationActive = false;

    private String basicAuthenticationUserName = null;

    private String basicAuthenticationPassword = null;

    private static DefaultHttpClient httpclient;

    private static final XStream XSTREAM;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final ResponseHandler<String> RESPONSE_HANDLER = new BonitaResponseHandler();

    static {
        XSTREAM = new XStream();
        XSTREAM.registerConverter(new BonitaStackTraceElementConverter(), XStream.PRIORITY_VERY_HIGH);
    }

    public HTTPServerAPI(final Map<String, String> parameters) {
        // initialize httpclient in the constructor to avoid incompatibility when running tests:
        // java.security.NoSuchAlgorithmException: class configured for SSLContext: sun.security.ssl.SSLContextImpl$TLS10Context not a SSLContext
        if (httpclient == null) {
            httpclient = new DefaultHttpClient(new PoolingClientConnectionManager());
        }
        serverUrl = parameters.get(SERVER_URL);
        applicationName = parameters.get(APPLICATION_NAME);
        basicAuthenticationActive = "true".equalsIgnoreCase(parameters.get(BASIC_AUTHENTICATION_ACTIVE));
        basicAuthenticationUserName = parameters.get(BASIC_AUTHENTICATION_USERNAME);
        basicAuthenticationPassword = parameters.get(BASIC_AUTHENTICATION_PASSWORD);
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException {
        String response = null;
        try {
            response = executeHttpPost(options, apiInterfaceName, methodName, classNameParameters, parametersValues, XSTREAM);
            return checkInvokeMethodReturn(response, XSTREAM);
        } catch (final UndeclaredThrowableException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, e.getMessage(), e);
            }
            throw new ServerWrappedException(e);
        } catch (final Throwable e) {
            final StackTraceElement[] stackTrace = new Exception().getStackTrace();
            StackTraceTransformer.addStackTo(e, stackTrace);
            throw new ServerWrappedException(e.getMessage() + "response= " + response, e);
        }
    }

    private Object checkInvokeMethodReturn(final String response, final XStream xstream) throws Throwable {
        Object invokeMethodReturn = null;
        if (response != null && !response.isEmpty() && !"null".equals(response)) {
            invokeMethodReturn = fromXML(response, xstream);
            if (invokeMethodReturn instanceof Throwable) {
                throw (Throwable) invokeMethodReturn;
            }
        }
        return invokeMethodReturn;
    }

    private String executeHttpPost(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues, final XStream xstream) throws UnsupportedEncodingException, IOException,
            ClientProtocolException {
        final HttpPost httpost = createHttpPost(options, apiInterfaceName, methodName, classNameParameters, parametersValues, xstream);
        try {
            return httpclient.execute(httpost, RESPONSE_HANDLER);
        } catch (final ClientProtocolException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.getMessage() + System.getProperty("line.separator") + "httpost = <" + httpost + ">");
            }
            throw e;
        }
    }

    private final HttpPost createHttpPost(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues, final XStream xstream) throws UnsupportedEncodingException, IOException {
        final HttpEntity httpEntity = buildEntity(options, classNameParameters, parametersValues, xstream);
        final StringBuilder sBuilder = new StringBuilder(serverUrl);
        sBuilder.append(SLASH).append(applicationName).append(SERVER_API).append(apiInterfaceName).append(SLASH).append(methodName);
        final HttpPost httpost = new HttpPost(sBuilder.toString());
        httpost.setEntity(httpEntity);

        // Basic authentication
        if (basicAuthenticationActive) {
            final StringBuilder credentials = new StringBuilder();
            credentials.append(basicAuthenticationUserName).append(":").append(basicAuthenticationPassword);
            final Base64 encoder = new Base64();
            final String encodedCredentials = encoder.encodeAsString(credentials.toString().getBytes());
            httpost.setHeader("Authorization", "Basic " + encodedCredentials);
        }

        return httpost;
    }

    final HttpEntity buildEntity(final Map<String, Serializable> options, final List<String> classNameParameters, final Object[] parametersValues,
            final XStream xstream) throws UnsupportedEncodingException, IOException {
        final HttpEntity httpEntity;
        /*
         * if we have a business archive we use multipart to have the business archive attached as a binary content (it can be big)
         */
        if (classNameParameters.contains(BusinessArchive.class.getName()) || classNameParameters.contains(byte[].class.getName())) {
            final List<Object> bytearrayParameters = new ArrayList<Object>();
            final MultipartEntity entity = new MultipartEntity(null, null, UTF8);
            entity.addPart(OPTIONS, new StringBody(toXML(options, xstream), UTF8));
            entity.addPart(CLASS_NAME_PARAMETERS, new StringBody(toXML(classNameParameters, xstream), UTF8));
            for (int i = 0; i < parametersValues.length; i++) {
                final Object parameterValue = parametersValues[i];
                if (parameterValue instanceof BusinessArchive || parameterValue instanceof byte[]) {
                    parametersValues[i] = BYTE_ARRAY;
                    bytearrayParameters.add(parameterValue);
                }
            }
            entity.addPart(PARAMETERS_VALUES, new StringBody(toXML(parametersValues, xstream), UTF8));
            int i = 0;
            for (final Object object : bytearrayParameters) {
                entity.addPart(BINARY_PARAMETER + i, new ByteArrayBody(serialize(object), BINARY_PARAMETER + i));
                i++;
            }
            httpEntity = entity;
        } else {
            final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair(OPTIONS, toXML(options, xstream)));
            nvps.add(new BasicNameValuePair(CLASS_NAME_PARAMETERS, toXML(classNameParameters, xstream)));
            nvps.add(new BasicNameValuePair(PARAMETERS_VALUES, toXML(parametersValues, xstream)));
            httpEntity = new UrlEncodedFormEntity(nvps, UTF_8);
        }
        return httpEntity;
    }

    public byte[] serialize(final Object obj) throws IOException {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    public Object deserialize(final byte[] bytes) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        final ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }

    private String toXML(final Object options, final XStream xstream) throws IOException {
        final StringWriter stringWriter = new StringWriter();
        final ObjectOutputStream out = xstream.createObjectOutputStream(stringWriter);
        out.writeObject(options);
        out.close();
        return stringWriter.toString();
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
            } catch (final CannotResolveClassException e) {
                throw new BonitaRuntimeException("unable to deserialize object " + object, e);
            } finally {
                in.close();
                xmlReader.close();
            }
        } catch (final IOException e) {
            throw new BonitaRuntimeException("unable to deserialize object " + object, e);
        }
    }

}
