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
package org.bonitasoft.console.common.server.auth;

import org.bonitasoft.console.common.server.auth.impl.standard.StandardAuthenticationManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ruiheng Fan
 */
public class AuthenticationManagerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationManagerFactory.class.getName());

    static AuthenticationManager authenticationManager;

    public static AuthenticationManager getAuthenticationManager() throws AuthenticationManagerNotFoundException {
        String authenticationManagerName = null;
        if (authenticationManager == null) {
            try {
                authenticationManagerName = getManagerImplementationClassName();
                authenticationManager = (AuthenticationManager) Class.forName(authenticationManagerName).newInstance();
            } catch (final Exception e) {
                final String message = "The AuthenticationManager implementation " + authenticationManagerName
                        + " does not exist!";
                throw new AuthenticationManagerNotFoundException(message);
            }
        }
        return authenticationManager;
    }

    private static String getManagerImplementationClassName() {
        String authenticationManagerName = AuthenticationManagerProperties.getProperties()
                .getAuthenticationManagerImpl();
        if (authenticationManagerName == null || authenticationManagerName.isEmpty()) {
            authenticationManagerName = StandardAuthenticationManagerImpl.class.getName();
            LOGGER.trace("The login manager implementation is undefined. Using default implementation : "
                    + authenticationManagerName);
        }
        return authenticationManagerName;
    }
}
