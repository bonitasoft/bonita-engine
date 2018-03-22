/**
 * Copyright (C) 2015-2017 BonitaSoft S.A.
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
package org.bonitasoft.engine.persistence;

import static org.bonitasoft.engine.services.Vendor.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleStateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.mapping.PersistentClass;
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

    private final OrderByCheckingMode orderByCheckingMode;

    private final Map<String, String> classAliasMappings;

    private final Map<String, String> cacheQueries;

    private final List<Class<? extends PersistentObject>> classMapping;

    protected final Map<String, Class<? extends PersistentObject>> interfaceToClassMapping;

    private final List<String> mappingExclusions;
    final OrderByBuilder orderByBuilder;
    private Statistics statistics;
    private int stat_display_count;
    private QueryBuilderFactory queryBuilderFactory = new QueryBuilderFactory();

    protected AbstractHibernatePersistenceService(final SessionFactory sessionFactory, final List<Class<? extends PersistentObject>> classMapping,
            final Map<String, String> classAliasMappings, final boolean enableWordSearch,
            final Set<String> wordSearchExclusionMappings, final TechnicalLoggerService logger)
            throws ClassNotFoundException {
        super("TEST", '#', enableWordSearch, wordSearchExclusionMappings, logger);
        this.sessionFactory = sessionFactory;
        this.classAliasMappings = classAliasMappings;
        this.classMapping = classMapping;
        orderByCheckingMode = getOrderByCheckingMode();
        statistics = sessionFactory.getStatistics();
        cacheQueries = Collections.emptyMap();
        interfaceToClassMapping = Collections.emptyMap();
        mappingExclusions = Collections.emptyList();
        orderByBuilder = new DefaultOrderByBuilder();
    }

    public AbstractHibernatePersistenceService(final String name, final HibernateConfigurationProvider hbmConfigurationProvider,
            final Properties extraHibernateProperties, final char likeEscapeCharacter, final TechnicalLoggerService logger,
            final SequenceManager sequenceManager, final DataSource datasource, final boolean enableWordSearch, final Set<String> wordSearchExclusionMappings)
            throws SPersistenceException, ClassNotFoundException {
        super(name, likeEscapeCharacter, sequenceManager, datasource, enableWordSearch,
                wordSearchExclusionMappings, logger);
        orderByCheckingMode = getOrderByCheckingMode();
        Configuration configuration;
        try {
            configuration = hbmConfigurationProvider.getConfiguration();
            if (extraHibernateProperties != null) {
                configuration.addProperties(extraHibernateProperties);
            }
        } catch (final ConfigurationException e) {
            throw new SPersistenceException(e);
        }

        final String dialect = configuration.getProperty("hibernate.dialect");
        OrderByBuilder orderByBuilder = new DefaultOrderByBuilder();

        if (dialect != null) {
            if (dialect.toLowerCase().contains("postgresql")) {
                configuration.setInterceptor(new PostgresInterceptor());
                configuration.registerTypeOverride(new PostgresMaterializedBlobType());
                configuration.registerTypeOverride(new PostgresMaterializedClobType());
                configuration.registerTypeOverride(new PostgresXMLType());
                queryBuilderFactory.setVendor(POSTGRES);
            } else if (dialect.toLowerCase().contains("sqlserver")) {
                SQLServerInterceptor sqlServerInterceptor = new SQLServerInterceptor();
                configuration.setInterceptor(sqlServerInterceptor);
                configuration.registerTypeOverride(new XMLType());
                orderByBuilder = new SQLServerOrderByBuilder();
                queryBuilderFactory.setVendor(SQLSERVER);
            } else if (dialect.toLowerCase().contains("oracle")) {
                configuration.registerTypeOverride(new XMLType());
                queryBuilderFactory.setVendor(ORACLE);
            } else if (dialect.toLowerCase().contains("mysql")) {
                configuration.registerTypeOverride(new XMLType());
                queryBuilderFactory.setVendor(MYSQL);
            }else{
                configuration.registerTypeOverride(new XMLType());
            }
        }

        this.orderByBuilder = orderByBuilder;
        final String className = configuration.getProperty("hibernate.interceptor");
        if (className != null && !className.isEmpty()) {
            try {
                final Interceptor interceptor = (Interceptor) Class.forName(className).newInstance();
                configuration.setInterceptor(interceptor);
            } catch (final ClassNotFoundException | IllegalAccessException | InstantiationException cnfe) {
                throw new SPersistenceException(cnfe);
            }

        }

        sessionFactory = configuration.buildSessionFactory();
        statistics = sessionFactory.getStatistics();

        final Iterator<PersistentClass> classMappingsIterator = configuration.getClassMappings();
        classMapping = new ArrayList<>();
        while (classMappingsIterator.hasNext()) {
            classMapping.add(classMappingsIterator.next().getMappedClass());
        }

        classAliasMappings = hbmConfigurationProvider.getClassAliasMappings();

        interfaceToClassMapping = hbmConfigurationProvider.getInterfaceToClassMapping();

        mappingExclusions = hbmConfigurationProvider.getMappingExclusions();

        cacheQueries = hbmConfigurationProvider.getCacheQueries();
    }

    private OrderByCheckingMode getOrderByCheckingMode() {
        final String property = System.getProperty("sysprop.bonita.orderby.checking.mode");
        return property != null && !property.isEmpty() ? OrderByCheckingMode.valueOf(property) : OrderByCheckingMode.NONE;
    }

    /**
     * Log synthetic information about cache every 10.000 sessions, if hibernate.gather_statistics, is enabled.
     */
    private void logStats() {
        if (!statistics.isStatisticsEnabled()) {
            return;
        }
        if (stat_display_count == 10 || stat_display_count == 100 || stat_display_count == 1000 || stat_display_count % 10000 == 0) {
            final long query_cache_hit = statistics.getQueryCacheHitCount();
            final long query_cache_miss = statistics.getQueryCacheMissCount();
            final long query_cahe_put = statistics.getQueryCachePutCount();
            final long level_2_cache_hit = statistics.getSecondLevelCacheHitCount();
            final long level_2_cache_miss = statistics.getSecondLevelCacheMissCount();
            final long level_2_put = statistics.getSecondLevelCachePutCount();

            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "Query Cache Ratio "
                    + (int) ((double) query_cache_hit / (query_cache_hit + query_cache_miss) * 100) + "% " + query_cache_hit + " hits " + query_cache_miss
                    + " miss " + query_cahe_put + " puts");
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "2nd Level Cache Ratio "
                    + (int) ((double) level_2_cache_hit / (level_2_cache_hit + level_2_cache_miss) * 100) + "% " + level_2_cache_hit + " hits "
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
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
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
    public int update(final String updateQueryName, final Map<String, Object> inputParameters) throws SPersistenceException {
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

    private void setField(final PersistentObject entity, final String fieldName, final Object parameterValue) throws SPersistenceException {
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

    Class<? extends PersistentObject> getMappedClass(final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
        if (classMapping.contains(entityClass)) {
            return entityClass;
        }
        if (interfaceToClassMapping.containsKey(entityClass.getName())) {
            return interfaceToClassMapping.get(entityClass.getName());
        }
        throw new SPersistenceException("Unable to locate class " + entityClass + " in Hibernate configuration");
    }

    private void checkClassMapping(final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
        if (!classMapping.contains(entityClass) && !interfaceToClassMapping.containsKey(entityClass.getName())
                && !mappingExclusions.contains(entityClass.getName())) {
            throw new SPersistenceException("Unable to locate class " + entityClass + " in Hibernate configuration");
        }
    }

    @Override
    public <T extends PersistentObject> T selectById(final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
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
    <T extends PersistentObject> T selectById(final Session session, final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
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

    private void setQueryCache(final Query query, final String name) {
        if (cacheQueries != null && cacheQueries.containsKey(name)) {
            query.setCacheable(true);
        }
    }

    @Override
    public <T> List<T> selectList(final SelectListDescriptor<T> selectDescriptor) throws SBonitaReadException {
        try {
            final Class<? extends PersistentObject> entityClass = selectDescriptor.getEntityType();
            checkClassMapping(entityClass);

            final Session session = getSession(true);

            Query query = session.getNamedQuery(selectDescriptor.getQueryName());
            QueryBuilder queryBuilder = queryBuilderFactory.createQueryBuilderFor(query, selectDescriptor.getEntityType(), orderByBuilder,
                    classAliasMappings, interfaceToClassMapping,
                    likeEscapeCharacter);
            if (selectDescriptor.hasAFilter()) {
                final QueryOptions queryOptions = selectDescriptor.getQueryOptions();
                final boolean enableWordSearch = isWordSearchEnabled(selectDescriptor.getEntityType());
                queryBuilder.appendFilters(queryOptions.getFilters(), queryOptions.getMultipleFilter(), enableWordSearch);
            }
            if (selectDescriptor.hasOrderByParameters()) {
                queryBuilder.appendOrderByClause(selectDescriptor.getQueryOptions().getOrderByOptions(), selectDescriptor.getEntityType());
            }

            if (queryBuilder.hasChanged()) {
                query = queryBuilder.buildQuery(session);
            }
            setQueryCache(query, selectDescriptor.getQueryName());
            try {
                queryBuilder.setTenantId(query, getTenantId());
            } catch (STenantIdNotSetException e) {
                throw new SBonitaReadException(e);
            }
            setParameters(query, selectDescriptor.getInputParameters());
            query.setFirstResult(selectDescriptor.getStartIndex());
            query.setMaxResults(selectDescriptor.getPageSize());

            checkOrderByClause(query);

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

    private void checkOrderByClause(final Query query) {
        if (!query.getQueryString().toLowerCase().contains("order by")) {
            switch (orderByCheckingMode) {
                case NONE:
                    break;
                case WARNING:
                    logger.log(
                            AbstractHibernatePersistenceService.class,
                            TechnicalLogSeverity.WARNING,
                            "Query '"
                                    + query.getQueryString()
                                    + "' does not contain 'ORDER BY' clause. It's better to modify your query to order the result, especially if you use the pagination.");
                    break;
                case STRICT:
                default:
                    throw new IllegalArgumentException("Query " + query.getQueryString()
                            + " does not contain 'ORDER BY' clause hence is not allowed. Please specify ordering before re-sending the query");
            }
        }
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
    public void delete(final long id, final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
        final Class<? extends PersistentObject> mappedClass = getMappedClass(entityClass);
        final Query query = getSession(true).getNamedQuery("delete" + mappedClass.getSimpleName());
        query.setLong("id", id);
        try {
            query.executeUpdate();
        } catch (final AssertionFailure | LockAcquisitionException | StaleStateException e) {
            throw new SRetryableException(e);
        } catch (final HibernateException he) {
            throw new SPersistenceException(he);
        }
    }

    @Override
    public void delete(final List<Long> ids, final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
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
        logger.log(getClass(), TechnicalLogSeverity.INFO, "Closing Hibernate session factory of " + getClass().getName());
        sessionFactory.close();
    }
}
