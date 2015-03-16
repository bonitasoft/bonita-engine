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
package org.bonitasoft.engine.bdm.dao.client.resources.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;

import org.bonitasoft.engine.bdm.dao.client.resources.BusinessObjectDeserializer;
import org.bonitasoft.engine.bdm.dao.client.resources.utils.BDMQueryCommandParameters;
import org.bonitasoft.engine.bdm.dao.client.resources.utils.EntityGetter;

public class LazyLoader {

    private final APISession apiSession;
    private final BusinessObjectDeserializer deserializer;

    public LazyLoader(final APISession apiSession) {
        if (apiSession == null) {
            throw new IllegalArgumentException("apiSession cannot be null");
        }
        this.apiSession = apiSession;
        deserializer = new BusinessObjectDeserializer();
    }

    public Object load(final Method method, final long persistenceId) {
        try {
            EntityGetter getter = new EntityGetter(method);
            final Map<String, Serializable> commandParameters = BDMQueryCommandParameters.createCommandParameters(getter, persistenceId);
            final byte[] serializedResult = (byte[]) getCommandAPI().execute("executeBDMQuery", commandParameters);
            if (getter.returnsList()) {
                return deserializer.deserializeList(serializedResult, getter.getTargetEntityClass());
            }
            return deserializer.deserialize(serializedResult, getter.getTargetEntityClass());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * protected for testing
     */
    protected CommandAPI getCommandAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getCommandAPI(apiSession);
    }
}
