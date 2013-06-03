/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.home;

import java.io.File;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public final class BonitaHomeClient extends BonitaHome {

    public static final String BONITA_HOME_CLIENT = "client";

    private String clientPath;

    public static final BonitaHomeClient INSTANCE = new BonitaHomeClient();

    private BonitaHomeClient() {
        super();
    }

    public static BonitaHomeClient getInstance() {
        return INSTANCE;
    }

    public String getBonitaHomeClientFolder() throws BonitaHomeNotSetException {
        if (clientPath == null) {
            final StringBuilder path = new StringBuilder(getBonitaHomeFolder());
            path.append(File.separatorChar);
            path.append(BONITA_HOME_CLIENT);
            clientPath = path.toString();
        }
        return clientPath;
    }

    @Override
    protected void refresh() {
        clientPath = null;
    }

}
