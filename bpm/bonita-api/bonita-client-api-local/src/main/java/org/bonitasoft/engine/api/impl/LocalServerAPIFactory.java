/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.exception.ServerAPIException;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class LocalServerAPIFactory {

    private static Class<?> forName = null;

    private LocalServerAPIFactory() {
        // For Sonar
    }

    static {
        try {
            forName = Class.forName("org.bonitasoft.engine.api.impl.ServerAPIFactory");
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static ServerAPI getServerAPI() throws ServerAPIException {
        try {
            return (ServerAPI) forName.getMethod("getServerAPI").invoke(null);
        } catch (final Exception e) {
            throw new ServerAPIException(e);
        }
    }

}
