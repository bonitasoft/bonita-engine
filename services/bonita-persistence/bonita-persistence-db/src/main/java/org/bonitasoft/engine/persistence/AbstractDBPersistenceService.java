/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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

    private final String likeEscapeCharacter;

    private final String name;

    private final SequenceManager sequenceManager;

    protected final DataSource datasource;

    private final Set<Class<? extends PersistentObject>> wordSearchExclusionMappings = new HashSet<Class<? extends PersistentObject>>();

    private final boolean enableWordSearch;

    protected final TechnicalLoggerService logger;

    public AbstractDBPersistenceService(final String name, final String likeEscapeCharacter,
            final boolean enableWordSearch, final Set<String> wordSearchExclusionMappings, final TechnicalLoggerService logger) throws ClassNotFoundException {
        this.name = name;
        sequenceManager = null;
        datasource = null;
        this.likeEscapeCharacter = likeEscapeCharacter;
        this.enableWordSearch = enableWordSearch;
        if (wordSearchExclusionMappings != null) {
            for (final String wordSearchExclusionMapping : wordSearchExclusionMappings) {
                final Class<?> clazz = Class.forName(wordSearchExclusionMapping);
                if (!PersistentObject.class.isAssignableFrom(clazz)) {
                    throw new RuntimeException("Unable to add a word search exclusion mapping for class " + clazz + " because it does not implements "
                            + PersistentObject.class);
                }
                this.wordSearchExclusionMappings.add((Class<? extends PersistentObject>) clazz);
            }
        }
        this.logger = logger;
    }

    public AbstractDBPersistenceService(final String name,
            final String likeEscapeCharacter, final SequenceManager sequenceManager, final DataSource datasource,
            final boolean enableWordSearch, final Set<String> wordSearchExclusionMappings, final TechnicalLoggerService logger) throws ClassNotFoundException {
        this.name = name;
        this.sequenceManager = sequenceManager;
        this.datasource = datasource;
        this.likeEscapeCharacter = likeEscapeCharacter;
        this.enableWordSearch = enableWordSearch;
        this.logger = logger;

        if (enableWordSearch && logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
            logger.log(getClass(), TechnicalLogSeverity.WARNING,
                    "The word based search feature is experimental, using it in production may impact performances.");
        }
        if (wordSearchExclusionMappings != null && !wordSearchExclusionMappings.isEmpty()) {
            if (!enableWordSearch && logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
                logger.log(getClass(), TechnicalLogSeverity.INFO, "You defined an exclusion mapping for the word based search feature, but it is not enabled.");
            }
            for (final String wordSearchExclusionMapping : wordSearchExclusionMappings) {
                final Class<?> clazz = Class.forName(wordSearchExclusionMapping);
                if (!PersistentObject.class.isAssignableFrom(clazz)) {
                    throw new RuntimeException("Unable to add a word search exclusion mapping for class " + clazz + " because it does not implements "
                            + PersistentObject.class);
                }
                this.wordSearchExclusionMappings.add((Class<? extends PersistentObject>) clazz);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    protected boolean isWordSearchEnabled(final Class<? extends PersistentObject> entityClass) {
        if (!enableWordSearch || entityClass == null) {
            return false;
        }
        for (final Class<? extends PersistentObject> exclusion : wordSearchExclusionMappings) {
            if (exclusion.isAssignableFrom(entityClass)) {
                return false;
            }
        }
        return true;
    }

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
    protected String buildLikeEscapeClause(final String term, final String prefixPattern, final String suffixPattern) {
        return " LIKE '" + (prefixPattern != null ? prefixPattern : "") + escapeTerm(term) + (suffixPattern != null ? suffixPattern : "") + "' ESCAPE '"
                + getLikeEscapeCharacter() + "'";
    }

    /**
     * @param term
     * @return
     */
    protected String escapeTerm(final String term) {
        // 1) escape ' character by adding another ' character
        // 2) protect escape character if this character is used in data
        // 3) escape % character (sql query wildcard) by adding escape character
        // 4) escape _ character (sql query wildcard) by adding escape character
        return term
                .replaceAll("'", "''")
                .replaceAll(getLikeEscapeCharacter(), getLikeEscapeCharacter() + getLikeEscapeCharacter())
                .replaceAll("%", getLikeEscapeCharacter() + "%")
                .replaceAll("_", getLikeEscapeCharacter() + "_");
    }

    protected String getLikeEscapeCharacter() {
        return likeEscapeCharacter;
    }

}
