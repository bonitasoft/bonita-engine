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

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.exceptions.SReflectException;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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

/**
 * @author Baptiste Mesta
 * @author Nicolas Chabanoles
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class TenantHibernatePersistenceService extends AbstractHibernatePersistenceService {

    private static final String TENANT_ID = "tenantId";

    private static final String TENANT_FILTER = "tenantFilter";

    private final ReadSessionAccessor sessionAccessor;

    public TenantHibernatePersistenceService(final String name, final ReadSessionAccessor sessionAccessor,
            final HibernateConfigurationProvider hbmConfigurationProvider, final Properties extraHibernateProperties,
            final TechnicalLoggerService logger, final SequenceManager sequenceManager, final DataSource datasource,
            HibernateMetricsBinder hibernateMetricsBinder,
            QueryBuilderFactory queryBuilderFactory)
            throws Exception {
        super(name, hbmConfigurationProvider, extraHibernateProperties, logger,
                sequenceManager, datasource, queryBuilderFactory);
        this.sessionAccessor = sessionAccessor;
        hibernateMetricsBinder.bindMetrics(getSessionFactory());
    }

    protected void updateTenantFilter(final Session session, final boolean useTenant) throws SPersistenceException {
        if (useTenant) {
            try {
                session.enableFilter(TENANT_FILTER).setParameter(TENANT_ID, getTenantId());
            } catch (final STenantIdNotSetException e) {
                throw new SPersistenceException(e);
            }
        } else {
            session.disableFilter(TENANT_FILTER);
        }
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
        } catch (final SReflectException e) {
            throw new SPersistenceException("Can't set tenantId = <" + tenantId + "> on entity." + entity, e);
        } catch (final STenantIdNotSetException e) {
            throw new SPersistenceException("Can't set tenantId = <" + tenantId + "> on entity." + entity, e);
        }
    }

    @Override
    protected Session getSession(final boolean useTenant) throws SPersistenceException {
        final Session session = super.getSession(useTenant);
        updateTenantFilter(session, useTenant);
        return session;
    }

    @Override
    public void flushStatements() throws SPersistenceException {
        super.flushStatements(true);
    }

    @Override
    public void delete(final PersistentObject entity) throws SPersistenceException {
        try {
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                        "[Tenant] Deleting instance of class " + entity.getClass().getSimpleName()
                                + " with id=" + entity.getId());
            }
            final Class<? extends PersistentObject> mappedClass = getMappedClass(entity.getClass());
            final Session session = getSession(true);
            final Object pe = session.get(mappedClass, new PersistentObjectId(entity.getId(), getTenantId()));
            session.delete(pe);
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final STenantIdNotSetException | HibernateException e) {
            throw new SPersistenceException(e);
        }
    }

    @Override
    public void insert(final PersistentObject entity) throws SPersistenceException {
        setTenant(entity);
        super.insert(entity);
    }

    @Override
    public void insertInBatch(final List<? extends PersistentObject> entities) throws SPersistenceException {
        for (final PersistentObject entity : entities) {
            setTenant(entity);
        }
        super.insertInBatch(entities);
    }

    @Override
    protected long getTenantId() throws STenantIdNotSetException {
        return sessionAccessor.getTenantId();
    }

    @SuppressWarnings("unchecked")
    @Override
    <T extends PersistentObject> T selectById(final Session session, final SelectByIdDescriptor<T> selectDescriptor)
            throws SBonitaReadException {
        try {
            final PersistentObjectId id = new PersistentObjectId(selectDescriptor.getId(), getTenantId());
            Class<? extends PersistentObject> mappedClass = null;
            mappedClass = getMappedClass(selectDescriptor.getEntityType());
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
        try {
            final Session session = getSession(true);
            final String entityClassName = entityClass.getCanonicalName();

            boolean hasFilters = filters != null && !filters.isEmpty();
            Map<String, Object> parameters = new HashMap<>();
            String baseQuery = "DELETE FROM " + entityClassName + " "
                    + (hasFilters ? getClassAliasMappings().get(entityClassName) : "")
                    + " WHERE tenantId= :tenantId";

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
                baseQuery += " AND ( " + whereClause.getFilters() + " )";
            }
            Query query = session.createQuery(baseQuery);
            query.setLong(TENANT_ID, getTenantId());
            parameters.forEach(query::setParameter);
            query.executeUpdate();
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                        "[Tenant] Deleting all instance of class " + entityClass.getClass().getSimpleName());
            }
        } catch (final STenantIdNotSetException e) {
            throw new SPersistenceException(e);
        }
    }

}
