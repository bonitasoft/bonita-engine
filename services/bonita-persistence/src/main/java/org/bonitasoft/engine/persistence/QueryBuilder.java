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

import static java.util.Collections.emptySet;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.query.Query;

/**
 * @author Baptiste Mesta
 */
@Slf4j
abstract class QueryBuilder<T> {

    private final Query baseQuery;
    private final OrderByCheckingMode orderByCheckingMode;
    private final AbstractSelectDescriptor<T> selectDescriptor;
    private final QueryGeneratorForFilters queryGeneratorForFilters;
    private final QueryGeneratorForSearchTerm queryGeneratorForSearchTerm;
    private final QueryGeneratorForOrderBy queryGeneratorForOrderBy;
    StringBuilder stringQueryBuilder;
    private Map<String, String> classAliasMappings;
    private Session session;
    private boolean cacheEnabled;
    private Map<String, Object> parameters = new HashMap<>();

    QueryBuilder(Session session, Query baseQuery, OrderByBuilder orderByBuilder,
            Map<String, String> classAliasMappings,
            char likeEscapeCharacter, OrderByCheckingMode orderByCheckingMode,
            AbstractSelectDescriptor<T> selectDescriptor) {
        this.session = session;
        this.classAliasMappings = classAliasMappings;
        stringQueryBuilder = new StringBuilder(baseQuery.getQueryString());
        this.baseQuery = baseQuery;
        this.orderByCheckingMode = orderByCheckingMode;
        this.selectDescriptor = selectDescriptor;
        this.queryGeneratorForFilters = new QueryGeneratorForFilters(classAliasMappings,
                likeEscapeCharacter);
        this.queryGeneratorForSearchTerm = new QueryGeneratorForSearchTerm(likeEscapeCharacter);
        this.queryGeneratorForOrderBy = new QueryGeneratorForOrderBy(classAliasMappings, orderByBuilder);

    }

    public String getQuery() {
        return stringQueryBuilder.toString();
    }

    void appendFilters(List<FilterOption> filters, SearchFields multipleFilter) {
        Set<String> specificFilters = emptySet();
        if (!filters.isEmpty()) {
            if (!hasWHEREInRootQuery(stringQueryBuilder.toString())) {
                stringQueryBuilder.append(" WHERE (");
            } else {
                stringQueryBuilder.append(" AND (");
            }
            QueryGeneratorForFilters.QueryGeneratedFilters whereClause = queryGeneratorForFilters.generate(filters);
            specificFilters = whereClause.getSpecificFilters();
            stringQueryBuilder.append(whereClause.getFilters());
            stringQueryBuilder.append(")");
            parameters.putAll(whereClause.getParameters());
        }
        if (multipleFilter != null && multipleFilter.getTerms() != null && !multipleFilter.getTerms().isEmpty()) {
            handleMultipleFilters(stringQueryBuilder, multipleFilter, specificFilters);
        }
    }

    static boolean hasWHEREInRootQuery(String query) {
        // We simply remove all blocks that are in parenthesis in order to remove all subqueries
        // Then we check if there is the `where` word here
        return removeAllParenthesisBlocks(query.toLowerCase()).contains("where");
    }

    private static String removeAllParenthesisBlocks(String q) {
        StringBuilder stringBuilder = new StringBuilder(q.length());
        int depthCounter = 0;
        for (char c : q.toCharArray()) {
            switch (c) {
                case '(':
                    depthCounter++;
                    break;
                case ')':
                    depthCounter--;
                    break;
                default:
                    if (depthCounter == 0) {
                        stringBuilder.append(c);
                    }
            }
        }
        return stringBuilder.toString();
    }

    private void handleMultipleFilters(final StringBuilder builder, final SearchFields multipleFilter,
            final Set<String> specificFilters) {
        final Map<Class<? extends PersistentObject>, Set<String>> allTextFields = multipleFilter.getFields();
        final Set<String> fields = new HashSet<>();
        for (final Map.Entry<Class<? extends PersistentObject>, Set<String>> entry : allTextFields.entrySet()) {
            final String alias = classAliasMappings.get(entry.getKey().getName());
            for (final String field : entry.getValue()) {
                fields.add(alias + '.' + field);
            }
        }
        fields.removeAll(specificFilters);

        if (!fields.isEmpty()) {
            final List<String> terms = multipleFilter.getTerms();
            applyFiltersOnQuery(builder, fields, terms);
        }
    }

    private void applyFiltersOnQuery(final StringBuilder queryBuilder, final Set<String> fields,
            final List<String> terms) {
        if (!hasWHEREInRootQuery(queryBuilder.toString())) {
            queryBuilder.append(" WHERE ");
        } else {
            queryBuilder.append(" AND ");
        }
        queryBuilder.append("(");

        QueryGeneratorForSearchTerm.QueryGeneratedSearchTerms result = queryGeneratorForSearchTerm.generate(fields,
                terms);
        queryBuilder.append(result.getSearch());

        queryBuilder.append(")");
        parameters.putAll(result.getParameters());
    }

    void appendOrderByClause(List<OrderByOption> orderByOptions, Class<? extends PersistentObject> entityType)
            throws SBonitaReadException {
        String result = queryGeneratorForOrderBy.generate(orderByOptions, entityType);
        stringQueryBuilder.append(result);
    }

    boolean hasChanged() {
        return !baseQuery.getQueryString().equals(stringQueryBuilder.toString());
    }

    abstract Query rebuildQuery(AbstractSelectDescriptor<T> selectDescriptor, Session session, Query query);

    void manageFiltersAndParameters(AbstractSelectDescriptor<T> selectDescriptor)
            throws SBonitaReadException {
        if (selectDescriptor.hasAFilter()) {
            final QueryOptions queryOptions = selectDescriptor.getQueryOptions();
            appendFilters(queryOptions.getFilters(), queryOptions.getMultipleFilter());
        }
        if (selectDescriptor.hasOrderByParameters()) {
            appendOrderByClause(selectDescriptor.getQueryOptions().getOrderByOptions(),
                    selectDescriptor.getEntityType());
        }
    }

    public QueryBuilder cache(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        return this;
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

    public Query build() throws SBonitaReadException {
        manageFiltersAndParameters(selectDescriptor);
        Query query = baseQuery;
        if (hasChanged()) {
            query = rebuildQuery(selectDescriptor, session, baseQuery);
        }
        addConstantsAsParameters(query);
        setParameters(query, selectDescriptor.getInputParameters());
        query.setFirstResult(selectDescriptor.getStartIndex());
        query.setMaxResults(selectDescriptor.getPageSize());
        query.setCacheable(cacheEnabled);
        checkOrderByClause(query);
        return query;
    }

    protected abstract void addConstantsAsParameters(Query query);

    private void checkOrderByClause(final Query query) {
        if (!query.getQueryString().toLowerCase().contains("order by")) {
            switch (orderByCheckingMode) {
                case NONE:
                    break;
                case WARNING:
                    log.warn(
                            "Query '{}' does not contain 'ORDER BY' clause. It's better to modify your query to order" +
                                    " the result, especially if you use the pagination.",
                            query.getQueryString());
                    break;
                case STRICT:
                default:
                    throw new IllegalArgumentException("Query " + query.getQueryString()
                            + " does not contain 'ORDER BY' clause hence is not allowed. Please specify ordering before re-sending the query");
            }
        }
    }

    /*
     * escape for other things than like
     */
    static String escapeString(final String term) {
        // 1) escape ' character by adding another ' character
        return term.replaceAll("'", "''");
    }

    /*
     * escape for like
     */
    static String escapeTerm(final String term, String likeEscapeCharacter) {
        // 1) protect escape character if this character is used in data
        // 2) escape % character (sql query wildcard) by adding escape character
        // 3) escape _ character (sql query wildcard) by adding escape character
        return term
                .replace(likeEscapeCharacter, likeEscapeCharacter + likeEscapeCharacter)
                .replace("%", likeEscapeCharacter + "%")
                .replace("_", likeEscapeCharacter + "_");
    }

    Map<String, Object> getQueryParameters() {
        return parameters;
    }
}
