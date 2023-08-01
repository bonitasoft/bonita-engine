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

import java.util.*;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.TenantPersistenceService;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.slf4j.Logger;

/**
 * Common implementation to persistence services relying on a database
 *
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public abstract class AbstractDBPersistenceService implements TenantPersistenceService {

    private final SequenceManager sequenceManager;

    protected final DataSource datasource;

    protected abstract Logger getLogger();

    public AbstractDBPersistenceService(final String name) {
        sequenceManager = null;
        datasource = null;
    }

    public AbstractDBPersistenceService(final SequenceManager sequenceManager,
            final DataSource datasource) {
        this.sequenceManager = sequenceManager;
        this.datasource = datasource;
    }

    @Override
    public <T extends PersistentObject> long getNumberOfEntities(final Class<T> entityClass, final QueryOptions options,
            final Map<String, Object> parameters)
            throws SBonitaReadException {
        return getNumberOfEntities(entityClass, null, options, parameters);
    }

    @Override
    public <T extends PersistentObject> long getNumberOfEntities(final Class<T> entityClass, final String querySuffix,
            final QueryOptions options,
            final Map<String, Object> parameters) throws SBonitaReadException {
        List<FilterOption> filters;
        if (options == null) {
            filters = Collections.emptyList();
        } else {
            filters = options.getFilters();
        }
        final String queryName = getQueryName("getNumberOf", querySuffix, entityClass, filters);

        final SelectListDescriptor<Long> descriptor = new SelectListDescriptor<Long>(queryName, parameters, entityClass,
                Long.class, options);
        return selectList(descriptor).get(0);
    }

    @Override
    public <T extends PersistentObject> List<T> searchEntity(final Class<T> entityClass, final QueryOptions options,
            final Map<String, Object> parameters)
            throws SBonitaReadException {
        return searchEntity(entityClass, null, options, parameters);
    }

    @Override
    public <T extends PersistentObject> List<T> searchEntity(final Class<T> entityClass, final String querySuffix,
            final QueryOptions options,
            final Map<String, Object> parameters) throws SBonitaReadException {
        final String queryName = getQueryName("search", querySuffix, entityClass, options.getFilters());
        final SelectListDescriptor<T> descriptor = new SelectListDescriptor<T>(queryName, parameters, entityClass,
                options);
        return selectList(descriptor);
    }

    private <T extends PersistentObject> String getQueryName(final String prefix, final String suffix,
            final Class<T> entityClass,
            final List<FilterOption> filters) {
        final SortedSet<String> query = new TreeSet<String>();
        for (final FilterOption filter : filters) {
            // if filter is just an operator, PersistentClass is not defined:
            if (filter.getPersistentClass() != null) {
                query.add(filter.getPersistentClass().getSimpleName());
            }
        }
        final String searchOnClassName = entityClass.getSimpleName();
        query.remove(searchOnClassName);
        final StringBuilder builder = new StringBuilder(prefix);
        builder.append(searchOnClassName);
        if (!query.isEmpty()) {
            builder.append("with");
        }
        for (final String entity : query) {
            builder.append(entity);
        }
        if (suffix != null) {
            builder.append(suffix);
        }
        return builder.toString();
    }

    /**
     * @throws STenantIdNotSetException
     */
    protected abstract long getTenantId() throws STenantIdNotSetException;

    protected SequenceManager getSequenceManager() {
        return sequenceManager;
    }

    protected void setId(final PersistentObject entity) throws SPersistenceException {
        if (entity == null) {
            return;
        }
        // if this entity has no id, set it
        Long id = null;
        try {
            id = entity.getId();
        } catch (final Exception e) {
            // this is a new object to save
        }
        if (id == null || id == -1 || id == 0) {
            try {
                final long tenantId = entity instanceof PlatformPersistentObject ? -1 : getTenantId();
                id = getSequenceManager().getNextId(entity.getClass().getName(), tenantId);
                ClassReflector.invokeSetter(entity, "setId", long.class, id);
            } catch (final Exception e) {
                throw new SPersistenceException("Problem while saving entity: " + entity + " with id: " + id, e);
            }
        }
    }

}
