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
package org.bonitasoft.web.rest.model.user;

import java.io.Serializable;

/**
 * @author Guo Yongtao
 */
public class User implements Serializable {

    /**
     * ID used for serialization.
     */
    private static final long serialVersionUID = 1940844173066923676L;

    /**
     * The username
     */
    protected String username;

    /**
     * Indicates the locale to use to display the user interface
     */
    protected String locale;

    public User() {
        super();
        // Mandatory for serialization.
    }

    public User(final String username, final String locale) {
        this.username = username;
        this.locale = locale;
    }

    /**
     * @return the userUUID
     */
    public String getUsername() {
        return username;
    }

    public String getLocale() {
        return locale;
    }

}
