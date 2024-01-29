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
package org.bonitasoft.console.common.server.login.credentials;

/**
 * @author Julien Reboul
 */
public class StandardCredentials implements Credentials {

    private final String name;

    private final String password;

    public StandardCredentials(final String name, final String password) {
        this.name = name;
        this.password = password;
    }

    /**
     * @see org.bonitasoft.console.common.server.login.credentials.Credentials#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @see org.bonitasoft.console.common.server.login.credentials.Credentials#getPassword()
     */
    @Override
    public String getPassword() {
        return password;
    }

}
