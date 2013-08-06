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

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.EnumToObjectConvertible;
import org.bonitasoft.engine.commons.IOUtil;
import org.bonitasoft.engine.commons.StringUtil;
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
import org.hibernate.stat.Statistics;

/**
 * Hibernate implementation of the persistence service
 * 
 * @author Charles Souillard
 * @author Nicolas Chabanoles
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class AbstractHibernatePersistenceService extends AbstractDBPersistenceService {

    private final SessionFactory sessionFactory;

    private final Map<String, String> classAliasMappings;

    protected final Map<String, String> cacheQueries;

    protected final List<Class<? extends PersistentObject>> classMapping;

    protected final Map<String, Class<? extends PersistentObject>> interfaceToClassMapping;

    protected final List<String> mappingExclusions;

    Statistics statistics;

    int stat_display_count;

    TechnicalLoggerService logger;

    @SuppressWarnings("unchecked")
    public AbstractHibernatePersistenceService(final String name, final HibernateConfigurationProvider hbmConfigurationProvider,
            final DBConfigurationsProvider tenantConfigurationsProvider, final String statementDelimiter, final String likeEscapeCharacter,
            final TechnicalLoggerService logger, final SequenceManager sequenceManager, final DataSource datasource) throws SPersistenceException {
        super(name, tenantConfigurationsProvider, statementDelimiter, likeEscapeCharacter, sequenceManager, datasource);
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
            } catch (InstantiationException e) {
                throw new SPersistenceException(e);
            } catch (IllegalAccessException e) {
                throw new SPersistenceException(e);
            }

        }

        sessionFactory = configuration.buildSessionFactory();
        statistics = sessionFactory.getStatistics();

        final Iterator<org.hibernate.mapping.PersistentClass> classMappingsIterator = configuration.getClassMappings();
        classMapping = new ArrayList<Class<? extends PersistentObject>>();
        while (classMappingsIterator.hasNext()) {
            classMapping.add(classMappingsIterator.next().getMappedClass());
        }

        classAliasMappings = hbmConfigurationProvider.getClassAliasMappings();

        interfaceToClassMapping = hbmConfigurationProvider.getInterfaceToClassMapping();

        mappingExclusions = hbmConfigurationProvider.getMappingExclusions();

        cacheQueries = hbmConfigurationProvider.getCacheQueries();
        this.logger = logger;
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
            final org.hibernate.classic.Session currentSession = sessionFactory.getCurrentSession();
            return currentSession;
        } catch (HibernateException e) {
            throw new SPersistenceException(e);
        }
    }

    protected void flushStatements(final boolean useTenant) throws SPersistenceException {
        final Session session = getSession(useTenant);
        session.flush();
    }

    @Override
    public void delete(final PersistentObject entity) throws SPersistenceException {
        logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                "Deleting instance of class " + entity.getClass().getSimpleName() + " with id=" + entity.getId());
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
    public void purge(final String classToPurge) throws SPersistenceException {
        final int index = classToPurge.lastIndexOf('.');
        String suffix = classToPurge;
        if (index != -1) {
            suffix = classToPurge.substring(index + 1, classToPurge.length());
        }
        final Query query = getSession(true).getNamedQuery("purge" + suffix);
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
            final String setterName = "set" + StringUtil.firstCharToUpperCase(fieldName);
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
            setPramaters(query, parameters);
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

    protected <T> String getQueryWithFilters(final String query, final List<FilterOption> filters, SearchFields multipleFilter) {
        final StringBuilder builder = new StringBuilder(query);
        final Set<String> specificFilters = new HashSet<String>(filters.size());
        FilterOption previousFilter = null;
        if (!filters.isEmpty()) {
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
            final Iterator<String> fieldIterator = fields.iterator();
            final List<String> terms = multipleFilter.getTerms();
            if (!fields.isEmpty()) {
                if (!builder.toString().contains("WHERE")) {
                    builder.append(" WHERE (");
                } else {
                    builder.append(" AND (");
                }
                while (fieldIterator.hasNext()) {
                    final Iterator<String> termIterator = terms.iterator();
                    final String currentField = fieldIterator.next();
                    while (termIterator.hasNext()) {
                        final String currentTerm = termIterator.next();
                        builder.append(currentField).append(getLikeEscapeClause(currentTerm));
                        if (termIterator.hasNext() || fieldIterator.hasNext()) {
                            builder.append(" OR ");
                        }
                    }
                }
                builder.append(")");
            }
        }
        return builder.toString();
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
                // TODO:write IN
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

            if (selectDescriptor.hasAFilter()) {
                final QueryOptions queryOptions = selectDescriptor.getQueryOptions();
                query = session.createQuery(getQueryWithFilters(query.getQueryString(), queryOptions.getFilters(), queryOptions.getMultipleFilter()));
            }
            if (selectDescriptor.hasOrderByParameters()) {
                query = session.createQuery(getQueryWithOrderByClause(query.getQueryString(), selectDescriptor));
            }
            setQueryCache(query, selectDescriptor.getQueryName());

            if (selectDescriptor != null) {
                setPramaters(query, selectDescriptor.getInputParameters());
            }
            query.setFirstResult(selectDescriptor.getStartIndex());
            query.setMaxResults(selectDescriptor.getPageSize());

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

    protected void setPramaters(Query query, final Map<String, Object> inputParameters) {
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

        logger.log(getClass(), TechnicalLogSeverity.TRACE, "Processing SQL resource : " + sqlResource);
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
}
