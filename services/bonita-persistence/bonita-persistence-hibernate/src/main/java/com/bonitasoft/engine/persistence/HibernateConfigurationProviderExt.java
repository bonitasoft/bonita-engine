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
package com.bonitasoft.engine.persistence;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.persistence.HibernateResourcesConfigurationProvider;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.persistence.HibernateConfigurationProviderImpl;

/**
 * @author Celine Souchet
 */
public class HibernateConfigurationProviderExt extends HibernateConfigurationProviderImpl {

    private final Map<String, String> cacheQueries;

    public HibernateConfigurationProviderExt(final Properties properties,
            final HibernateResourcesConfigurationProvider hibernateResourcesConfigurationProvider,
            final Map<String, String> cacheQueries, final List<String> classesWithLoadByQuery)
            throws SPersistenceException {
        super(properties, hibernateResourcesConfigurationProvider, classesWithLoadByQuery);
        this.cacheQueries = cacheQueries;
    }

    @Override
    public Map<String, String> getCacheQueries() {
        return cacheQueries;
    }
}
