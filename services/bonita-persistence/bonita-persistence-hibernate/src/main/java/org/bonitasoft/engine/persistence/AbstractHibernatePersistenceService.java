/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import static org.bonitasoft.engine.persistence.search.FilterOperationType.L_PARENTHESIS;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.R_PARENTHESIS;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.isNormalOperator;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.text.WordUtils;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.EnumToObjectConvertible;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
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
 */
public abstract class AbstractHibernatePersistenceService extends AbstractDBPersistenceService {

    private final SessionFactory sessionFactory;

    private final OrderByCheckingMode orderByCheckingMode;

    private final Map<String, String> classAliasMappings;

    protected final Map<String, String> cacheQueries;

    protected final List<Class<? extends PersistentObject>> classMapping;

    protected final Map<String, Class<? extends PersistentObject>> interfaceToClassMapping;

    protected final List<String> mappingExclusions;

    Statistics statistics;

    int stat_display_count;

    // ----

    /**
     * @param sessionFactory
     * @param classMapping
     * @param classAliasMappings
     * @param enableWordSearch
     * @param wordSearchExclusionMappings
     * @param logger
     * @throws ClassNotFoundException
     */
    protected AbstractHibernatePersistenceService(final SessionFactory sessionFactory, final List<Class<? extends PersistentObject>> classMapping,
            final Map<String, String> classAliasMappings, final boolean enableWordSearch,
            final Set<String> wordSearchExclusionMappings, final TechnicalLoggerService logger) throws ClassNotFoundException {
        super("TEST", ";", "#", enableWordSearch, wordSearchExclusionMappings, logger);
        this.sessionFactory = sessionFactory;
        this.classAliasMappings = classAliasMappings;
        this.classMapping = classMapping;
        orderByCheckingMode = getOrderByCheckingMode();
        statistics = sessionFactory.getStatistics();
        cacheQueries = Collections.emptyMap();
        interfaceToClassMapping = Collections.emptyMap();
        mappingExclusions = Collections.emptyList();
    }

    // Setter for session

    // ----

    /**
     * @param name
     * @param hbmConfigurationProvider
     * @param tenantConfigurationsProvider
     * @param statementDelimiter
     * @param likeEscapeCharacter
     * @param logger
     * @param sequenceManager
     * @param datasource
     * @param enableWordSearch
     * @param wordSearchExclusionMappings
     * @throws SPersistenceException
     * @throws ClassNotFoundException
     */
    public AbstractHibernatePersistenceService(final String name, final HibernateConfigurationProvider hbmConfigurationProvider,
            final DBConfigurationsProvider tenantConfigurationsProvider, final String statementDelimiter, final String likeEscapeCharacter,
            final TechnicalLoggerService logger, final SequenceManager sequenceManager, final DataSource datasource, final boolean enableWordSearch,
            final Set<String> wordSearchExclusionMappings) throws SPersistenceException, ClassNotFoundException {
        super(name, tenantConfigurationsProvider, statementDelimiter, likeEscapeCharacter, sequenceManager, datasource, enableWordSearch,
                wordSearchExclusionMappings, logger);
        orderByCheckingMode = getOrderByCheckingMode();
        Configuration configuration;
        try {
            configuration = hbmConfigurationProvider.getConfiguration();
        } catch (final ConfigurationException e) {
            throw new SPersistenceException(e);
        }
        final String dialect = configuration.getProperty("hibernate.dialect");
        if (dialect != null) {
            if (dialect.contains("PostgreSQL")) {
                configuration.setInterceptor(new PostgresInterceptor());
            } else if (dialect.contains("SQLServer")) {
                configuration.setInterceptor(new SQLServerInterceptor());
            }
        }
        final String className = configuration.getProperty("hibernate.interceptor");
        if (className != null && !className.isEmpty()) {
            try {
                final Interceptor interceptor = (Interceptor) Class.forName(className).newInstance();
                configuration.setInterceptor(interceptor);
            } catch (final ClassNotFoundException cnfe) {
                throw new SPersistenceException(cnfe);
            } catch (final InstantiationException e) {
                throw new SPersistenceException(e);
            } catch (final IllegalAccessException e) {
                throw new SPersistenceException(e);
            }

        }

        sessionFactory = configuration.buildSessionFactory();
        statistics = sessionFactory.getStatistics();

        final Iterator<PersistentClass> classMappingsIterator = configuration.getClassMappings();
        classMapping = new ArrayList<Class<? extends PersistentObject>>();
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
    protected void logStats() {
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

    protected void flushStatements(final boolean useTenant) throws SPersistenceException {
        final Session session = getSession(useTenant);
        session.flush();
    }

    protected <T> void logWarningMessage(final SelectListDescriptor<T> selectDescriptor, final Query query) {
        final StringBuilder message = new StringBuilder();
        message.append("selectList call without \"order by\" clause ");
        message.append("\n");
        message.append(query.toString());
        message.append("\n");
        message.append(String.format("query name:%s\nentity:%s\nstart index:%d\npage size:%d", selectDescriptor.getQueryName(), selectDescriptor
                .getEntityType().getCanonicalName(), selectDescriptor.getStartIndex(), selectDescriptor.getPageSize()));
        logger.log(this.getClass(), TechnicalLogSeverity.WARNING, message.toString());

        // throw new IllegalArgumentException("Query " + selectDescriptor.getQueryName());
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
        } catch (final AssertionFailure af) {
            throw new SRetryableException(af);
        } catch (final LockAcquisitionException lae) {
            throw new SRetryableException(lae);
        } catch (final StaleStateException sse) {
            throw new SRetryableException(sse);
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
        } catch (final AssertionFailure af) {
            throw new SRetryableException(af);
        } catch (final LockAcquisitionException lae) {
            throw new SRetryableException(lae);
        } catch (final StaleStateException sse) {
            throw new SRetryableException(sse);
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
        } catch (final AssertionFailure af) {
            throw new SRetryableException(af);
        } catch (final LockAcquisitionException lae) {
            throw new SRetryableException(lae);
        } catch (final StaleStateException sse) {
            throw new SRetryableException(sse);
        } catch (final HibernateException he) {
            throw new SPersistenceException(he);
        }
    }

    @Override
    public void insertInBatch(final List<PersistentObject> entities) throws SPersistenceException {
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
            throw new SPersistenceException("The object cannot be updated because it's deconnected " + entity);
        }
        for (final Map.Entry<String, Object> field : updateDescriptor.getFields().entrySet()) {
            setField(entity, field.getKey(), field.getValue());
        }
    }

    private void setField(final PersistentObject entity, final String fieldName, final Object parameterValue) throws SPersistenceException {
        Long id = null;
        try {
            id = entity.getId();
            final String setterName = "set" + WordUtils.capitalize(fieldName);
            ClassReflector.invokeMethodByName(entity, setterName, parameterValue);
        } catch (final Exception e) {
            throw new SPersistenceException("Problem while updating entity: " + entity + " with id: " + id, e);
        }
    }

    @Override
    public <T> T selectOne(final SelectOneDescriptor<T> selectDescriptor) throws SBonitaReadException {
        try {
            final Session session = getSession(true);
            return this.selectOne(session, selectDescriptor, selectDescriptor.getInputParameters());
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    protected Class<? extends PersistentObject> getMappedClass(final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
        if (classMapping.contains(entityClass)) {
            return entityClass;
        }
        if (interfaceToClassMapping.containsKey(entityClass.getName())) {
            return interfaceToClassMapping.get(entityClass.getName());
        }
        throw new SPersistenceException("Unable to locate class " + entityClass + " in Hibernate configuration");
    }

    protected void checkClassMapping(final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
        if (!classMapping.contains(entityClass) && !interfaceToClassMapping.containsKey(entityClass.getName())
                && !mappingExclusions.contains(entityClass.getName())) {
            throw new SPersistenceException("Unable to locate class " + entityClass + " in Hibernate configuration");
        }
    }

    @Override
    public <T extends PersistentObject> T selectById(final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
        try {
            return this.selectById(getSession(true), selectDescriptor);
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentObject> T selectById(final Session session, final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
        Class<? extends PersistentObject> mappedClass = null;
        try {
            mappedClass = getMappedClass(selectDescriptor.getEntityType());
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e);
        }
        try {
            return (T) session.get(mappedClass, selectDescriptor.getId());
        } catch (final AssertionFailure af) {
            throw new SRetryableException(af);
        } catch (final LockAcquisitionException lae) {
            throw new SRetryableException(lae);
        } catch (final StaleStateException sse) {
            throw new SRetryableException(sse);
        } catch (final HibernateException he) {
            throw new SBonitaReadException(he);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T selectOne(final Session session, final AbstractSelectDescriptor<T> selectDescriptor, final Map<String, Object> parameters)
            throws SBonitaReadException {
        try {
            checkClassMapping(selectDescriptor.getEntityType());
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e);
        }
        final Query query = session.getNamedQuery(selectDescriptor.getQueryName());
        setQueryCache(query, selectDescriptor.getQueryName());
        if (parameters != null) {
            setParameters(query, parameters);
        }
        query.setMaxResults(1);
        try {
            return (T) query.uniqueResult();
        } catch (final AssertionFailure af) {
            throw new SRetryableException(af);
        } catch (final LockAcquisitionException lae) {
            throw new SRetryableException(lae);
        } catch (final StaleStateException sse) {
            throw new SRetryableException(sse);
        } catch (final HibernateException he) {
            throw new SBonitaReadException(he);
        }
    }

    protected String getQueryWithFilters(final String query, final List<FilterOption> filters, final SearchFields multipleFilter, final boolean enableWordSearch) {
        final StringBuilder builder = new StringBuilder(query);
        final Set<String> specificFilters = new HashSet<String>(filters.size());
        if (!filters.isEmpty()) {
            FilterOption previousFilter = null;
            if (!query.contains("WHERE")) {
                builder.append(" WHERE (");
            } else {
                builder.append(" AND (");
            }
            for (final FilterOption filterOption : filters) {
                if (previousFilter != null) {
                    final FilterOperationType prevOp = previousFilter.getFilterOperationType();
                    final FilterOperationType currOp = filterOption.getFilterOperationType();
                    // Auto add AND if previous operator was normal op or ')' and that current op is normal op or '(' :
                    if ((isNormalOperator(prevOp) || prevOp == R_PARENTHESIS) && (isNormalOperator(currOp) || currOp == L_PARENTHESIS)) {
                        builder.append(" AND ");
                    }
                }
                final StringBuilder aliasBuilder = appendFilterClause(builder, filterOption);
                // FIXME: is it really filterOption.getFieldName() or is it its formatted value: classAliasMappings.get(.......) ?:
                // specificFilters.add(filterOption.getFieldName());
                if (aliasBuilder != null) {
                    specificFilters.add(aliasBuilder.toString());
                }
                previousFilter = filterOption;
            }
            builder.append(")");
        }
        if (multipleFilter != null && multipleFilter.getTerms() != null && !multipleFilter.getTerms().isEmpty()) {
            handleMultipleFilters(builder, multipleFilter, specificFilters, enableWordSearch);
        }
        return builder.toString();
    }

    /**
     * @param builder
     * @param multipleFilter
     * @param specificFilters
     * @param enableWordSearch
     */
    protected void handleMultipleFilters(final StringBuilder builder, final SearchFields multipleFilter, final Set<String> specificFilters,
            final boolean enableWordSearch) {
        final Map<Class<? extends PersistentObject>, Set<String>> allTextFields = multipleFilter.getFields();
        final Set<String> fields = new HashSet<String>();
        for (final Entry<Class<? extends PersistentObject>, Set<String>> entry : allTextFields.entrySet()) {
            final String alias = getClassAliasMappings().get(entry.getKey().getName());
            for (final String field : entry.getValue()) {
                final StringBuilder aliasBuilder = new StringBuilder(alias);
                aliasBuilder.append('.').append(field);
                fields.add(aliasBuilder.toString());
            }
        }
        fields.removeAll(specificFilters);

        if (!fields.isEmpty()) {
            final List<String> terms = multipleFilter.getTerms();
            applyFiltersOnQuery(builder, fields, terms, enableWordSearch);
        }
    }

    protected void applyFiltersOnQuery(final StringBuilder queryBuilder, final Set<String> fields, final List<String> terms, final boolean enableWordSearch) {
        if (!queryBuilder.toString().contains("WHERE")) {
            queryBuilder.append(" WHERE ");
        } else {
            queryBuilder.append(" AND ");
        }
        queryBuilder.append("(");

        final Iterator<String> fieldIterator = fields.iterator();
        while (fieldIterator.hasNext()) {
            buildLikeClauseForOneFieldMultipleTerms(queryBuilder, fieldIterator.next(), terms, enableWordSearch);
            if (fieldIterator.hasNext()) {
                queryBuilder.append(" OR ");
            }
        }

        queryBuilder.append(")");
    }

    /**
     * @param queryBuilder
     * @param currentField
     * @param terms
     * @param enableWordSearch
     */
    protected void buildLikeClauseForOneFieldMultipleTerms(final StringBuilder queryBuilder, final String currentField, final List<String> terms,
            final boolean enableWordSearch) {
        final Iterator<String> termIterator = terms.iterator();
        while (termIterator.hasNext()) {
            final String currentTerm = termIterator.next();

            buildLikeClauseForOneFieldOneTerm(queryBuilder, currentField, currentTerm, enableWordSearch);

            if (termIterator.hasNext()) {
                queryBuilder.append(" OR ");
            }
        }
    }

    /**
     * @param queryBuilder
     * @param currentField
     * @param currentTerm
     * @param enableWordSearch
     */
    protected void buildLikeClauseForOneFieldOneTerm(final StringBuilder queryBuilder, final String currentField, final String currentTerm,
            final boolean enableWordSearch) {
        // Search if a sentence starts with the term
        queryBuilder.append(currentField).append(buildLikeEscapeClause(currentTerm, "", "%"));

        if (enableWordSearch) {
            // Search also if a word starts with the term
            // We do not want to search for %currentTerm% to ensure we can use Lucene-like library.
            queryBuilder.append(" OR ").append(currentField).append(buildLikeEscapeClause(currentTerm, "% ", "%"));
        }
    }

    private <T> String getQueryWithOrderByClause(final String query, final SelectListDescriptor<T> selectDescriptor) throws SBonitaReadException {
        final StringBuilder builder = new StringBuilder(query);
        appendOrderByClause(builder, selectDescriptor);
        return builder.toString();
    }

    private StringBuilder appendFilterClause(final StringBuilder clause, final FilterOption filterOption) {
        final FilterOperationType type = filterOption.getFilterOperationType();
        StringBuilder completeField = null;
        if (filterOption.getPersistentClass() != null) {
            completeField = new StringBuilder(getClassAliasMappings().get(filterOption.getPersistentClass().getName())).append('.').append(
                    filterOption.getFieldName());
        }
        Object fieldValue = filterOption.getValue();
        if (fieldValue instanceof String) {
            fieldValue = "'" + fieldValue + "'";
        } else if (fieldValue instanceof EnumToObjectConvertible) {
            fieldValue = ((EnumToObjectConvertible) fieldValue).fromEnum();
        }
        switch (type) {
            case EQUALS:
                if (fieldValue == null) {
                    clause.append(completeField).append(" IS NULL");
                } else {
                    clause.append(completeField).append(" = ").append(fieldValue);
                }
                break;
            case GREATER:
                clause.append(completeField).append(" > ").append(fieldValue);
                break;
            case GREATER_OR_EQUALS:
                clause.append(completeField).append(" >= ").append(fieldValue);
                break;
            case LESS:
                clause.append(completeField).append(" < ").append(fieldValue);
                break;
            case LESS_OR_EQUALS:
                clause.append(completeField).append(" <= ").append(fieldValue);
                break;
            case DIFFERENT:
                clause.append(completeField).append(" != ").append(fieldValue);
                break;
            case IN:
                clause.append(getInClause(completeField, filterOption));
                break;
            case BETWEEN:
                final Object from = filterOption.getFrom() instanceof String ? "'" + filterOption.getFrom() + "'" : filterOption.getFrom();
                final Object to = filterOption.getTo() instanceof String ? "'" + filterOption.getTo() + "'" : filterOption.getTo();
                clause.append("(").append(from).append(" <= ").append(completeField);
                clause.append(" AND ").append(completeField).append(" <= ").append(to).append(")");
                break;
            case LIKE:
                // TODO:write LIKE
                clause.append(completeField).append(" LIKE '%").append(filterOption.getValue()).append("%'");
                break;
            case L_PARENTHESIS:
                clause.append(" (");
                break;
            case R_PARENTHESIS:
                clause.append(" )");
                break;
            case AND:
                clause.append(" AND ");
                break;
            case OR:
                clause.append(" OR ");
                break;
            default:
                // TODO:do we want default behaviour?
                break;
        }
        return completeField;
    }

    private String getInClause(final StringBuilder completeField, final FilterOption filterOption) {
        final StringBuilder stb = new StringBuilder(completeField);
        stb.append(" in (");
        stb.append(getInValues(filterOption));
        stb.append(")");
        return stb.toString();
    }

    private String getInValues(final FilterOption filterOption) {
        final StringBuilder stb = new StringBuilder();
        for (final Object element : filterOption.getIn()) {
            stb.append(element + ",");
        }
        final String inValues = stb.toString();
        return inValues.substring(0, inValues.length() - 1);
    }

    private <T> void appendOrderByClause(final StringBuilder builder, final SelectListDescriptor<T> selectDescriptor) throws SBonitaReadException {
        builder.append(" ORDER BY ");
        boolean startWithComma = false;
        boolean sortedById = false;
        for (final OrderByOption orderByOption : selectDescriptor.getQueryOptions().getOrderByOptions()) {
            if (startWithComma) {
                builder.append(',');
            }
            final Class<? extends PersistentObject> clazz = orderByOption.getClazz();
            if (clazz != null) {
                appendClassAlias(builder, clazz);
            }
            final String fieldName = orderByOption.getFieldName();
            if ("id".equalsIgnoreCase(fieldName) || "sourceObjectId".equalsIgnoreCase(fieldName)) {
                sortedById = true;
            }
            builder.append(fieldName);
            builder.append(' ');
            builder.append(orderByOption.getOrderByType().toString());
            startWithComma = true;
        }
        if (!sortedById) {
            if (startWithComma) {
                builder.append(',');
            }
            appendClassAlias(builder, selectDescriptor.getEntityType());
            builder.append("id");
            builder.append(' ');
            builder.append("ASC");
        }
    }

    private void appendClassAlias(final StringBuilder builder, final Class<? extends PersistentObject> clazz) throws SBonitaReadException {
        final String className = clazz.getName();
        final String classAlias = getClassAliasMappings().get(className);
        if (classAlias == null || classAlias.trim().isEmpty()) {
            throw new SBonitaReadException("No class alias found for class " + className);
        }
        builder.append(classAlias);
        builder.append('.');
    }

    protected void setQueryCache(final Query query, final String name) {
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
            String builtQuery = query.getQueryString();

            if (selectDescriptor.hasAFilter()) {
                final QueryOptions queryOptions = selectDescriptor.getQueryOptions();
                final boolean enableWordSearch = isWordSearchEnabled(selectDescriptor.getEntityType());
                builtQuery = getQueryWithFilters(builtQuery, queryOptions.getFilters(), queryOptions.getMultipleFilter(), enableWordSearch);
            }

            if (selectDescriptor.hasOrderByParameters()) {
                builtQuery = getQueryWithOrderByClause(builtQuery, selectDescriptor);
            }

            if (!builtQuery.equals(query.getQueryString())) {
                query = session.createQuery(builtQuery);
            }
            setQueryCache(query, selectDescriptor.getQueryName());
            setParameters(query, selectDescriptor.getInputParameters());
            query.setFirstResult(selectDescriptor.getStartIndex());
            query.setMaxResults(selectDescriptor.getPageSize());

            checkOrderByClause(query);

            @SuppressWarnings("unchecked")
            final List<T> list = query.list();
            if (list != null) {
                return list;
            }
            return Collections.emptyList();
        } catch (final AssertionFailure af) {
            throw new SRetryableException(af);
        } catch (final LockAcquisitionException lae) {
            throw new SRetryableException(lae);
        } catch (final StaleStateException sse) {
            throw new SRetryableException(sse);
        } catch (final HibernateException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
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

    protected void setParameters(final Query query, final Map<String, Object> inputParameters) {
        for (final Map.Entry<String, Object> entry : inputParameters.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof Collection<?>) {
                query.setParameterList(entry.getKey(), (Collection<?>) value);
            } else {
                query.setParameter(entry.getKey(), value);
            }
        }
    }

    @Override
    protected void doExecuteSQL(final String sqlResource, final String statementDelimiter, final Map<String, String> replacements,
            final boolean useDataSourceConnection) throws SPersistenceException, IOException {
        final URL url = this.getClass().getResource(sqlResource);
        if (url == null) {
            throw new IOException("SQL file not found, path=" + sqlResource);
        }
        final String fileContent = new String(IOUtil.getAllContentFrom(url));

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Processing SQL resource : " + sqlResource);
        }
        final String regex = statementDelimiter.concat("\r?\n");
        final List<String> commands = new ArrayList<String>();
        final String[] tmp = fileContent.split(regex);
        for (final String command : tmp) {
            final String filledCommand = fillTemplate(replacements, command);
            if (!filledCommand.isEmpty()) {
                commands.add(filledCommand);
            }
        }
        if (commands.isEmpty()) {
            return;
        }
        final int lastIndex = commands.size() - 1;
        String lastCommand = commands.get(lastIndex);
        final int index = lastCommand.lastIndexOf(statementDelimiter);
        if (index > 0) {
            lastCommand = lastCommand.substring(0, index);
            commands.remove(lastIndex);
            commands.add(lastCommand);
        }

        if (useDataSourceConnection) {
            doExecuteSQLThroughJDBC(commands);
        } else {
            doExecuteSQLThroughHibernate(sqlResource, commands);
        }
    }

    private void doExecuteSQLThroughHibernate(final String sqlResource, final List<String> commands) throws SPersistenceException {
        final Session session = getSession(false);
        for (final String command : commands) {
            try {
                session.createSQLQuery(command).executeUpdate();// FIXME autocommit
                // session.flush();
                // session.clear(); // lvaills : Why clearing the session ? the tx is not committed.
            } catch (final AssertionFailure af) {
                throw new SRetryableException("Unable to execute command of file " + sqlResource + " content:\n " + command, af);
            } catch (final LockAcquisitionException lae) {
                throw new SRetryableException("Unable to execute command of file " + sqlResource + " content:\n " + command, lae);
            } catch (final StaleStateException sse) {
                throw new SRetryableException("Unable to execute command of file " + sqlResource + " content:\n " + command, sse);
            } catch (final HibernateException e) {
                throw new SPersistenceException("Unable to execute command of file " + sqlResource + " content:\n " + command, e);
            }
        }
    }

    private void doExecuteSQLThroughJDBC(final List<String> commands) throws SPersistenceException {
        try {
            final Connection connection = datasource.getConnection();
            connection.setAutoCommit(false);
            try {
                for (final String command : commands) {
                    final Statement stmt = connection.createStatement();
                    try {
                        stmt.execute(command);
                    } catch (final SQLException e) {
                        logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Following SQL command failed: " + command);
                        throw e;
                    } finally {
                        stmt.close();
                    }
                }
                connection.commit();
            } catch (final SQLException sqe) {
                connection.rollback();
                throw sqe;
            } finally {
                connection.close();
            }
        } catch (final SQLException e) {
            throw new SPersistenceException(e);
        }
    }

    private String fillTemplate(final Map<String, String> replacements, final String command) {
        String trimmedCommand = command.trim();
        if (trimmedCommand.isEmpty() || replacements == null) {
            return trimmedCommand;
        }

        for (final Map.Entry<String, String> tableMapping : replacements.entrySet()) {
            final String stringToReplace = tableMapping.getKey();
            final String value = tableMapping.getValue();
            trimmedCommand = trimmedCommand.replaceAll(stringToReplace, value);
        }
        return trimmedCommand;
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
        } catch (final AssertionFailure af) {
            throw new SRetryableException(af);
        } catch (final LockAcquisitionException lae) {
            throw new SRetryableException(lae);
        } catch (final StaleStateException sse) {
            throw new SRetryableException(sse);
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
        } catch (final AssertionFailure af) {
            throw new SRetryableException(af);
        } catch (final LockAcquisitionException lae) {
            throw new SRetryableException(lae);
        } catch (final StaleStateException sse) {
            throw new SRetryableException(sse);
        } catch (final HibernateException he) {
            throw new SPersistenceException(he);
        }
    }

    public void destroy() {
        logger.log(getClass(), TechnicalLogSeverity.INFO, "Closing Hibernate session factory of " + getClass().getName());
        sessionFactory.close();
    }
}
