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
package org.bonitasoft.web.toolkit.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.utils.LocaleUtils;
import org.bonitasoft.web.toolkit.client.common.exception.http.ServerException;
import org.bonitasoft.web.toolkit.client.common.json.JSonSerializer;

/**
 * @author Séverin Moussel
 * @author Baptiste Mesta
 * @author Fabio Lombardi
 */
public abstract class ServletCall {

    private String inputStream = null;

    /**
     * The parameters of the URL.<br />
     * Result of the parsing of the query string : "?a=b&c=d&..."
     */
    protected final Map<String, String[]> parameters = new HashMap<>();

    /**
     * The request made to access this servletCall.
     */
    private final HttpServletRequest request;

    /**
     * The response to return.
     */
    private final HttpServletResponse response;

    /**
     * Default constructor.
     *
     * @param request
     *        The request made to access this servletCall.
     * @param response
     *        The response to return.
     */
    public ServletCall(final HttpServletRequest request, final HttpServletResponse response) {
        super();
        this.request = request;
        this.response = response;

        parseRequest(request, response);
    }

    /**
     * Constructor for tests
     */
    public ServletCall() {
        request = null;
        response = null;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PARAMETERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the current call's HttpSession
     *
     * @return This method returns the session from the current call.
     */
    public HttpSession getHttpSession() {
        return request.getSession();
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        return request.getQueryString();
    }

    /**
     * Reconstruct the URL the client used to make the request.
     * The returned URL contains a protocol, server name, port
     * number, and server path, but it does not include query
     * string parameters.
     *
     * @return This method returns the reconstructed URL
     */
    public String getRequestURL() {
        return request.getRequestURL().toString();
    }

    /**
     * Read the input stream and set it in a String
     */
    public String getInputStream() {
        if (inputStream == null) {

            BufferedReader reader = null;
            try {

                // BS-8474 - use custom reader instead of request reader to avoid JBoss5.1 bug
                // see https://issues.jboss.org/browse/JBAS-7817
                final ServletInputStream stream = request.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

                final StringBuilder sb = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = reader.readLine();
                }

                inputStream = sb.toString();

            } catch (final IOException e) {
                throw new RuntimeException("Can't read input Stream.", e);
            } finally {
                closeQuietly(reader);
            }
        }

        return inputStream;
    }

    private void closeQuietly(final BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException e) {
                // do nothing / close quietly
            }
        }
    }

    /**
     * Count the number of parameters passed in the URL
     *
     * @return This method returns the number of parameters in the URL
     */
    public int countParameters() {
        return parameters.size();
    }

    /**
     * Get a parameter values by its name
     *
     * @param name
     *        The name of the parameter (case sensitive)
     * @return This method returns the values of a parameter as a list of String or null if the parameter isn't defined
     */
    public List<String> getParameterAsList(final String name) {
        return getParameterAsList(name, null);
    }

    /**
     * Get a parameter values by its name
     *
     * @param name
     *        The name of the parameter (case sensitive)
     * @param defaultValue
     *        The value to return if the parameter isn't define
     * @return This method returns the values of a parameter as a list of String
     */
    public List<String> getParameterAsList(final String name, final String defaultValue) {
        if (parameters.containsKey(name)) {
            return Arrays.asList(parameters.get(name));
        }
        if (defaultValue != null) {
            final List<String> results = new ArrayList<>();
            results.add(defaultValue);
            return results;
        }
        return null;
    }

    /**
     * Get a parameter first value by its name
     *
     * @param name
     *        The name of the parameter (case sensitive)
     * @return This method returns the first value of a parameter as a String or null if the parameter isn't define
     */
    public String getParameter(final String name) {
        return getParameter(name, null);
    }

    /**
     * Get a parameter first value by its name
     *
     * @param name
     *        The name of the parameter (case sensitive)
     * @param defaultValue
     *        The value to return if the parameter isn't define
     * @return This method returns the first value of a parameter as a String
     */
    public String getParameter(final String name, final String defaultValue) {
        if (parameters.containsKey(name)) {
            final String[] result = parameters.get(name);
            if (result.length > 0) {
                return result[0];
            }
        }
        return defaultValue;
    }

    /**
     * Get all the parameters
     *
     * @return This method returns all the parameters as a Map of array of String
     */
    public Map<String, String[]> getParameters() {
        return parameters;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GENERATE RESPONSES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Write into the output header.
     *
     * @param name
     *        The name of the header to write.
     * @param value
     *        The value of the header to write.
     */
    protected void head(final String name, final String value) {
        response.addHeader(name, value);
    }

    /**
     * Output a file
     *
     * @param file
     *        The file to output
     */
    protected void output(final File file) {
        try (InputStream stream = new FileInputStream(file)) {
            output(stream);
        } catch (final FileNotFoundException e) {
            throw new ServerException(e);
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }

    /**
     * Output a stream as a file
     *
     * @param stream
     *        The stream to output
     * @param filename
     *        The name of the file to retrieve with the stream.
     */
    protected void output(final InputStream stream, final String filename) {
        response.addHeader("Content-Disposition", "attachment; filename=" + filename + ";");
        output(stream);
    }

    /**
     * Output a stream as a file
     *
     * @param stream
     *        The stream to output
     */
    protected void output(final InputStream stream) {
        response.setContentType("application/octet-stream");
        try {
            IOUtils.copy(stream, response.getOutputStream());
        } catch (final IOException e) {
            throw new ServerException(e);
        }
    }

    /**
     * Write into the output
     *
     * @param string
     *        The string to output
     */
    protected void output(final String string) {
        final PrintWriter outputWriter = getOutputWriter();
        outputWriter.print(string);
        outputWriter.flush();
    }

    /**
     * Write into the output
     *
     * @param object
     *        An object that will be transform into JSon
     */
    protected void output(final Object object) {
        final PrintWriter outputWriter = getOutputWriter();
        outputWriter.print(JSonSerializer.serialize(object));
        outputWriter.flush();
    }

    /**
     * The outputWriter in which to write the response String.
     */
    private PrintWriter outputWriter = null;

    /**
     * Prepare the output
     *
     * @param response
     */
    private PrintWriter getOutputWriter() {
        if (outputWriter == null) {
            response.setContentType("application/json;charset=UTF-8");
            try {
                outputWriter = response.getWriter();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        return outputWriter;
    }

    /**
     * Read elements form the request
     * <ul>
     * <li>API tokens</li>
     * <li>item id (if defined)</li>
     * <li>parameters</li>
     * </ul>
     *
     * @param request
     * @param response
     */
    @SuppressWarnings("unchecked")
    protected void parseRequest(final HttpServletRequest request, final HttpServletResponse response) {
        // Create a new HashMap and copy all the elements in the new one
        parameters.putAll(this.request.getParameterMap());
    }

    public String getLocale() {
        return LocaleUtils.getUserLocaleAsString(request);
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // REQUEST ENTRY POINTS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Entry point for GET and SEARCH
     *
     * @throws IOException
     */
    public abstract void doGet() throws IOException;

    /**
     * Entry point for CREATE
     *
     * @throws IOException
     */
    public abstract void doPost() throws IOException;

    /**
     * Entry point for UPDATE
     *
     * @throws IOException
     */
    public abstract void doPut() throws IOException;

    /**
     * Entry point for DELETE
     *
     * @throws IOException
     */
    public abstract void doDelete() throws IOException;

}
