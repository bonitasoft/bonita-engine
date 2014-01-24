/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.data.instance.impl;

import java.util.Hashtable;
import java.util.Map;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.data.DataSourceConfiguration;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Elias Ricken de Medeiros
 */
public class TransientDataInstanceDataSourceConfiguration implements DataSourceConfiguration {

    public static final String SESSION_ACCESSOR_KEY = "sessionAccessor";

    public static final String CACHE_SERVICE_KEY = "cacheService";

    private static final String QUERIABLE_LOGGER_SERVICE = "queriableLoggerService";

    private final Map<String, Object> resources;

    public TransientDataInstanceDataSourceConfiguration(final CacheService cacheService,
            final QueriableLoggerService queriableLoggerService) {
        resources = new Hashtable<String, Object>();
        resources.put(CACHE_SERVICE_KEY, cacheService);
        resources.put(QUERIABLE_LOGGER_SERVICE, queriableLoggerService);
    }

    @Override
    public Map<String, Object> getResources() {
        return resources;
    }

}
