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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.exceptions.SReflectException;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.query.Query;
import org.slf4j.Logger;

/**
 * @author Baptiste Mesta
 * @author Nicolas Chabanoles
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@Slf4j
public class TenantHibernatePersistenceService extends AbstractHibernatePersistenceService {

    private final ReadSessionAccessor sessionAccessor;

    public TenantHibernatePersistenceService(final ReadSessionAccessor sessionAccessor,
            final HibernateConfigurationProvider hbmConfigurationProvider, final Properties extraHibernateProperties,
            final SequenceManager sequenceManager, final DataSource datasource,
            HibernateMetricsBinder hibernateMetricsBinder,
            QueryBuilderFactory queryBuilderFactory) {
        super(hbmConfigurationProvider, extraHibernateProperties, sequenceManager, datasource,
                queryBuilderFactory);
        this.sessionAccessor = sessionAccessor;
        hibernateMetricsBinder.bindMetrics(getSessionFactory());
    }

    protected void setTenant(final PersistentObject entity) throws SPersistenceException {
        if (entity == null) {
            return;
        }
        // if this entity has no id, set it
        Long tenantId = null;
        try {
            tenantId = ClassReflector.invokeGetter(entity, "getTenantId");
        } catch (final Exception e) {
            // this is a new object to save
        }
        if (tenantId == null || tenantId == -1 || tenantId == 0) {
            setTenantByClassReflector(entity, tenantId);
        }
    }

    private void setTenantByClassReflector(final PersistentObject entity, Long tenantId) throws SPersistenceException {
        try {
            tenantId = getTenantId();
            ClassReflector.invokeSetter(entity, "setTenantId", long.class, tenantId);
        } catch (final SReflectException | STenantIdNotSetException e) {
            throw new SPersistenceException("Can't set tenantId = <" + tenantId + "> on entity." + entity, e);
        }
    }

    @Override
    public void insert(final PersistentObject entity) throws SPersistenceException {
        if (!(entity instanceof PlatformPersistentObject)) {
            setTenant(entity);
        }
        super.insert(entity);
    }

    @Override
    public void insertInBatch(final List<? extends PersistentObject> entities) throws SPersistenceException {
        for (final PersistentObject entity : entities) {
            if (!(entity instanceof PlatformPersistentObject)) {
                setTenant(entity);
            }
        }
        super.insertInBatch(entities);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected long getTenantId() throws STenantIdNotSetException {
        return sessionAccessor.getTenantId();
    }

    @SuppressWarnings("unchecked")
    @Override
    <T extends PersistentObject> T selectById(final Session session, final SelectByIdDescriptor<T> selectDescriptor)
            throws SBonitaReadException {
        if (PlatformPersistentObject.class.isAssignableFrom(selectDescriptor.getEntityType())) {
            return super.selectById(session, selectDescriptor);
        }
        try {
            final PersistentObjectId id = new PersistentObjectId(selectDescriptor.getId(), getTenantId());
            Class<? extends PersistentObject> mappedClass = getMappedClass(selectDescriptor.getEntityType());
            return (T) session.get(mappedClass, id);
        } catch (final STenantIdNotSetException e) {
            return super.selectById(session, selectDescriptor);
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final SPersistenceException | HibernateException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public void deleteByTenant(final Class<? extends PersistentObject> entityClass, final List<FilterOption> filters)
            throws SPersistenceException {
        final Session session = getSession();
        final String entityClassName = entityClass.getCanonicalName();

        boolean hasFilters = filters != null && !filters.isEmpty();
        Map<String, Object> parameters = new HashMap<>();
        String baseQuery = "DELETE FROM " + entityClassName + " "
                + (hasFilters ? getClassAliasMappings().get(entityClassName) : "");

        if (hasFilters) {
            if (filters.stream().anyMatch(f -> f.getFilterOperationType() == FilterOperationType.LIKE)) {
                throw new IllegalStateException("Delete queries do not support queries with LIKE");
            }
            QueryGeneratorForFilters.QueryGeneratedFilters whereClause = new QueryGeneratorForFilters(
                    getClassAliasMappings(), '%'/*
                                                 * there is no
                                                 * 'like' in these
                                                 * delete queries
                                                 */)
                            .generate(filters);
            parameters.putAll(whereClause.getParameters());
            baseQuery += " WHERE ( " + whereClause.getFilters() + " )";
        }
        Query query = session.createQuery(baseQuery);
        parameters.forEach(query::setParameter);
        query.executeUpdate();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("[Tenant] Deleting all instance of class " + entityClass.getSimpleName());
        }
    }

}
