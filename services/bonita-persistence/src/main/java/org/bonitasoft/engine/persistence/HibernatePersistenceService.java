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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PreDestroy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.exceptions.SReflectException;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.query.Query;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;

/**
 * Hibernate implementation of the persistence service
 *
 * @author Charles Souillard
 * @author Nicolas Chabanoles
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Laurent Vaills
 * @author Guillaume Rosinosky
 */
@Slf4j
public class HibernatePersistenceService implements PersistenceService {

    @Getter
    private final SessionFactory sessionFactory;

    private final ReadSessionAccessor sessionAccessor;

    private final Map<String, String> classAliasMappings;

    @Getter // for testing purposes
    private final Map<String, String> cacheQueries;

    private final List<Class<? extends PersistentObject>> classMapping;

    private final List<String> mappingExclusions;
    private final Statistics statistics;
    private final SequenceManager sequenceManager;
    private int stat_display_count;
    private final QueryBuilderFactory queryBuilderFactory;

    public HibernatePersistenceService(final ReadSessionAccessor sessionAccessor,
            final HibernateConfigurationProvider hbmConfigurationProvider,
            final Properties extraHibernateProperties, final SequenceManager sequenceManager,
            final QueryBuilderFactory queryBuilderFactory, HibernateMetricsBinder hibernateMetricsBinder) {
        this.sequenceManager = sequenceManager;
        this.sessionAccessor = sessionAccessor;
        hbmConfigurationProvider.bootstrap(extraHibernateProperties);
        sessionFactory = hbmConfigurationProvider.getSessionFactory();

        this.queryBuilderFactory = queryBuilderFactory;
        statistics = sessionFactory.getStatistics();
        classMapping = hbmConfigurationProvider.getMappedClasses();
        classAliasMappings = hbmConfigurationProvider.getClassAliasMappings();
        mappingExclusions = hbmConfigurationProvider.getMappingExclusions();
        cacheQueries = hbmConfigurationProvider.getCacheQueries();

        hibernateMetricsBinder.bindMetrics(getSessionFactory());
    }

    /**
     * Log synthetic information about cache every 10.000 sessions, if hibernate.gather_statistics, is enabled.
     */
    private void logStats() {
        if (!statistics.isStatisticsEnabled() || !getLogger().isInfoEnabled()) {
            return;
        }
        if (stat_display_count == 10 || stat_display_count == 100 || stat_display_count == 1000
                || stat_display_count % 10000 == 0) {
            final long query_cache_hit = statistics.getQueryCacheHitCount();
            final long query_cache_miss = statistics.getQueryCacheMissCount();
            final long query_cache_put = statistics.getQueryCachePutCount();
            final long level_2_cache_hit = statistics.getSecondLevelCacheHitCount();
            final long level_2_cache_miss = statistics.getSecondLevelCacheMissCount();
            final long level_2_put = statistics.getSecondLevelCachePutCount();

            getLogger().info("Query Cache Ratio "
                    + (int) ((double) query_cache_hit / (query_cache_hit + query_cache_miss) * 100) + "% "
                    + query_cache_hit + " hits " + query_cache_miss
                    + " miss " + query_cache_put + " puts");
            getLogger().info("2nd Level Cache Ratio "
                    + (int) ((double) level_2_cache_hit / (level_2_cache_hit + level_2_cache_miss) * 100) + "% "
                    + level_2_cache_hit + " hits "
                    + level_2_cache_miss + " miss " + level_2_put + " puts");
        }
        stat_display_count++;
    }

    protected Session getSession() throws SPersistenceException {
        logStats();
        try {
            return sessionFactory.getCurrentSession();
        } catch (final HibernateException e) {
            throw new SPersistenceException(e);
        }
    }

    public void flushStatements() throws SPersistenceException {
        getSession().flush();
    }

    @Override
    public void delete(final PersistentObject entity) throws SPersistenceException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(
                    "Deleting instance of class " + entity.getClass().getSimpleName() + " with id=" + entity.getId());
        }
        final Session session = getSession();
        try {
            if (session.contains(entity)) {
                session.delete(entity);
            } else {
                final Class<? extends PersistentObject> mappedClass = getMappedClass(entity.getClass());
                final Serializable id = (entity instanceof PlatformPersistentObject)
                        ? entity.getId()
                        : new PersistentObjectId(entity.getId(), getTenantId());
                // Deletion must be performed on the session entity and not on a potential transitional entity.
                final Object pe = session.get(mappedClass, id);
                session.delete(pe);
            }
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final STenantIdNotSetException | HibernateException e) {
            throw new SPersistenceException(e);
        }
    }

    @Override
    public int update(final String updateQueryName) throws SPersistenceException {
        return update(updateQueryName, null);
    }

    @Override
    public int update(final String updateQueryName, final Map<String, Object> inputParameters)
            throws SPersistenceException {
        final Query query = getSession().getNamedQuery(updateQueryName);
        try {
            if (inputParameters != null) {
                setParameters(query, inputParameters);
            }

            return query.executeUpdate();
        } catch (final HibernateException he) {
            throw new SPersistenceException(he);
        }
    }

    @Override
    public void deleteAll(final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
        final Class<? extends PersistentObject> mappedClass = getMappedClass(entityClass);
        final Query query = getSession().getNamedQuery("deleteAll" + mappedClass.getSimpleName());
        try {
            query.executeUpdate();
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final HibernateException he) {
            throw new SPersistenceException(he);
        }
    }

    @Override
    public void insert(final PersistentObject entity) throws SPersistenceException {
        if (!(entity instanceof PlatformPersistentObject)) {
            setTenant(entity);
        }
        final Class<? extends PersistentObject> entityClass = entity.getClass();
        checkClassMapping(entityClass);
        final Session session = getSession();
        setId(entity);
        try {
            session.save(entity);
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final HibernateException he) {
            throw new SPersistenceException(he);
        }
    }

    @Override
    public void insertInBatch(final List<? extends PersistentObject> entities) throws SPersistenceException {
        for (final PersistentObject entity : entities) {
            if (!(entity instanceof PlatformPersistentObject)) {
                setTenant(entity);
            }
        }
        if (!entities.isEmpty()) {
            final Session session = getSession();
            for (final PersistentObject entity : entities) {
                final Class<? extends PersistentObject> entityClass = entity.getClass();
                checkClassMapping(entityClass);
                setId(entity);
                session.save(entity);
            }
        }
    }

    @Override
    public void update(final UpdateDescriptor updateDescriptor) throws SPersistenceException {
        // FIXME: deal with disconnected objects:
        final Class<? extends PersistentObject> entityClass = updateDescriptor.getEntity().getClass();
        checkClassMapping(entityClass);
        final PersistentObject entity = updateDescriptor.getEntity();
        final Session session = getSession();
        if (!session.contains(entity)) {
            throw new SPersistenceException("The object cannot be updated because it's disconnected " + entity);
        }
        for (final Map.Entry<String, Object> field : updateDescriptor.getFields().entrySet()) {
            setField(entity, field.getKey(), field.getValue());
        }
    }

    private void setField(final PersistentObject entity, final String fieldName, final Object parameterValue)
            throws SPersistenceException {
        Long id = null;
        try {
            id = entity.getId();
            ClassReflector.setField(entity, fieldName, parameterValue);
        } catch (final Exception e) {
            throw new SPersistenceException("Problem while updating entity: " + entity + " with id: " + id, e);
        }
    }

    @Override
    public <T> T selectOne(final SelectOneDescriptor<T> selectDescriptor) throws SBonitaReadException {
        try {
            return selectOne(getSession(), selectDescriptor);
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    Class<? extends PersistentObject> getMappedClass(final Class<? extends PersistentObject> entityClass)
            throws SPersistenceException {
        if (classMapping.contains(entityClass)) {
            return entityClass;
        }
        throw new SPersistenceException("Unable to locate class " + entityClass + " in Hibernate configuration");
    }

    private void checkClassMapping(final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
        if (!classMapping.contains(entityClass) && !mappingExclusions.contains(entityClass.getName())) {
            throw new SPersistenceException("Unable to locate class " + entityClass + " in Hibernate configuration");
        }
    }

    @Override
    public <T extends PersistentObject> T selectById(final SelectByIdDescriptor<T> selectDescriptor)
            throws SBonitaReadException {
        try {
            final Session session = getSession();
            final T object = this.selectById(session, selectDescriptor);
            if (selectDescriptor.isReadOnly()) {
                disconnectEntityFromSession(session, object);
            }
            return object;
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    private static <T> void disconnectEntityFromSession(Session session, T object) {
        if (object != null) {
            session.evict(object);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentObject> T selectObjectById(final Session session,
            final SelectByIdDescriptor<T> selectDescriptor)
            throws SBonitaReadException {
        Class<? extends PersistentObject> mappedClass;
        try {
            mappedClass = getMappedClass(selectDescriptor.getEntityType());
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e);
        }
        try {
            return (T) session.get(mappedClass, selectDescriptor.getId());
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final HibernateException he) {
            throw new SBonitaReadException(he);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentObject> T selectById(final Session session, final SelectByIdDescriptor<T> selectDescriptor)
            throws SBonitaReadException {
        if (PlatformPersistentObject.class.isAssignableFrom(selectDescriptor.getEntityType())) {
            return selectObjectById(session, selectDescriptor);
        }
        try {
            final PersistentObjectId id = new PersistentObjectId(selectDescriptor.getId(), getTenantId());
            Class<? extends PersistentObject> mappedClass = getMappedClass(selectDescriptor.getEntityType());
            return (T) session.get(mappedClass, id);
        } catch (final STenantIdNotSetException e) {
            return selectObjectById(session, selectDescriptor);
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final SPersistenceException | HibernateException e) {
            throw new SBonitaReadException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T selectOne(final Session session, final SelectOneDescriptor<T> selectDescriptor)
            throws SBonitaReadException {
        try {
            checkClassMapping(selectDescriptor.getEntityType());
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e);
        }
        final Query query = session.getNamedQuery(selectDescriptor.getQueryName());
        setQueryCache(query, selectDescriptor.getQueryName());
        final Map<String, Object> parameters = selectDescriptor.getInputParameters();
        if (parameters != null) {
            setParameters(query, parameters);
        }
        query.setMaxResults(1);
        try {
            return disconnectIfReadOnly((T) query.uniqueResult(), query, session);
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final HibernateException he) {
            throw new SBonitaReadException(he);
        }
    }

    private boolean isCacheEnabled(String queryName) {
        return cacheQueries != null && cacheQueries.containsKey(queryName);
    }

    private void setQueryCache(final Query query, final String name) {
        if (isCacheEnabled(name)) {
            query.setCacheable(true);
        }
    }

    @Override
    public <T> List<T> selectList(final SelectListDescriptor<T> selectDescriptor) throws SBonitaReadException {
        try {
            final Class<? extends PersistentObject> entityClass = selectDescriptor.getEntityType();
            checkClassMapping(entityClass);

            final Session session = getSession();

            org.hibernate.query.Query query = queryBuilderFactory.createQueryBuilderFor(session, selectDescriptor)
                    .cache(isCacheEnabled(selectDescriptor.getQueryName()))
                    .build();

            @SuppressWarnings("unchecked")
            final List<T> list = query.list();
            if (list != null) {
                disconnectIfReadOnly(list, query, session);
                return list;
            }
            return Collections.emptyList();
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final HibernateException | SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    private static <T> void disconnectIfReadOnly(List<T> list, Query query, Session session) {
        if (query.isReadOnly()) {
            for (T t : list) {
                disconnectEntityFromSession(session, t);
            }
        }
    }

    private static <T> T disconnectIfReadOnly(T object, Query query, Session session) {
        if (query.isReadOnly()) {
            disconnectEntityFromSession(session, object);
        }
        return object;
    }

    private void setParameters(final Query query, final Map<String, Object> inputParameters) {
        for (final Map.Entry<String, Object> entry : inputParameters.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof Collection<?>) {
                query.setParameterList(entry.getKey(), (Collection<?>) value);
            } else {
                query.setParameter(entry.getKey(), value);
            }
        }
    }

    public Map<String, String> getClassAliasMappings() {
        return classAliasMappings;
    }

    @Override
    public void delete(final long id, final Class<? extends PersistentObject> entityClass)
            throws SPersistenceException {
        final Class<? extends PersistentObject> mappedClass = getMappedClass(entityClass);
        final Query query = getSession().getNamedQuery("delete" + mappedClass.getSimpleName());
        query.setParameter("id", id);
        try {
            query.executeUpdate();
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final HibernateException he) {
            throw new SPersistenceException(he);
        }
    }

    @Override
    public void delete(final List<Long> ids, final Class<? extends PersistentObject> entityClass)
            throws SPersistenceException {
        final Class<? extends PersistentObject> mappedClass = getMappedClass(entityClass);
        final Query query = getSession().getNamedQuery("deleteByIds" + mappedClass.getSimpleName());
        query.setParameterList("ids", ids);
        try {
            query.executeUpdate();
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final HibernateException sse) {
            throw new SPersistenceException(sse);
        }
    }

    @PreDestroy
    public void destroy() {
        getLogger().info(
                "Closing Hibernate session factory of " + getClass().getName());
        sessionFactory.close();
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

    protected Logger getLogger() {
        return log;
    }

    protected long getTenantId() throws STenantIdNotSetException {
        return sessionAccessor.getTenantId();
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
                    getClassAliasMappings(), '%'/* there is no 'like' in these delete queries */)
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
