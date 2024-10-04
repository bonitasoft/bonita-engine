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
package org.bonitasoft.console.common.server.auth.impl.jaas;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.bonitasoft.console.common.server.utils.TenantsManagementUtils;

/**
 * Console call back handler
 *
 * @author Qixiang Zhang
 */
public class ConsoleCallbackHandler implements CallbackHandler {

    /**
     * User name
     */
    private final String name;

    /**
     * User password
     */
    private final String password;

    /**
     * Default Constructor.
     *
     * @param name
     *        user name
     * @param password
     *        user password
     */
    public ConsoleCallbackHandler(final String name, final String password) {
        this.name = name;
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    @Override
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (final Callback callback : callbacks) {
            if (callback instanceof NameCallback nc) {
                nc.setName(this.name);
            } else if (callback instanceof PasswordCallback pc) {
                pc.setPassword(this.password.toCharArray());
            } else if (callback instanceof TextInputCallback tc) {
                tc.setText(String.valueOf(TenantsManagementUtils.getDefaultTenantId()));
            }

        }
    }

}
