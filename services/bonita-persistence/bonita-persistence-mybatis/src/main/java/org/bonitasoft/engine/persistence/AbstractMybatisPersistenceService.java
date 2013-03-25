/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.persistence;

import static org.bonitasoft.engine.persistence.search.FilterOperationType.L_PARENTHESIS;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.R_PARENTHESIS;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.isNormalOperator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.STransactionResourceException;
import org.bonitasoft.engine.transaction.TechnicalTransaction;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public abstract class AbstractMybatisPersistenceService extends AbstractDBPersistenceService implements MybatisTechnicalTransactionListener {

    private final String dbIdentifier;

    private final MybatisSqlSessionFactoryProvider mybatisSqlSessionFactoryProvider;

    private final ThreadLocal<MybatisTechnicalTransaction> technicalTxs = new ThreadLocal<MybatisTechnicalTransaction>();

    private final TransactionService txService;

    private final boolean cacheEnabled;

    private final Map<String, StatementMapping> statementMappings;

    private final Map<String, String> classAliasMappings;

    private final Map<String, String> classFieldAliasMappings;

    private final Map<String, String> dbStatementsMapping;

    private final Map<String, String> entityMappings;

    private final TechnicalLoggerPrintWriter debugWriter;

    private final TechnicalLoggerPrintWriter errorWriter;

    public AbstractMybatisPersistenceService(final String name, final String dbIdentifier, final TransactionService txService, final boolean cacheEnabled,
            final MybatisSqlSessionFactoryProvider mybatisSqlSessionFactoryProvider, final AbstractMyBatisConfigurationsProvider configurations,
            final DBConfigurationsProvider tenantConfigurationsProvider, final String statementDelimiter, final TechnicalLoggerService technicalLoggerService,
            final SequenceManager sequenceManager) throws SPersistenceException {
        super(name, tenantConfigurationsProvider, statementDelimiter, sequenceManager);
        this.dbIdentifier = dbIdentifier;
        this.txService = txService;
        this.cacheEnabled = cacheEnabled;
        this.mybatisSqlSessionFactoryProvider = mybatisSqlSessionFactoryProvider;
        statementMappings = new HashMap<String, StatementMapping>();
        classAliasMappings = new HashMap<String, String>();
        classFieldAliasMappings = new HashMap<String, String>();
        dbStatementsMapping = new HashMap<String, String>();
        entityMappings = new HashMap<String, String>();
        this.initMappings(configurations);
        debugWriter = new TechnicalLoggerPrintWriter(technicalLoggerService, TechnicalLogSeverity.DEBUG);
        errorWriter = new TechnicalLoggerPrintWriter(technicalLoggerService, TechnicalLogSeverity.ERROR);
    }

    private void initMappings(final AbstractMyBatisConfigurationsProvider configurations) {
        for (final AbstractMyBatisConfiguration myBatisConfiguration : configurations.getConfigurations()) {
            this.initMappings(myBatisConfiguration);
        }
    }

    private void initMappings(final AbstractMyBatisConfiguration myBatisConfiguration) {
        statementMappings.putAll(myBatisConfiguration.getStatementMappings());
        classAliasMappings.putAll(myBatisConfiguration.getClassAliasMappings());
        classFieldAliasMappings.putAll(myBatisConfiguration.getClassFieldAliasMappings());
        dbStatementsMapping.putAll(myBatisConfiguration.getDbStatementsMapping());
        entityMappings.putAll(myBatisConfiguration.getEntityMappings());
    }

    private MybatisTechnicalTransaction getTechnicalTx() {
        return technicalTxs.get();
    }

    @Override
    public void close(final MybatisSession session) {
        if (isCurrentSession(session)) {
            closeCurrentSession();
        }
    }

    private boolean isCurrentSession(final MybatisSession session) {
        final MybatisSession currentSession = getCurrentSession();
        return currentSession != null && currentSession.equals(session);
    }

    private MybatisSession getCurrentSession() {
        final MybatisTechnicalTransaction technicalTx = getTechnicalTx();
        if (technicalTx != null) {
            return technicalTx.getSession();
        }
        return null;
    }

    private void closeCurrentSession() {
        technicalTxs.remove();
    }

    MybatisTechnicalTransaction createTechnicalTransaction(final MybatisTechnicalTransactionListener listener, final boolean enableCache)
            throws SPersistenceException {
        final SqlSession session = mybatisSqlSessionFactoryProvider.getSqlSessionFactory().openSession();
        final boolean useCache = cacheEnabled && enableCache;
        return new MybatisTechnicalTransaction(listener, session, useCache);
    }

    private MybatisSession getSession() throws SPersistenceException {
        MybatisTechnicalTransaction technicalTx = getTechnicalTx();
        if (technicalTx != null) {
            return technicalTx.getSession();
        }
        org.bonitasoft.engine.transaction.BusinessTransaction globalTransaction = null;
        try {
            globalTransaction = txService.getTransaction();
        } catch (final STransactionNotFoundException e) {
            throw new SPersistenceException(e);
        }
        technicalTx = createTechnicalTransaction(this, globalTransaction.isCacheEnabled());

        try {
            globalTransaction.enlistTechnicalTransaction(technicalTx);
        } catch (final STransactionResourceException e) {
            technicalTx.getSession().close();
            throw new SPersistenceException(e);
        }
        technicalTxs.set(technicalTx);
        return technicalTx.getSession();
    }

    @Override
    protected void doExecuteSQL(final String sqlResource, final String statementDelimiter, final Map<String, String> replacements)
            throws SPersistenceException, IOException {
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

            final MybatisSession session = getSession();
            final ScriptRunner runner = session.getScriptRunner();
            runner.setLogWriter(debugWriter);
            runner.setErrorLogWriter(errorWriter);
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
        if (statementMappings.containsKey(deleteStatement)) {
            final StatementMapping statementMapping = statementMappings.get(deleteStatement);
            if (statementMapping.hasParameter()) {
                parameters.put(statementMapping.getParameterName(), statementMapping.getParameterValue());
            }
            statement = new DeleteStatement(statementMapping.getDestinationStatement(), parameters);
        } else {
            statement = new DeleteStatement(deleteStatement, parameters);
        }
        getSession().executeDeleteStatement(statement);
    }

    @Override
    public TechnicalTransaction getTechnicalTransaction() {
        return technicalTxs.get();
    }

    @Override
    public void insert(final PersistentObject entity) throws SPersistenceException {
        setId(entity);
        Map<String, Object> parameters;
        try {
            parameters = getDefaultParameters();
        } catch (final TenantIdNotSetException e) {
            throw new SPersistenceException(e);
        }
        parameters.put("entity", entity);

        InsertStatement statement = null;
        final String insertStatement = getInsertStatement(entity);

        if (statementMappings.containsKey(insertStatement)) {
            final StatementMapping statementMapping = statementMappings.get(insertStatement);
            if (statementMapping.hasParameter()) {
                parameters.put(statementMapping.getParameterName(), statementMapping.getParameterValue());
            }
            statement = new InsertStatement(statementMapping.getDestinationStatement(), parameters, entity);
        } else {
            statement = new InsertStatement(insertStatement, parameters, entity);
        }

        getSession().executeInsertStatement(statement);
    }

    @Override
    public void insertInBatch(final List<PersistentObject> entities) throws SPersistenceException {
        for (final PersistentObject entity : entities) {
            setId(entity);
        }
        final String sql = getInsertScript(entities);
        executeInBatch(sql);
    }

    private void executeInBatch(final String sql) throws SPersistenceException {
        final MybatisSession session = getSession();
        final ScriptRunner runner = session.getScriptRunner();
        runner.setDelimiter(SQLTransformer.DELIMITER);
        runner.setLogWriter(null);// don't print scripts
        runner.setErrorLogWriter(errorWriter);
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
            stringBuilder.append(SQLTransformer.DELIMITER);
            executeInBatch(stringBuilder.toString());
        }
    }

    protected String getInsertScript(final List<PersistentObject> entities) throws SPersistenceException {
        final String className = entities.get(0).getClass().getName();
        final SQLTransformer sqlTransformer = getSqlTransformer(className);
        if (sqlTransformer == null) {
            throw new SPersistenceException("Unable to insert in batch: No transformer for " + className);
        }
        final String sql = sqlTransformer.getInsertScript(entities);
        return sql;
    }

    private String getInsertStatement(final PersistentObject entity) {
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
        if (dbStatementsMapping.containsKey(dbIdentifier + "_" + queryName)) {
            return dbStatementsMapping.get(dbIdentifier + "_" + queryName);
        }
        return defaultStatement;
    }

    private String getEntityClassName(final Class<? extends PersistentObject> entityClass) {
        final String className = entityClass.getName();
        if (entityMappings.containsKey(className)) {
            return entityMappings.get(className);
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

    void update(final MybatisSession session, final UpdateDescriptor updateDescriptor) throws SPersistenceException {

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
        if (statementMappings.containsKey(updateStatement)) {
            final StatementMapping statementMapping = statementMappings.get(updateStatement);
            if (statementMapping.hasParameter()) {
                parameters.put(statementMapping.getParameterName(), statementMapping.getParameterValue());
            }
            statement = new UpdateStatement(statementMapping.getDestinationStatement(), parameters, entity);
        } else {
            statement = new UpdateStatement(updateStatement, parameters, entity);
        }
        session.executeUpdateStatement(statement);
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
            return getSession().executeSelectOneStatement(new SelectOneStatement<T>(selectStatement, parameters));
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    @Override
    public <T extends PersistentObject> T selectById(final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
        try {
            return this.selectById(getSession(), selectDescriptor);
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    <T extends PersistentObject> T selectById(final MybatisSession session, final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
        Map<String, Object> parameters;
        try {
            parameters = getDefaultParameters();
        } catch (final TenantIdNotSetException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
        parameters.put("id", selectDescriptor.getId());

        final String selectStatement = this.getSelectStatement(selectDescriptor, parameters);
        try {
            return session.executeSelectByIdStatement(new SelectByIdStatement<T>(selectStatement, parameters, selectDescriptor.getEntityType(),
                    selectDescriptor.getId()));
        } catch (final SPersistenceException e) {
            throw new SBonitaReadException(e, selectDescriptor);
        }
    }

    private <T> String getSelectStatement(final AbstractSelectDescriptor<T> selectDescriptor, final Map<String, Object> parameters) throws SBonitaReadException {
        final String selectStatement = this.getSelectStatement(selectDescriptor);
        if (statementMappings.containsKey(selectStatement)) {
            final StatementMapping statementMapping = statementMappings.get(selectStatement);
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

        if (statementMappings.containsKey(selectStatement)) {
            final StatementMapping statementMapping = statementMappings.get(selectStatement);
            if (statementMapping.hasParameter()) {
                parameters.put(statementMapping.getParameterName(), statementMapping.getParameterValue());
            }
            statement = new SelectListStatement<T>(statementMapping.getDestinationStatement(), parameters, getRowBounds(selectDescriptor));
        } else {
            statement = new SelectListStatement<T>(selectStatement, parameters, getRowBounds(selectDescriptor));
        }
        try {
            return getSession().executeSelectListStatement(statement);
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
                final String alias = classAliasMappings.get(className);
                for (final String fieldName : entry.getValue()) {
                    final StringBuilder aliasBuilder = new StringBuilder(alias);
                    aliasBuilder.append('.');
                    final String fieldQualifiedName = className + "." + fieldName;
                    if (classFieldAliasMappings.containsKey(fieldQualifiedName)) {
                        aliasBuilder.append(classFieldAliasMappings.get(fieldQualifiedName));
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
                        builder.append(currentField).append(" LIKE '").append(currentTerm).append('\'');
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
        builder.append(classAliasMappings.get(className));
        builder.append('.');
        final String fieldQualifiedName = className + "." + fieldName;
        if (classFieldAliasMappings.containsKey(fieldQualifiedName)) {
            builder.append(classFieldAliasMappings.get(fieldQualifiedName));
        } else {
            builder.append(fieldName);
        }

    }
}
