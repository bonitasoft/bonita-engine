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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.LOCALE;

/**
 * This class represent a service.<br />
 * It must define a public final static String TOKEN.
 *
 * @author Séverin Moussel
 */
public abstract class Service {

    /**
     * The ServletCall responsible of this service.
     */
    private ServiceServletCall caller = null;

    /**
     * Set the caller.
     *
     * @param caller
     *        The ServletCall responsible of this service.
     */
    public void setCaller(final ServiceServletCall caller) {
        this.caller = caller;
    }

    /**
     * @see org.bonitasoft.web.toolkit.server.ServletCall#getHttpSession()
     */
    protected HttpSession getHttpSession() {
        return caller.getHttpSession();
    }

    /**
     * @see org.bonitasoft.web.toolkit.server.ServletCall#getResponse()
     */
    public HttpServletResponse getHttpResponse() {
        return caller.getResponse();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ENTRY POINT
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Entry point of the service.<br />
     * The returned object will be send as response.
     * <ul>
     * <li>If the object is a String, Integer, Long, ..., the response content will be the value as is.</li>
     * <li>If the object implements JsonSerializable, the content will be returned as a json String.</li>
     * <li>If the object is a list or a map, the response will be a json String.</li>
     * <li>Otherwise, the response content will be the object.toString().</li>
     * </ul>
     *
     * @return This method returns the output to respond.
     * @throws IOException
     */
    public abstract Object run() throws IOException;

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DELEGATIONS FOR PARAMETERS ACCESS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @see org.bonitasoft.web.toolkit.server.ServletCall#getParameter(java.lang.String)
     */
    public String getParameter(final String name) {
        return caller.getParameter(name);
    }

    public LOCALE getLocale() {
        try {
            return LOCALE.valueOf(caller.getLocale());
        } catch (final IllegalArgumentException e) {
            return AbstractI18n.getDefaultLocale();
        }
    }

}
