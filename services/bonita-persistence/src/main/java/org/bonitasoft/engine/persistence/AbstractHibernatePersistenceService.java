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
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.bonitasoft.engine.services.Vendor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.hibernate.*;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.query.Query;
import org.hibernate.stat.Statistics;

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
public abstract class AbstractHibernatePersistenceService extends AbstractDBPersistenceService {

    private final SessionFactory sessionFactory;

    private final Map<String, String> classAliasMappings;

    private final Map<String, String> cacheQueries;

    private final List<Class<? extends PersistentObject>> classMapping;

    private final List<String> mappingExclusions;
    private Statistics statistics;
    private int stat_display_count;
    private QueryBuilderFactory queryBuilderFactory;

    protected AbstractHibernatePersistenceService(final SessionFactory sessionFactory,
            final List<Class<? extends PersistentObject>> classMapping,
            final Map<String, String> classAliasMappings, final boolean enableWordSearch,
            final Set<String> wordSearchExclusionMappings)
            throws Exception {
        super("TEST");
        this.sessionFactory = sessionFactory;
        this.classAliasMappings = classAliasMappings;
        this.classMapping = classMapping;
        statistics = sessionFactory.getStatistics();
        cacheQueries = Collections.emptyMap();
        mappingExclusions = Collections.emptyList();
        queryBuilderFactory = new QueryBuilderFactory(OrderByCheckingMode.NONE, classAliasMappings, '#',
                enableWordSearch, wordSearchExclusionMappings);
    }

    public AbstractHibernatePersistenceService(final String name,
            final HibernateConfigurationProvider hbmConfigurationProvider,
            final Properties extraHibernateProperties,
            final SequenceManager sequenceManager, final DataSource datasource, QueryBuilderFactory queryBuilderFactory)
            throws Exception {
        super(name, sequenceManager, datasource);
        hbmConfigurationProvider.bootstrap(extraHibernateProperties);
        sessionFactory = hbmConfigurationProvider.getSessionFactory();

        this.queryBuilderFactory = queryBuilderFactory;
        if (hbmConfigurationProvider.getVendor() == Vendor.SQLSERVER) {
            this.queryBuilderFactory.setOrderByBuilder(new SQLServerOrderByBuilder());
        }
        statistics = sessionFactory.getStatistics();
        classMapping = hbmConfigurationProvider.getMappedClasses();
        classAliasMappings = hbmConfigurationProvider.getClassAliasMappings();
        mappingExclusions = hbmConfigurationProvider.getMappingExclusions();
        cacheQueries = hbmConfigurationProvider.getCacheQueries();
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

    protected Session getSession(final boolean useTenant) throws SPersistenceException {
        logStats();
        try {
            return sessionFactory.getCurrentSession();
        } catch (final HibernateException e) {
            throw new SPersistenceException(e);
        }
    }

    void flushStatements(final boolean useTenant) throws SPersistenceException {
        final Session session = getSession(useTenant);
        session.flush();
    }

    @Override
    public void delete(final PersistentObject entity) throws SPersistenceException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(
                    "Deleting instance of class " + entity.getClass().getSimpleName() + " with id=" + entity.getId());
        }
        final Class<? extends PersistentObject> mappedClass = getMappedClass(entity.getClass());
        final Session session = getSession(true);
        try {
            if (session.contains(entity)) {
                session.delete(entity);
            } else {
                // Deletion must be performed on the session entity and not on a potential transitional entity.
                final Object pe = session.get(mappedClass, new PersistentObjectId(entity.getId(), 0));
                session.delete(pe);
            }
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final HibernateException he) {
            throw new SPersistenceException(he);
        }
    }

    @Override
    public int update(final String updateQueryName) throws SPersistenceException {
        return update(updateQueryName, null);
    }

    @Override
    public int update(final String updateQueryName, final Map<String, Object> inputParameters)
            throws SPersistenceException {
        final Query query = getSession(true).getNamedQuery(updateQueryName);
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
        final Query query = getSession(true).getNamedQuery("deleteAll" + mappedClass.getSimpleName());
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
        final Class<? extends PersistentObject> entityClass = entity.getClass();
        checkClassMapping(entityClass);
        final Session session = getSession(true);
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
        if (!entities.isEmpty()) {
            final Session session = getSession(true);
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
        final Session session = getSession(false);
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
            return selectOne(getSession(true), selectDescriptor);
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
            final Session session = getSession(true);
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

    @Override
    protected void setId(PersistentObject entity) throws SPersistenceException {
        super.setId(entity);
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentObject> T selectById(final Session session, final SelectByIdDescriptor<T> selectDescriptor)
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

            final Session session = getSession(true);

            org.hibernate.query.Query query = queryBuilderFactory.createQueryBuilderFor(session, selectDescriptor)
                    .tenantId(getTenantId())
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
        } catch (final HibernateException | SPersistenceException | STenantIdNotSetException e) {
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
        final Query query = getSession(true).getNamedQuery("delete" + mappedClass.getSimpleName());
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
        final Query query = getSession(true).getNamedQuery("deleteByIds" + mappedClass.getSimpleName());
        query.setParameterList("ids", ids);
        try {
            query.executeUpdate();
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final HibernateException sse) {
            throw new SPersistenceException(sse);
        }
    }

    public void destroy() {
        getLogger().info(
                "Closing Hibernate session factory of " + getClass().getName());
        sessionFactory.close();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
