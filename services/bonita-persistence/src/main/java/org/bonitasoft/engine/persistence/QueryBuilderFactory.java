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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

/**
 * @author Baptiste Mesta
 */
@Slf4j
public class QueryBuilderFactory {

    private OrderByCheckingMode orderByCheckingMode;
    private OrderByBuilder orderByBuilder = new DefaultOrderByBuilder();
    private Map<String, String> classAliasMappings;
    private char likeEscapeCharacter;
    private final Set<Class<? extends PersistentObject>> wordSearchExclusionMappings = new HashSet<>();
    private final boolean enableWordSearch;

    public QueryBuilderFactory(OrderByCheckingMode orderByCheckingMode, Map<String, String> classAliasMappings,
            char likeEscapeCharacter, boolean enableWordSearch, Set<String> wordSearchExclusionMappings)
            throws Exception {
        this.orderByCheckingMode = orderByCheckingMode;
        this.classAliasMappings = classAliasMappings;
        this.likeEscapeCharacter = likeEscapeCharacter;
        this.enableWordSearch = enableWordSearch;
        initializeWordSearchExclusions(enableWordSearch, wordSearchExclusionMappings);
    }

    private void initializeWordSearchExclusions(boolean enableWordSearch, Set<String> wordSearchExclusionMappings)
            throws Exception {
        if (enableWordSearch) {
            log.warn("The word based search feature is experimental, using it in production may impact performances.");
        }
        if (wordSearchExclusionMappings != null && !wordSearchExclusionMappings.isEmpty()) {
            if (!enableWordSearch) {
                log.info("You defined an exclusion mapping for the word based search feature, but it is not enabled.");
            }
            for (final String wordSearchExclusionMapping : wordSearchExclusionMappings) {
                final Class<?> clazz = Class.forName(wordSearchExclusionMapping);
                if (!PersistentObject.class.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException(
                            "Unable to add a word search exclusion mapping for class " + clazz
                                    + " because it does not implements "
                                    + PersistentObject.class);
                }
                this.wordSearchExclusionMappings.add((Class<? extends PersistentObject>) clazz);
            }
        }
    }

    public <T> QueryBuilder createQueryBuilderFor(Session session,
            SelectListDescriptor<T> selectDescriptor) {
        Query query = session.getNamedQuery(selectDescriptor.getQueryName());
        boolean wordSearchEnabled = isWordSearchEnabled(selectDescriptor.getEntityType());
        if (query instanceof NativeQuery) {
            return new SQLQueryBuilder<>(session, query, orderByBuilder, classAliasMappings,
                    likeEscapeCharacter,
                    wordSearchEnabled, orderByCheckingMode, selectDescriptor);
        } else {
            return new HQLQueryBuilder<>(session, query, orderByBuilder, classAliasMappings, likeEscapeCharacter,
                    wordSearchEnabled, orderByCheckingMode, selectDescriptor);
        }
    }

    public void setOrderByBuilder(OrderByBuilder orderByBuilder) {
        this.orderByBuilder = orderByBuilder;
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

}
