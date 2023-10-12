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
package org.bonitasoft.console.common.server.login.filter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.api.token.APIToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien Reboul
 */
public class TokenGenerator {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TokenGenerator.class.getName());

    public static final String API_TOKEN = "api_token";
    public static final String X_BONITA_API_TOKEN = "X-Bonita-API-Token";

    /**
     * generate and store the CSRF security inside HTTP session
     * or retrieve it token from the HTTP session
     *
     * @param session the HTTP session
     * @return the CSRF security token
     */
    public String createOrLoadToken(final HttpSession session) {
        Object apiTokenFromClient = session.getAttribute(API_TOKEN);
        if (apiTokenFromClient == null) {
            apiTokenFromClient = new APIToken().getToken();
            session.setAttribute(API_TOKEN, apiTokenFromClient);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Bonita API Token generated: " + apiTokenFromClient);
            }
        }
        return apiTokenFromClient.toString();
    }

    /**
     * set the CSRF security token to the HTTP response as HTTP Header.
     *
     * @param res the http response
     * @param token the security token to set
     */
    public void setTokenToResponseHeader(final HttpServletResponse res, final String token) {
        if (res.containsHeader(X_BONITA_API_TOKEN)) {
            res.setHeader(X_BONITA_API_TOKEN, token);
        } else {
            res.addHeader(X_BONITA_API_TOKEN, token);
        }
    }

}
