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
package org.bonitasoft.console.common.server.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APISessionInvalidException;

/**
 * @author Vincent Elcrin
 */
public final class CommandCaller {

    private final APISession session;

    private final String command;

    private final Map<String, Serializable> parameters;

    /**
     * Default Constructor.
     */
    public CommandCaller(APISession session, final String command) {
        this.session = session;
        this.command = command;
        parameters = new HashMap<>();
    }

    public CommandCaller addParameter(final String key, final Serializable value) {
        parameters.put(key, value);
        return this;
    }

    public Serializable run() {
        try {
            return TenantAPIAccessor.getCommandAPI(this.session).execute(this.command, this.parameters);
        } catch (InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }
}
