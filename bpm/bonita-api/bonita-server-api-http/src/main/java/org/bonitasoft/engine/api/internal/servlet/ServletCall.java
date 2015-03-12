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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.io.IOUtil;

/**
 * @author Severin Moussel
 */
public abstract class ServletCall {

    private static final String BINARY_PARAMETER = "binaryParameter";

    private String inputStream = null;

    /**
     * The parameters of the URL.<br />
     * Result of the parsing of the query string : "?a=b&c=d&..."
     */
    protected Map<String, String> parameters = null;

    /**
     * The request made to access this servletCall.
     */
    private final HttpServletRequest request;

    /**
     * The response to return.
     */
    private final HttpServletResponse response;

    private final List<byte[]> binaryParameters;

    /**
     * Default constructor.
     * 
     * @param request
     *        The request made to access this servletCall.
     * @param response
     *        The response to return.
     * @throws IOException
     * @throws FileUploadException
     */
    public ServletCall(final HttpServletRequest request, final HttpServletResponse response) throws FileUploadException, IOException {
        super();
        this.request = request;
        this.response = response;
        parameters = new HashMap<String, String>();
        binaryParameters = new ArrayList<byte[]>();
        if (ServletFileUpload.isMultipartContent(request)) {
            final ServletFileUpload upload = new ServletFileUpload();
            // Parse the request
            final FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                try {
                    final FileItemStream item = iter.next();
                    final InputStream stream = item.openStream();
                    String fieldName = item.getFieldName();
                    if (fieldName.startsWith(BINARY_PARAMETER)) {
                        binaryParameters.add(IOUtil.getAllContentFrom(stream));
                    } else {
                        String read = IOUtil.read(stream);
                        parameters.put(fieldName, read);
                    }
                    stream.close();
                } catch (final Exception t) {
                    throw new IOException(t);
                }
            }
        } else {
            final Map<String, String[]> parameterMap = this.request.getParameterMap();
            final Set<Entry<String, String[]>> entrySet = parameterMap.entrySet();
            for (final Entry<String, String[]> entry : entrySet) {
                parameters.put(entry.getKey(), entry.getValue()[0]);
            }
        }
    }

    /**
     * @return the binaryParameters
     */
    public List<byte[]> getBinaryParameters() {
        return binaryParameters;
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
    public final String getRequestURL() {
        return request.getRequestURL().toString();
    }

    /**
     * Read the input stream and set it in a String
     */
    public final String getInputStream() {
        if (inputStream == null) {
            try {
                final BufferedReader reader = request.getReader();
                final StringBuilder sb = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = reader.readLine();
                }
                reader.close();

                inputStream = sb.toString();

            } catch (final IOException e) {
                throw new RuntimeException("Can't read input Stream.", e);
            }
        }

        return inputStream;
    }

    /**
     * Count the number of parameters passed in the URL
     * 
     * @return This method returns the number of parameters in the URL
     */
    public final int countParameters() {
        return parameters.size();
    }

    /**
     * Get a parameter values by its name
     * 
     * @param name
     *        The name of the parameter (case sensitive)
     * @return This method returns the values of a parameter as a list of String or null if the parameter isn't defined
     */
    public final List<String> getParameterAsList(final String name) {
        return getParameterAsList(name, (String) null);
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
    public final List<String> getParameterAsList(final String name, final String defaultValue) {
        if (parameters.containsKey(name)) {
            return Arrays.asList(parameters.get(name));
        }
        if (defaultValue != null) {
            final List<String> results = new ArrayList<String>();
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
    public final String getParameter(final String name) {
        return getParameter(name, (String) null);
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
    public final String getParameter(final String name, final String defaultValue) {
        if (parameters.containsKey(name)) {
            return parameters.get(name);
        }
        return defaultValue;
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
    protected final void head(final String name, final String value) {
        response.addHeader(name, value);
    }

    /**
     * Output a file
     * 
     * @param file
     *        The file to output
     */
    protected final void output(final File file) {
        try {
            output(new FileInputStream(file));
        } catch (final FileNotFoundException e) {
            error("Can not download file.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
            error("Can not download stream.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void error(final String message, final int errorCode) {
        output(message);
        response.setStatus(errorCode);
    }

    /**
     * Write into the output
     * 
     * @param string
     *        The string to output
     */
    protected final void output(final String string) {
        final PrintWriter outputWriter = getOutputWriter();
        outputWriter.print(string);
        outputWriter.flush();
        outputWriter.close();
    }

    /**
     * Write into the output
     * 
     * @param object
     *        An object that will be transform into JSon
     */
    protected final void output(final Object object) {
        final PrintWriter outputWriter = getOutputWriter();
        // FIXME use xstream

        outputWriter.print(object.toString());
        outputWriter.flush();
        outputWriter.close();
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

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // REQUEST ENTRY POINTS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Entry point for GET and SEARCH
     */
    public abstract void doGet();

    /**
     * Entry point for CREATE
     */
    public abstract void doPost();

    /**
     * Entry point for UPDATE
     */
    public abstract void doPut();

    /**
     * Entry point for DELETE
     */
    public abstract void doDelete();

}
