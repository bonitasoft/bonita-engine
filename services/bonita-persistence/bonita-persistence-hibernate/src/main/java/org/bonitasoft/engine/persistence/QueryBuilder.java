/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import static org.bonitasoft.engine.persistence.search.FilterOperationType.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.commons.EnumToObjectConvertible;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author Baptiste Mesta
 */
abstract class QueryBuilder {

    private final String baseQuery;
    StringBuilder stringQueryBuilder;
    Map<String, String> classAliasMappings;
    Map<String, Class<? extends PersistentObject>> interfaceToClassMapping;
    private String likeEscapeCharacter;
    private OrderByBuilder orderByBuilder;

    QueryBuilder(String baseQuery, OrderByBuilder orderByBuilder, Map<String, String> classAliasMappings,
            Map<String, Class<? extends PersistentObject>> interfaceToClassMapping, char likeEscapeCharacter) {
        this.orderByBuilder = orderByBuilder;
        this.classAliasMappings = classAliasMappings;
        this.interfaceToClassMapping = interfaceToClassMapping;
        this.likeEscapeCharacter = String.valueOf(likeEscapeCharacter);
        stringQueryBuilder = new StringBuilder(baseQuery);
        this.baseQuery = baseQuery;
    }

    public String getQuery() {
        return stringQueryBuilder.toString();
    }

    void appendFilters(List<FilterOption> filters, SearchFields multipleFilter, boolean enableWordSearch) {
        final Set<String> specificFilters = new HashSet<>(filters.size());
        if (!filters.isEmpty()) {
            FilterOption previousFilter = null;
            if (!this.stringQueryBuilder.toString().contains("WHERE")) {
                stringQueryBuilder.append(" WHERE (");
            } else {
                stringQueryBuilder.append(" AND (");
            }
            for (final FilterOption filterOption : filters) {
                if (previousFilter != null) {
                    final FilterOperationType prevOp = previousFilter.getFilterOperationType();
                    final FilterOperationType currOp = filterOption.getFilterOperationType();
                    // Auto add AND if previous operator was normal op or ')' and that current op is normal op or '(' :
                    if ((isNormalOperator(prevOp) || prevOp == R_PARENTHESIS) && (isNormalOperator(currOp) || currOp == L_PARENTHESIS)) {
                        stringQueryBuilder.append(" AND ");
                    }
                }
                final StringBuilder aliasBuilder = appendFilterClause(stringQueryBuilder, filterOption);
                if (aliasBuilder != null) {
                    specificFilters.add(aliasBuilder.toString());
                }
                previousFilter = filterOption;
            }
            stringQueryBuilder.append(")");
        }
        if (multipleFilter != null && multipleFilter.getTerms() != null && !multipleFilter.getTerms().isEmpty()) {
            handleMultipleFilters(stringQueryBuilder, multipleFilter, specificFilters, enableWordSearch);
        }
    }

    private StringBuilder appendFilterClause(final StringBuilder clause, final FilterOption filterOption) {
        final FilterOperationType type = filterOption.getFilterOperationType();
        StringBuilder completeField = null;
        if (filterOption.getPersistentClass() != null) {
            completeField = new StringBuilder(classAliasMappings.get(filterOption.getPersistentClass().getName())).append('.').append(
                    filterOption.getFieldName());
        }
        Object fieldValue = filterOption.getValue();
        fieldValue = processValue(fieldValue);
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
                final Object from = filterOption.getFrom() instanceof String ? "'" + escapeString((String) filterOption.getFrom()) + "'"
                        : filterOption.getFrom();
                final Object to = filterOption.getTo() instanceof String ? "'" + escapeString((String) filterOption.getTo()) + "'" : filterOption.getTo();
                clause.append("(").append(from).append(" <= ").append(completeField);
                clause.append(" AND ").append(completeField).append(" <= ").append(to).append(")");
                break;
            case LIKE:
                clause.append(completeField).append(" LIKE '%").append(escapeTerm((String) filterOption.getValue())).append("%'");
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
                break;
        }
        return completeField;
    }

    protected Object processValue(Object fieldValue) {
        if (fieldValue instanceof String) {
            fieldValue = "'" + escapeString((String) fieldValue) + "'";
        } else if (fieldValue instanceof EnumToObjectConvertible) {
            fieldValue = ((EnumToObjectConvertible) fieldValue).fromEnum();
        }
        return fieldValue;
    }

    private void handleMultipleFilters(final StringBuilder builder, final SearchFields multipleFilter, final Set<String> specificFilters,
            final boolean enableWordSearch) {
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
            applyFiltersOnQuery(builder, fields, terms, enableWordSearch);
        }
    }

    private void applyFiltersOnQuery(final StringBuilder queryBuilder, final Set<String> fields, final List<String> terms, final boolean enableWordSearch) {
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

    private void buildLikeClauseForOneFieldMultipleTerms(final StringBuilder queryBuilder, final String currentField, final List<String> terms,
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

    void buildLikeClauseForOneFieldOneTerm(final StringBuilder queryBuilder, final String currentField, final String currentTerm,
            final boolean enableWordSearch) {
        // Search if a sentence starts with the term
        queryBuilder.append(currentField).append(buildLikeEscapeClause(currentTerm, "", "%"));

        if (enableWordSearch) {
            // Search also if a word starts with the term
            // We do not want to search for %currentTerm% to ensure we can use Lucene-like library.
            queryBuilder.append(" OR ").append(currentField).append(buildLikeEscapeClause(currentTerm, "% ", "%"));
        }
    }

    /**
     * Get like clause for given term with escaped sql query wildcards and escape character
     */
    private String buildLikeEscapeClause(final String term, final String prefixPattern, final String suffixPattern) {
        return " LIKE '" + (prefixPattern != null ? prefixPattern : "") + escapeTerm(term) + (suffixPattern != null ? suffixPattern : "") + "' ESCAPE '"
                + likeEscapeCharacter + "'";
    }

    /*
     * escape for like
     */
    private final String escapeTerm(final String term) {
        // 1) escape ' character by adding another ' character
        // 2) protect escape character if this character is used in data
        // 3) escape % character (sql query wildcard) by adding escape character
        // 4) escape _ character (sql query wildcard) by adding escape character
        return term
                .replace("'", "''")
                .replace(likeEscapeCharacter, likeEscapeCharacter + likeEscapeCharacter)
                .replace("%", likeEscapeCharacter + "%")
                .replace("_", likeEscapeCharacter + "_");
    }

    /*
     * escape for other things than like
     */
    String escapeString(final String term) {
        // 1) escape ' character by adding another ' character
        return term
                .replaceAll("'", "''");
    }

    private String getInClause(final StringBuilder completeField, final FilterOption filterOption) {
        return completeField + " in (" +
                getInValues(filterOption) +
                ")";
    }

    private String getInValues(final FilterOption filterOption) {
        final StringBuilder stb = new StringBuilder();
        for (final Object element : filterOption.getIn()) {
            stb.append(element).append(",");
        }
        final String inValues = stb.toString();
        return inValues.substring(0, inValues.length() - 1);
    }

    void appendOrderByClause(List<OrderByOption> orderByOptions, Class<? extends PersistentObject> entityType) throws SBonitaReadException {
        stringQueryBuilder.append(" ORDER BY ");
        boolean startWithComma = false;
        boolean sortedById = false;
        for (final OrderByOption orderByOption : orderByOptions) {
            if (startWithComma) {
                stringQueryBuilder.append(',');
            }
            StringBuilder fieldNameBuilder = new StringBuilder();
            final Class<? extends PersistentObject> clazz = orderByOption.getClazz();
            if (clazz != null) {
                appendClassAlias(fieldNameBuilder, clazz);
            }
            final String fieldName = orderByOption.getFieldName();
            if ("id".equalsIgnoreCase(fieldName) || "sourceObjectId".equalsIgnoreCase(fieldName)) {
                sortedById = true;
            }
            fieldNameBuilder.append(fieldName);
            orderByBuilder.appendOrderBy(stringQueryBuilder, fieldNameBuilder.toString(), orderByOption.getOrderByType());
            startWithComma = true;
        }
        if (!sortedById) {
            if (startWithComma) {
                stringQueryBuilder.append(',');
            }
            appendClassAlias(stringQueryBuilder, entityType);
            stringQueryBuilder.append("id");
            stringQueryBuilder.append(' ');
            stringQueryBuilder.append("ASC");
        }
    }

    private void appendClassAlias(final StringBuilder builder, final Class<? extends PersistentObject> clazz) throws SBonitaReadException {
        final String className = clazz.getName();
        final String classAlias = classAliasMappings.get(className);
        if (classAlias == null || classAlias.trim().isEmpty()) {
            throw new SBonitaReadException("No class alias found for class " + className);
        }
        builder.append(classAlias);
        builder.append('.');
    }

    boolean hasChanged() {
        return !baseQuery.equals(stringQueryBuilder.toString());
    }

    abstract Query buildQuery(Session session);

    public abstract void setTenantId(Query query, long tenantId);
}
