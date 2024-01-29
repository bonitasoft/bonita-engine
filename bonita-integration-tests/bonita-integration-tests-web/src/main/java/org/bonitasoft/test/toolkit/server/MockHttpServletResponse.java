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
package org.bonitasoft.test.toolkit.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Rohart Bastien
 */
public class MockHttpServletResponse implements HttpServletResponse {

    HttpServletResponse res = null;

    public MockHttpServletResponse() {
    }

    @Override
    public String getCharacterEncoding() {
        if (res != null) {
            return res.getCharacterEncoding();
        }
        return null;
    }

    @Override
    public String getContentType() {
        if (res != null) {
            return res.getContentType();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (res != null) {
            return res.getOutputStream();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        if (res != null) {
            return res.getWriter();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(String charset) {
        res.setCharacterEncoding(charset);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#setContentLength(int)
     */
    @Override
    public void setContentLength(int len) {
        res.setContentLength(len);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String type) {
        res.setContentType(type);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#setBufferSize(int)
     */
    @Override
    public void setBufferSize(int size) {
        res.setBufferSize(size);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#getBufferSize()
     */
    @Override
    public int getBufferSize() {
        if (res != null) {
            return res.getBufferSize();
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#flushBuffer()
     */
    @Override
    public void flushBuffer() throws IOException {
        res.flushBuffer();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#resetBuffer()
     */
    @Override
    public void resetBuffer() {
        res.resetBuffer();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#isCommitted()
     */
    @Override
    public boolean isCommitted() {
        if (res.isCommitted()) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#reset()
     */
    @Override
    public void reset() {
        res.reset();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale loc) {
        res.setLocale(loc);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletResponse#getLocale()
     */
    @Override
    public Locale getLocale() {
        if (res != null) {
            return res.getLocale();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
     */
    @Override
    public void addCookie(Cookie cookie) {
        if (cookie != null) {
            res.addCookie(cookie);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
     */
    @Override
    public boolean containsHeader(String name) {
        if (res.containsHeader(name)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
     */
    @Override
    public String encodeURL(String url) {
        if (res != null) {
            return res.encodeURL(url);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
     */
    @Override
    public String encodeRedirectURL(String url) {
        if (res != null) {
            return res.encodeRedirectURL(url);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
     */
    @Override
    public String encodeUrl(String url) {
        if (res != null) {
            return res.encodeUrl(url);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
     */
    @Override
    public String encodeRedirectUrl(String url) {
        if (res != null) {
            return res.encodeRedirectUrl(url);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
     */
    @Override
    public void sendError(int sc, String msg) throws IOException {
        res.sendError(sc, msg);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#sendError(int)
     */
    @Override
    public void sendError(int sc) throws IOException {
        res.sendError(sc);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
     */
    @Override
    public void sendRedirect(String location) throws IOException {
        res.sendRedirect(location);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
     */
    @Override
    public void setDateHeader(String name, long date) {
        res.setDateHeader(name, date);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
     */
    @Override
    public void addDateHeader(String name, long date) {
        res.addDateHeader(name, date);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void setHeader(String name, String value) {
        res.setHeader(name, value);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String name, String value) {
        if (res != null) {
            res.addHeader(name, value);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
     */
    @Override
    public void setIntHeader(String name, int value) {
        res.setIntHeader(name, value);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
     */
    @Override
    public void addIntHeader(String name, int value) {
        res.addIntHeader(name, value);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#setStatus(int)
     */
    @Override
    public void setStatus(int sc) {
        res.setStatus(sc);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
     */
    @Override
    public void setStatus(int sc, String sm) {
        res.setStatus(sc, sm);
    }

    @Override
    public int getStatus() {
        if (res != null) {
            return res.getStatus();
        }
        return 0;
    }

    @Override
    public String getHeader(String name) {
        if (res != null) {
            return res.getHeader(name);
        }
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        if (res != null) {
            return res.getHeaders(name);
        }
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        if (res != null) {
            return res.getHeaderNames();
        }
        return null;
    }

    @Override
    public void setContentLengthLong(long len) {
        if (res != null) {
            res.setContentLengthLong(len);
        }
    }

}
