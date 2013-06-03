/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.document.impl;

import org.bonitasoft.engine.document.CmisUserProvider;

/**
 * User provider that work with modified xcmis server
 * The server must accept as username one of it's user followed by # and the bonita user
 * 
 * @author Baptiste Mesta
 */
public class XCmisUserProvider implements CmisUserProvider {

    private final String password;

    private final String username;

    /**
   * 
   */
    public XCmisUserProvider(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * @see org.ow2.bonita.services.CmisUserProvider#getUser(java.lang.String)
     */
    @Override
    public String getUser(final String bonitaUser) {
        return username + "#" + bonitaUser;
    }

    /*
     * (non-Javadoc)
     * @see org.ow2.bonita.services.CmisUserProvider#getPassword(java.lang.String)
     */
    @Override
    public String getPassword(final String bonitaUser) {
        return password;
    }

}
