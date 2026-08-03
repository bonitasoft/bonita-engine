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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

/**
 * @author Nicolas Chabanoles
 */
public class MockHttpServletRequest implements HttpServletRequest {

    HttpSession session = null;

    Map<String, String[]> parametersMap;

    Map<String, Object> attributesMap;

    HttpServletRequest req = null;

    String pathInfo = null;

    public MockHttpServletRequest() {
        parametersMap = new HashMap<>();
        attributesMap = new HashMap<>();
    }

    @Override
    public String getAuthType() {
        if (this.req != null) {
            return this.req.getAuthType();
        }
        return null;
    }

    @Override
    public String getContextPath() {
        if (this.req != null) {
            return this.req.getContextPath();
        }
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        if (this.req != null) {
            return this.req.getCookies();
        }
        return null;
    }

    @Override
    public long getDateHeader(final String anName) {
        if (this.req != null) {
            return this.req.getDateHeader(anName);
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    @Override
    public String getHeader(final String anName) {
        if (this.req != null) {
            return this.req.getHeader(anName);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getHeaderNames()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getHeaderNames() {
        if (this.req != null) {
            return this.req.getHeaderNames();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getHeaders(final String anName) {
        if (this.req != null) {
            return this.req.getHeaders(anName);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    @Override
    public int getIntHeader(final String anName) {
        if (this.req != null) {
            return this.req.getIntHeader(anName);
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getMethod()
     */
    @Override
    public String getMethod() {
        if (this.req != null) {
            return this.req.getMethod();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getPathInfo()
     */
    @Override
    public String getPathInfo() {
        if (pathInfo != null) {
            return this.pathInfo;
        } else if (this.req != null) {
            return this.req.getPathInfo();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getPathTranslated()
     */
    @Override
    public String getPathTranslated() {
        if (this.req != null) {
            return this.req.getPathTranslated();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getQueryString()
     */
    @Override
    public String getQueryString() {
        if (this.req != null) {
            return this.req.getQueryString();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getRemoteUser()
     */
    @Override
    public String getRemoteUser() {
        if (this.req != null) {
            return this.req.getRemoteUser();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getRequestURI()
     */
    @Override
    public String getRequestURI() {
        if (this.req != null) {
            return this.req.getRequestURI();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getRequestURL()
     */
    @Override
    public StringBuffer getRequestURL() {
        if (this.req != null) {
            return this.req.getRequestURL();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    @Override
    public String getRequestedSessionId() {
        if (this.req != null) {
            return this.req.getRequestedSessionId();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getServletPath()
     */
    @Override
    public String getServletPath() {
        if (this.req != null) {
            return this.req.getRequestedSessionId();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getSession()
     */
    @Override
    public HttpSession getSession() {
        if (this.req != null) {
            this.session = this.req.getSession();
        }
        if (this.session == null) {
            this.session = new MockHttpSession();
        }
        return this.session;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getSession(boolean)
     */
    @Override
    public HttpSession getSession(final boolean anCreate) {

        if (anCreate) {
            if (this.req != null) {
                this.session = this.req.getSession(anCreate);
            } else {
                this.session = new MockHttpSession();
            }
        }
        return this.session;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    @Override
    public Principal getUserPrincipal() {
        if (this.req != null) {
            return this.req.getUserPrincipal();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        if (this.req != null) {
            return this.req.isRequestedSessionIdFromCookie();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        if (this.req != null) {
            return this.req.isRequestedSessionIdFromURL();
        }
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        if (this.req != null) {
            return this.req.isRequestedSessionIdValid();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
     */
    @Override
    public boolean isUserInRole(final String anRole) {
        if (this.req != null) {
            return this.req.isUserInRole(anRole);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(final String aName) {
        if (this.req != null) {
            return this.req.getAttribute(aName);
        } else {
            return attributesMap.get(aName);
        }
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getAttributeNames()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {
        if (this.req != null) {
            return this.req.getAttributeNames();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
        if (this.req != null) {
            return this.req.getCharacterEncoding();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getContentLength()
     */
    @Override
    public int getContentLength() {
        if (this.req != null) {
            return this.req.getContentLength();
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getContentType()
     */
    @Override
    public String getContentType() {
        if (this.req != null) {
            return this.req.getContentType();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getInputStream()
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.req != null) {
            return this.req.getInputStream();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getLocalAddr()
     */
    @Override
    public String getLocalAddr() {
        if (this.req != null) {
            return this.req.getLocalAddr();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getLocalName()
     */
    @Override
    public String getLocalName() {
        if (this.req != null) {
            return this.req.getLocalName();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getLocalPort()
     */
    @Override
    public int getLocalPort() {
        if (this.req != null) {
            return this.req.getLocalPort();
        }
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getLocale()
     */
    @Override
    public Locale getLocale() {
        if (this.req != null) {
            return this.req.getLocale();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getLocales()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getLocales() {
        if (this.req != null) {
            return this.req.getLocales();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(final String aName) {
        final String[] strings = this.parametersMap.get(aName);
        if (strings != null && strings.length > 0) {
            return strings[0];
        } else {
            return null;
        }
    }

    public void setParameter(final String anName, final String value) {
        this.parametersMap.put(anName, new String[] { value });
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getParameterMap()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String[]> getParameterMap() {
        if (req == null) {
            return this.parametersMap;
        } else {
            return this.req.getParameterMap();
        }
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getParameterNames()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getParameterNames() {
        if (this.req != null) {
            return this.req.getParameterNames();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues(final String anName) {
        if (this.req != null) {
            return this.req.getParameterValues(anName);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getProtocol()
     */
    @Override
    public String getProtocol() {
        if (this.req != null) {
            return this.req.getProtocol();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getReader()
     */
    @Override
    public BufferedReader getReader() throws IOException {
        if (this.req != null) {
            return this.req.getReader();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getRemoteAddr()
     */
    @Override
    public String getRemoteAddr() {
        if (this.req != null) {
            return this.req.getRemoteAddr();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getRemoteHost()
     */
    @Override
    public String getRemoteHost() {
        if (this.req != null) {
            return this.req.getRemoteHost();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getRemotePort()
     */
    @Override
    public int getRemotePort() {
        if (this.req != null) {
            return this.req.getRemotePort();
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    @Override
    public RequestDispatcher getRequestDispatcher(final String anPath) {
        if (this.req != null) {
            return this.req.getRequestDispatcher(anPath);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getScheme()
     */
    @Override
    public String getScheme() {
        if (this.req != null) {
            return this.req.getScheme();
        }
        return "http";
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getServerName()
     */
    @Override
    public String getServerName() {
        if (this.req != null) {
            return this.req.getServerName();
        }
        return "localhost";
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#getServerPort()
     */
    @Override
    public int getServerPort() {
        if (this.req != null) {
            return this.req.getServerPort();
        }
        return 8080;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#isSecure()
     */
    @Override
    public boolean isSecure() {
        if (this.req != null) {
            return this.req.isSecure();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(final String anName) {
        if (this.req != null) {
            this.req.removeAttribute(anName);
        }
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(final String aName, final Object aValue) {
        if (this.req != null) {
            this.req.setAttribute(aName, aValue);
        } else {
            this.attributesMap.put(aName, aValue);
        }
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(final String anEnv) throws UnsupportedEncodingException {
        if (this.req != null) {
            this.req.setCharacterEncoding(anEnv);
        }
    }

    /*
     * (non-Javadoc)
     * @see jakarta.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    public void setPathInfo(final String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public long getContentLengthLong() {
        if (this.req != null) {
            return this.req.getContentLengthLong();
        }
        return 0;
    }

    @Override
    public String changeSessionId() {
        if (this.req != null) {
            return this.req.changeSessionId();
        }
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public String getRequestId() {
        if (this.req != null) {
            return this.req.getRequestId();
        }
        return null;
    }

    @Override
    public String getProtocolRequestId() {
        if (this.req != null) {
            return this.req.getProtocolRequestId();
        }
        return null;
    }

    @Override
    public ServletConnection getServletConnection() {
        if (this.req != null) {
            return this.req.getServletConnection();
        }
        return null;
    }

}
