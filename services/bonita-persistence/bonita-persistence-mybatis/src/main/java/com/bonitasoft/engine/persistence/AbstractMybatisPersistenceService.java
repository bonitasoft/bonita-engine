/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import static org.bonitasoft.engine.persistence.search.FilterOperationType.L_PARENTHESIS;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.R_PARENTHESIS;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.isNormalOperator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.EnumToObjectConvertible;
import org.bonitasoft.engine.commons.IOUtil;
import org.bonitasoft.engine.commons.StringUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.AbstractDBPersistenceService;
import org.bonitasoft.engine.persistence.AbstractSelectDescriptor;
import org.bonitasoft.engine.persistence.DBConfigurationsProvider;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SearchFields;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public abstract class AbstractMybatisPersistenceService extends AbstractDBPersistenceService {

    private final String dbIdentifier;

    private final Map<String, StatementMapping> statementMappings;

    private final Map<String, String> classAliasMappings;

    private final Map<String, String> classFieldAliasMappings;

    private final Map<String, String> dbStatementsMapping;

    private final Map<String, String> entityMappings;

    private final TechnicalLoggerPrintWriter debugWriter;

    private final TechnicalLoggerPrintWriter errorWriter;

    private final String statementDelimiter;

    public AbstractMybatisPersistenceService(final String name, final String dbIdentifier, final AbstractMyBatisConfigurationsProvider configurations,
            final DBConfigurationsProvider tenantConfigurationsProvider, final String statementDelimiter, final String likeEscapeCharacter,
            final TechnicalLoggerService technicalLoggerService, final SequenceManager sequenceManager, final DataSource datasource) {
        super(name, tenantConfigurationsProvider, statementDelimiter, likeEscapeCharacter, sequenceManager, datasource);
        this.dbIdentifier = dbIdentifier;
        this.statementDelimiter = statementDelimiter;
        this.statementMappings = new HashMap<String, StatementMapping>();
        this.classAliasMappings = new HashMap<String, String>();
        this.classFieldAliasMappings = new HashMap<String, String>();
        this.dbStatementsMapping = new HashMap<String, String>();
        this.entityMappings = new HashMap<String, String>();
        this.initMappings(configurations);
        this.debugWriter = new TechnicalLoggerPrintWriter(technicalLoggerService, TechnicalLogSeverity.DEBUG);
        this.errorWriter = new TechnicalLoggerPrintWriter(technicalLoggerService, TechnicalLogSeverity.ERROR);
    }

    private void initMappings(final AbstractMyBatisConfigurationsProvider configurations) {
        for (final AbstractMyBatisConfiguration myBatisConfiguration : configurations.getConfigurations()) {
            this.initMappings(myBatisConfiguration);
        }
    }

    private void initMappings(final AbstractMyBatisConfiguration myBatisConfiguration) {
        this.statementMappings.putAll(myBatisConfiguration.getStatementMappings());
        this.classAliasMappings.putAll(myBatisConfiguration.getClassAliasMappings());
        this.classFieldAliasMappings.putAll(myBatisConfiguration.getClassFieldAliasMappings());
        this.dbStatementsMapping.putAll(myBatisConfiguration.getDbStatementsMapping());
        this.entityMappings.putAll(myBatisConfiguration.getEntityMappings());
    }

    private SqlSession getSession() throws SPersistenceException {
        // TODO charles how to get the current session in MyBatis associated to the current JTA transaction?
        return null;
    }

    @Override
    protected void doExecuteSQL(final String sqlResource, final String statementDelimiter, final Map<String, String> replacements,
            final boolean useDataSourceConnection)
            throws SPersistenceException, IOException {
        // TODO charles use the useDataSourceConnection parameter
        StringReader reader = null;
        try {
            final URL url = this.getClass().getResource(sqlResource);
            if (url == null) {
                throw new IOException("SQL file not found, path=" + sqlResource);
            }
            String fileContent = new String(IOUtil.getAllContentFrom(url));
            if (replacements != null && !replacements.isEmpty()) {
                for (final Map.Entry<String, String> tableMapping : replacements.entrySet()) {
                    final String stringToReplace = tableMapping.getKey();
                    final String value = tableMapping.getValue();
                    fileContent = fileContent.replaceAll(stringToReplace, value);
                }
            }
            reader = new StringReader(fileContent);

            final SqlSession session = getSession();
            final ScriptRunner runner = getScriptRunner(session);
            runner.setLogWriter(this.debugWriter);
            runner.setErrorLogWriter(this.errorWriter);
            runner.setDelimiter(statementDelimiter);
            runner.setAutoCommit(true);
            runner.setStopOnError(true);
            try {
                runner.runScript(reader);
            } catch (final RuntimeSqlException e) {
                throw new SPersistenceException("Unable to execute command " + fileContent + " of file " + sqlResource, e);
            }
        } catch (final FileNotFoundException e) {
            throw new SPersistenceException(e);
        } catch (final IOException e) {
            throw new SPersistenceException(e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Override
    public void delete(final PersistentObject entity) throws SPersistenceException {
        if (entity == null) {
            throw new SPersistenceException("unable to delete a null entity");
        }
        this.delete(entity.getId(), entity.getClass());
    }

    @Override
    public void delete(final long id, final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
        Map<String, Object> parameters;
        try {
            parameters = getDefaultParameters();
        } catch (final TenantIdNotSetException e) {
            throw new SPersistenceException(e);
        }
        parameters.put("id", id);
        this.delete(getDeleteStatement(entityClass), parameters);
    }

    @Override
    public void deleteAll(final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
        Map<String, Object> parameters;
        try {
            parameters = getDefaultParameters();
        } catch (final TenantIdNotSetException e) {
            throw new SPersistenceException(e);
        }
        this.delete(getDeleteAllStatement(entityClass), parameters);
    }

    @Override
    public void delete(final List<Long> ids, final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
        Map<String, Object> parameters;
        try {
            parameters = getDefaultParameters();
        } catch (final TenantIdNotSetException e) {
            throw new SPersistenceException(e);
        }
        parameters.put("ids", ids);
        this.delete(getDeleteByIdsStatement(entityClass), parameters);
    }

    private void delete(final String deleteStatement, final Map<String, Object> parameters) throws SPersistenceException {
        DeleteStatement statement = null;
        if (this.statementMappings.containsKey(deleteStatement)) {
            final StatementMapping statementMapping = this.statementMappings.get(deleteStatement);
            if (statementMapping.hasParameter()) {
                parameters.put(statementMapping.getParameterName(), statementMapping.getParameterValue());
            }
            statement = new DeleteStatement(statementMapping.getDestinationStatement(), parameters);
        } else {
            statement = new DeleteStatement(deleteStatement, parameters);
        }
        statement.execute(getSession());
    }

    @Override
    public void insert(final PersistentObject entity) throws SPersistenceException {
        setId(entity);
        final InsertStatement statement = getInsertStatement(entity);
        statement.execute(getSession());
    }

    private InsertStatement getInsertStatement(final PersistentObject entity) throws SPersistenceException {
        Map<String, Object> parameters;
        try {
            parameters = getDefaultParameters();
        } catch (final TenantIdNotSetException e) {
            throw new SPersistenceException(e);
        }
        parameters.put("entity", entity);

        InsertStatement statement = null;
        final String insertStatement = getStringInsertStatement(entity);

        if (this.statementMappings.containsKey(insertStatement)) {
            final StatementMapping statementMapping = this.statementMappings.get(insertStatement);
            if (statementMapping.hasParameter()) {
                parameters.put(statementMapping.getParameterName(), statementMapping.getParameterValue());
            }
            statement = new InsertStatement(statementMapping.getDestinationStatement(), parameters, entity);
        } else {
            statement = new InsertStatement(insertStatement, parameters, entity);
        }
        return statement;
    }

    @Override
    public void insertInBatch(final List<PersistentObject> entities) throws SPersistenceException {
        final SqlSession session = getSession();
        for (final PersistentObject entity : entities) {
            setId(entity);
            final InsertStatement insertStatement = getInsertStatement(entity);
            insertStatement.execute(session);
        }
    }

    private void executeInBatch(final String sql) throws SPersistenceException {
        final SqlSession session = getSession();
        final ScriptRunner runner = getScriptRunner(session);
        runner.setDelimiter(this.statementDelimiter);
        runner.setLogWriter(null);// don't print scripts
        runner.setErrorLogWriter(this.errorWriter);
        runner.setAutoCommit(false);
        runner.setStopOnError(true);
        runner.setSendFullScript(false);
        final StringReader reader = new StringReader(sql);
        try {
            runner.runScript(reader);
        } catch (final RuntimeSqlException e) {
            throw new SPersistenceException("Unable to insert element in batch: " + sql, e);
        }
    }

    @Override
    public void purge(final String classToPurge) throws SPersistenceException {
        final StringBuilder stringBuilder = new StringBuilder();
        final String deleteScript = getSqlTransformer(classToPurge).getDeleteScript();
        if (deleteScript != null && !deleteScript.isEmpty()) {
            stringBuilder.append(deleteScript);
            stringBuilder.append(this.statementDelimiter);
            executeInBatch(stringBuilder.toString());
        }
    }

    private String getStringInsertStatement(final PersistentObject entity) {
        return getBasicStatement(entity.getClass(), "insert" + entity.getClass().getSimpleName());
    }

    private String getDeleteStatement(final Class<? extends PersistentObject> entityClass) {
        return getBasicStatement(entityClass, "delete" + entityClass.getSimpleName());
    }

    private String getDeleteAllStatement(final Class<? extends PersistentObject> entityClass) {
        return getBasicStatement(entityClass, "deleteAll" + entityClass.getSimpleName());
    }

    private String getDeleteByIdsStatement(final Class<? extends PersistentObject> entityClass) {
        return getBasicStatement(entityClass, "deleteByIds" + entityClass.getSimpleName());
    }

    private String getUpdateStatement(final PersistentObject entity) {
        return getBasicStatement(entity.getClass(), "update" + entity.getClass().getSimpleName());
    }

    private String getSelectStatement(final AbstractSelectDescriptor<?> selectDescriptor) {
        return getBasicStatement(selectDescriptor.getEntityType(), selectDescriptor.getQueryName());
    }

    private String getBasicStatement(final Class<? extends PersistentObject> entityClass, final String queryName) {
        final String entityClassName = getEntityClassName(entityClass);
        final StringBuffer statement = new StringBuffer();
        statement.append(entityClassName);
        statement.append('.');
        statement.append(queryName);
        final String defaultStatement = statement.toString();
        if (this.dbStatementsMapping.containsKey(this.dbIdentifier + "_" + queryName)) {
            return this.dbStatementsMapping.get(this.dbIdentifier + "_" + queryName);
        }
        return defaultStatement;
    }

    private String getEntityClassName(final Class<? extends PersistentObject> entityClass) {
        final String className = entityClass.getName();
        if (this.entityMappings.containsKey(className)) {
            return this.entityMappings.get(className);
        }
        return className;
    }

    private RowBounds getRowBounds(final SelectListDescriptor<?> descriptor) {
        return new RowBounds(descriptor.getStartIndex(), descriptor.getPageSize());
    }

    protected abstract Map<String, Object> getDefaultParameters() throws TenantIdNotSetException;

    @Override
    public void update(final UpdateDescriptor updateDescriptor) throws SPersistenceException {
        this.update(getSession(), updateDescriptor);
    }

    void update(final SqlSession session, final UpdateDescriptor updateDescriptor) throws SPersistenceException {

        final PersistentObject entity = updateDescriptor.getEntity();

        Map<String, Object> parameters;
        try {
            parameters = getDefaultParameters();
        } catch (final TenantIdNotSetException e) {
            throw new SPersistenceException(e);
        }
        parameters.put("id", entity.getId());
        for (final Map.Entry<String, Object> entry : updateDescriptor.getFields().entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                value = "___bonitanull___";
            }
            parameters.put(entry.getKey(), value);
        }

        for (final Map.Entry<String, Object> field : updateDescriptor.getFields().entrySet()) {
            setField(entity, field.getKey(), field.getValue());
        }

        UpdateStatement statement = null;
        final String updateStatement = getUpdateStatement(updateDescriptor.getEntity());
        if (this.statementMappings.containsKey(updateStatement)) {
            final StatementMapping statementMapping = this.statementMappings.get(updateStatement);
            if (statementMapping.hasParameter()) {
                parameters.put(statementMapping.getParameterName(), statementMapping.getParameterValue());
            }
            statement = new UpdateStatement(statementMapping.getDestinationStatement(), parameters, entity);
        } else {
            statement = new UpdateStatement(updateStatement, parameters, entity);
        }
        statement.execute(session);
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
        Map<String, Object> parameters;
        try {
            parameters = getDefaultParameters();
        } catch (final TenantIdNotSetException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
        parameters.putAll(selectDescriptor.getInputParameters());

        final String selectStatement = this.getSelectStatement(selectDescriptor, parameters);
        try {
            final T result = new SelectOneStatement<T>(selectStatement, parameters).execute(getSession());
            return result;
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    @Override
    public <T extends PersistentObject> T selectById(final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
        Map<String, Object> parameters;
        try {
            parameters = getDefaultParameters();
        } catch (final TenantIdNotSetException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
        parameters.put("id", selectDescriptor.getId());

        final String selectStatement = this.getSelectStatement(selectDescriptor, parameters);
        try {
            final SelectByIdStatement<T> statement = new SelectByIdStatement<T>(selectStatement, parameters, selectDescriptor.getEntityType(),
                    selectDescriptor.getId());
            return statement.execute(getSession());
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    private <T> String getSelectStatement(final AbstractSelectDescriptor<T> selectDescriptor, final Map<String, Object> parameters) {
        final String selectStatement = this.getSelectStatement(selectDescriptor);
        if (this.statementMappings.containsKey(selectStatement)) {
            final StatementMapping statementMapping = this.statementMappings.get(selectStatement);
            if (statementMapping.hasParameter()) {
                parameters.put(statementMapping.getParameterName(), statementMapping.getParameterValue());
            }
            return statementMapping.getDestinationStatement();
        } else {
            return selectStatement;
        }
    }

    @Override
    public <T> List<T> selectList(final SelectListDescriptor<T> selectDescriptor) throws SBonitaReadException {
        Map<String, Object> parameters;
        try {
            parameters = getDefaultParameters();
        } catch (final TenantIdNotSetException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
        parameters.putAll(selectDescriptor.getInputParameters());
        if (selectDescriptor.hasAFilter()) {
            final String queryWithFilters = getQueryWithFilters(selectDescriptor);
            parameters.put("filter", queryWithFilters);
        }

        if (selectDescriptor.hasOrderByParameters()) {
            boolean startWithComma = false;
            final StringBuilder sBuilder = new StringBuilder();
            for (final OrderByOption orderByOption : selectDescriptor.getQueryOptions().getOrderByOptions()) {
                if (startWithComma) {
                    sBuilder.append(',');
                }
                /*
                 * String tableName = this.classAliasMappings.get(orderByOption.getClazz().getName()); for (Map.Entry<String, String>
                 * tableMapping : this.tablesMapping.entrySet()) { tableName = tableName.replaceAll(tableMapping.getKey(),
                 * tableMapping.getValue()); } builder.append(tableName);
                 */
                appendFieldClassAlias(sBuilder, orderByOption.getClazz().getName(), orderByOption.getFieldName());
                sBuilder.append(' ');
                sBuilder.append(orderByOption.getOrderByType().toString());
                startWithComma = true;
            }

            parameters.put("orderBy", sBuilder.toString());
        }
        SelectListStatement<T> statement = null;
        final String selectStatement = this.getSelectStatement(selectDescriptor);

        if (this.statementMappings.containsKey(selectStatement)) {
            final StatementMapping statementMapping = this.statementMappings.get(selectStatement);
            if (statementMapping.hasParameter()) {
                parameters.put(statementMapping.getParameterName(), statementMapping.getParameterValue());
            }
            statement = new SelectListStatement<T>(statementMapping.getDestinationStatement(), parameters, getRowBounds(selectDescriptor));
        } else {
            statement = new SelectListStatement<T>(selectStatement, parameters, getRowBounds(selectDescriptor));
        }
        try {
            return statement.execute(getSession());
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        } catch (final IllegalArgumentException e) {
            // this exception will occur for instance when a query is missing:
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    /**
     * builds the filter part of the query (without the main query part)
     */
    private <T> String getQueryWithFilters(final SelectListDescriptor<T> selectDescriptor) {
        final StringBuilder builder = new StringBuilder();
        final QueryOptions queryOptions = selectDescriptor.getQueryOptions();
        final List<FilterOption> filters = queryOptions.getFilters();
        final Set<String> specificFilters = new HashSet<String>(5);
        FilterOption previousFilter = null;
        if (!filters.isEmpty()) {
            for (final FilterOption filterOption : filters) {
                // Never add AND for first element:
                if (previousFilter != null) {
                    final FilterOperationType prevOp = previousFilter.getFilterOperationType();
                    final FilterOperationType currOp = filterOption.getFilterOperationType();
                    // Auto add AND if previous operator was normal op or ')' and that current op is normal op or '(' :
                    if ((isNormalOperator(prevOp) || prevOp == R_PARENTHESIS) && (isNormalOperator(currOp) || currOp == L_PARENTHESIS)) {
                        builder.append(" AND ");
                    }
                }
                final StringBuilder aliasBuilder = appendFilterClause(builder, filterOption);
                if (aliasBuilder != null) {
                    specificFilters.add(aliasBuilder.toString());
                }
                previousFilter = filterOption;
            }
        }
        final SearchFields multipleFilter = queryOptions.getMultipleFilter();
        if (multipleFilter != null) {
            final Map<Class<? extends PersistentObject>, Set<String>> allTextFields = multipleFilter.getFields();
            final Set<String> fields = new HashSet<String>();
            for (final Entry<Class<? extends PersistentObject>, Set<String>> entry : allTextFields.entrySet()) {
                final String className = entry.getKey().getName();
                final String alias = this.classAliasMappings.get(className);
                for (final String fieldName : entry.getValue()) {
                    final StringBuilder aliasBuilder = new StringBuilder(alias);
                    aliasBuilder.append('.');
                    final String fieldQualifiedName = className + "." + fieldName;
                    if (this.classFieldAliasMappings.containsKey(fieldQualifiedName)) {
                        aliasBuilder.append(this.classFieldAliasMappings.get(fieldQualifiedName));
                    } else {
                        aliasBuilder.append(fieldName);
                    }
                    fields.add(aliasBuilder.toString());
                }
            }
            fields.removeAll(specificFilters);
            final Iterator<String> fieldIterator = fields.iterator();
            final List<String> terms = multipleFilter.getTerms();
            if (!fields.isEmpty()) {
                if (!specificFilters.isEmpty()) {
                    builder.append(" AND (");
                } else {
                    builder.append(" (");
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
        // surround filter with parenthesis (if there is a Or this is mandatory)
        String filter = builder.toString().trim();
        if (!filter.isEmpty() && !filter.startsWith("(") && !filter.endsWith(")")) {
            filter = '(' + filter + ')';
        }
        return filter;
    }

    private StringBuilder appendFilterClause(final StringBuilder clause, final FilterOption filterOption) {
        final StringBuilder completeField = new StringBuilder();
        if (filterOption.getPersistentClass() != null) {
            appendFieldClassAlias(completeField, filterOption.getPersistentClass().getName(), filterOption.getFieldName());
        }
        Object fieldValue = filterOption.getValue();
        if (fieldValue instanceof String) {
            fieldValue = "'" + fieldValue + "'";
        } else if (fieldValue instanceof EnumToObjectConvertible) {
            fieldValue = ((EnumToObjectConvertible) fieldValue).fromEnum();
        }
        final FilterOperationType type = filterOption.getFilterOperationType();
        switch (type) {
            case EQUALS:
                clause.append(completeField).append(" = ").append(fieldValue);
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
                clause.append(") ");
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

    private void appendFieldClassAlias(final StringBuilder builder, final String className, final String fieldName) {
        builder.append(this.classAliasMappings.get(className));
        builder.append('.');
        final String fieldQualifiedName = className + "." + fieldName;
        if (this.classFieldAliasMappings.containsKey(fieldQualifiedName)) {
            builder.append(this.classFieldAliasMappings.get(fieldQualifiedName));
        } else {
            builder.append(fieldName);
        }

    }

    private ScriptRunner getScriptRunner(final SqlSession session) {
        final Connection connection = session.getConnection();
        final ScriptRunner runner = new ScriptRunner(connection);
        return runner;
    }

    @Override
    public void flushStatements() throws SPersistenceException {
        getSession().flushStatements();
    }
}
