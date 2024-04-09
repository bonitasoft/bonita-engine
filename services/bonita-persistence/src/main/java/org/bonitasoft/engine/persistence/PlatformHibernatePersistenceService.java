/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.persistence;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@Slf4j
public class PlatformHibernatePersistenceService extends AbstractHibernatePersistenceService {

    protected PlatformHibernatePersistenceService(final SessionFactory sessionFactory,
            final List<Class<? extends PersistentObject>> classMapping,
            final Map<String, String> classAliasMappings, final boolean enableWordSearch,
            final Set<String> wordSearchExclusionMappings) throws Exception {
        super(sessionFactory, classMapping, classAliasMappings, enableWordSearch, wordSearchExclusionMappings);
    }

    public PlatformHibernatePersistenceService(final String name,
            final HibernateConfigurationProvider hbmConfigurationProvider,
            final Properties extraHibernateProperties, final SequenceManager sequenceManager,
            final DataSource datasource, QueryBuilderFactory queryBuilderFactory)
            throws Exception {
        super(name, hbmConfigurationProvider, extraHibernateProperties, sequenceManager, datasource,
                queryBuilderFactory);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected long getTenantId() {
        return -1;
    }

    @Override
    public void flushStatements() throws SPersistenceException {
        super.flushStatements(false);
    }

    @Override
    public void deleteByTenant(final Class<? extends PersistentObject> entityClass, final List<FilterOption> filters) {
        // FIXME : Method for tenant. TODO: Refactor code for PlatformHibernatePersistenceService don't implements TenantPersistenceService
    }

}
