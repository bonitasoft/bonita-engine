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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.TenantPersistenceService;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

/**
 * Common implementation to persistence services relying on a database
 * 
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public abstract class AbstractDBPersistenceService implements TenantPersistenceService {

    private final List<String> createTablesFiles = new ArrayList<String>();

    private final List<String> postCreateStructureFiles = new ArrayList<String>();

    private final List<String> preDropStructureFiles = new ArrayList<String>();

    private final List<String> dropTablesFiles = new ArrayList<String>();

    private final List<String> initTablesFiles = new ArrayList<String>();

    private final List<String> cleanTablesFiles = new ArrayList<String>();

    private final List<String> deleteObjectsFiles = new ArrayList<String>();

    private final Map<String, SQLTransformer> sqlTransformers = new HashMap<String, SQLTransformer>();

    private final String statementDelimiter;

    private final String likeEscapeCharacter;

    private final String name;

    private final SequenceManager sequenceManager;

    protected final DataSource datasource;

    public AbstractDBPersistenceService(final String name, final DBConfigurationsProvider dbConfigurationsProvider, final String statementDelimiter,
            final String likeEscapeCharacter, final SequenceManager sequenceManager, final DataSource datasource) {
        this.name = name;
        this.sequenceManager = sequenceManager;
        this.datasource = datasource;
        initTablesFiles(dbConfigurationsProvider, name);
        this.statementDelimiter = statementDelimiter;
        this.likeEscapeCharacter = likeEscapeCharacter;
    }

    @Override
    public String getName() {
        return name;
    }

    protected void initTablesFiles(final DBConfigurationsProvider dbConfigurationsProvider, final String persistenceDBConfigFilter) {
        if (dbConfigurationsProvider != null) {
            for (final DBConfiguration dbConfiguration : dbConfigurationsProvider.getMatchingTenantConfigurations(persistenceDBConfigFilter)) {
                if (dbConfiguration.hasCreateTablesFile()) {
                    createTablesFiles.add(dbConfiguration.getCreateTablesFile());
                }
                if (dbConfiguration.hasInitTablesFile()) {
                    initTablesFiles.add(dbConfiguration.getInitTablesFile());
                }
                if (dbConfiguration.hasCleanTablesFile()) {
                    cleanTablesFiles.add(dbConfiguration.getCleanTablesFile());
                }
                if (dbConfiguration.hasDropTablesFile()) {
                    dropTablesFiles.add(dbConfiguration.getDropTablesFile());
                }
                if (dbConfiguration.hasDeleteTenantObjectsFile()) {
                    deleteObjectsFiles.add(dbConfiguration.getDeleteTenantObjectsFile());
                }
                if (dbConfiguration.hasPostCreateStructureFile()) {
                    postCreateStructureFiles.add(dbConfiguration.getPostCreateStructureFile());
                }
                if (dbConfiguration.hasPreDropStructureFile()) {
                    preDropStructureFiles.add(dbConfiguration.getPreDropStructureFile());
                }
                if (dbConfiguration.hasSqlTransformers()) {
                    sqlTransformers.putAll(dbConfiguration.getSqlTransformers());
                }
            }
        }
    }

    @Override
    public void createStructure() throws SPersistenceException, IOException {
        for (final String sqlResource : createTablesFiles) {
            executeSQL(sqlResource, statementDelimiter, null, true);
        }
    }

    @Override
    public void postCreateStructure() throws SPersistenceException, IOException {
        for (final String sqlResource : postCreateStructureFiles) {
            executeSQL(sqlResource, statementDelimiter, null, true);
        }
    }

    @Override
    public void preDropStructure() throws SPersistenceException, IOException {
        for (final String sqlResource : preDropStructureFiles) {
            executeSQL(sqlResource, statementDelimiter, null, true);
        }
    }

    @Override
    public void cleanStructure() throws SPersistenceException, IOException {
        for (final String sqlResource : cleanTablesFiles) {
            executeSQL(sqlResource, statementDelimiter, null, true);
        }
    }

    @Override
    public void deleteStructure() throws SPersistenceException, IOException {
        sequenceManager.clear();
        for (final String sqlResource : dropTablesFiles) {
            executeSQL(sqlResource, statementDelimiter, null, true);
        }
    }

    @Override
    public void initializeStructure() throws SPersistenceException, IOException {
        initializeStructure(Collections.<String, String> emptyMap());
    }

    @Override
    public void initializeStructure(final Map<String, String> replacements) throws SPersistenceException, IOException {
        for (final String sqlResource : initTablesFiles) {
            // FIXME Are we obliged to use the Hibernate connection ?
            executeSQL(sqlResource, statementDelimiter, replacements, false);
        }
    }

    @Override
    public void deleteTenant(final long tenantId) throws SPersistenceException, IOException {
        sequenceManager.clear(tenantId);
        final Map<String, String> replacements = Collections.singletonMap("tenantid", String.valueOf(tenantId));
        for (final String sqlResource : deleteObjectsFiles) {
            executeSQL(sqlResource, statementDelimiter, replacements, true);
        }
    }

    private void executeSQL(final String sqlResource, final String statementDelimiter, final Map<String, String> replacements,
            final boolean useDataSourceConnection) throws SPersistenceException, IOException {
        if (replacements != null) {
            final Map<String, String> replacementsWithVarDelimiters = new HashMap<String, String>();
            for (final Entry<String, String> entry : replacements.entrySet()) {
                if (entry.getKey().charAt(0) == '$') {
                    replacementsWithVarDelimiters.put(entry.getKey(), entry.getValue());
                } else {
                    replacementsWithVarDelimiters.put(new StringBuilder("\\$\\{").append(entry.getKey()).append("\\}").toString(), entry.getValue());
                }
            }
            doExecuteSQL(sqlResource, statementDelimiter, replacementsWithVarDelimiters, useDataSourceConnection);
        } else {
            doExecuteSQL(sqlResource, statementDelimiter, null, useDataSourceConnection);
        }
    }

    protected SQLTransformer getSqlTransformer(final String className) {
        return sqlTransformers.get(className);
    }

    protected List<SQLTransformer> getSqlTransformers() {
        return new ArrayList<SQLTransformer>(sqlTransformers.values());
    }

    protected abstract void doExecuteSQL(final String sqlResource, final String statementDelimiter, final Map<String, String> replacements,
            final boolean useDataSourceConnection) throws SPersistenceException, IOException;

    @Override
    public <T extends PersistentObject> long getNumberOfEntities(final Class<T> entityClass, final QueryOptions options, final Map<String, Object> parameters)
            throws SBonitaReadException {
        return getNumberOfEntities(entityClass, null, options, parameters);
    }

    @Override
    public <T extends PersistentObject> long getNumberOfEntities(final Class<T> entityClass, final String querySuffix, final QueryOptions options,
            final Map<String, Object> parameters) throws SBonitaReadException {
        List<FilterOption> filters;
        if (options == null) {
            filters = Collections.emptyList();
        } else {
            filters = options.getFilters();
        }
        final String queryName = getQueryName("getNumberOf", querySuffix, entityClass, filters);

        final SelectListDescriptor<Long> descriptor = new SelectListDescriptor<Long>(queryName, parameters, entityClass, Long.class, options);
        return selectList(descriptor).get(0);
    }

    @Override
    public <T extends PersistentObject> List<T> searchEntity(final Class<T> entityClass, final QueryOptions options, final Map<String, Object> parameters)
            throws SBonitaReadException {
        return searchEntity(entityClass, null, options, parameters);
    }

    @Override
    public <T extends PersistentObject> List<T> searchEntity(final Class<T> entityClass, final String querySuffix, final QueryOptions options,
            final Map<String, Object> parameters) throws SBonitaReadException {
        final String queryName = getQueryName("search", querySuffix, entityClass, options.getFilters());
        final SelectListDescriptor<T> descriptor = new SelectListDescriptor<T>(queryName, parameters, entityClass, options);
        return selectList(descriptor);
    }

    private <T extends PersistentObject> String getQueryName(final String prefix, final String suffix, final Class<T> entityClass,
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
     * @return
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
                id = getSequenceManager().getNextId(entity.getClass().getName(), getTenantId());
                ClassReflector.invokeSetter(entity, "setId", long.class, id);
            } catch (final Exception e) {
                throw new SPersistenceException("Problem while saving entity: " + entity + " with id: " + id, e);
            }
        }
    }

    /**
     * Get like clause for given term with escaped sql query wildcards and escape character
     */
    protected String getLikeEscapeClause(final String term) {
        final StringBuilder builder = new StringBuilder();
        builder.append(" LIKE '");
        // 1) escape ' character by adding another ' character
        // 2) protect escape character if this character is used in data
        // 3) escape % character (sql query wildcard) by adding escape character
        // 4) escape _ character (sql query wildcard) by adding escape character
        builder.append(term.replaceAll("'", "''").replaceAll(likeEscapeCharacter, likeEscapeCharacter + likeEscapeCharacter)
                .replaceAll("%", likeEscapeCharacter + "%").replaceAll("_", likeEscapeCharacter + "_"));
        builder.append("%' ESCAPE '").append(likeEscapeCharacter).append('\'');
        return builder.toString();
    }

}
