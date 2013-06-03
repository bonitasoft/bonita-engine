/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.session.Session;

/**
 * @author Matthieu Chaffotte
 */
public class ClientSessionInterceptor extends ClientInterceptor {

    /**
     * This interceptor is equivalent to its parent but adds the session in the map of options
     */
    private static final long serialVersionUID = 7901658474276091133L;

    private final Session session;

    public ClientSessionInterceptor(final String interfaceName, final ServerAPI api, final Session session) {
        super(interfaceName, api);
        this.session = session;
    }
    
    @Override
    protected Map<String, Serializable> getOptions() {
        final Map<String, Serializable> options = super.getOptions();
        options.put("session", this.session);
        return options;
    }

}
