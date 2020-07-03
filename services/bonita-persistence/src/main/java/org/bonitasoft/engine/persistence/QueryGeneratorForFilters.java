/**
 * Copyright (C) 2020 Bonitasoft S.A.
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

import static org.bonitasoft.engine.commons.Pair.pair;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.L_PARENTHESIS;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.R_PARENTHESIS;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.isNormalOperator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.commons.EnumToObjectConvertible;
import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.persistence.search.FilterOperationType;

class QueryGeneratorForFilters {

    private Map<String, String> classAliasMappings;
    private boolean useIntegerForBoolean;
    private String likeEscapeCharacter;

    QueryGeneratorForFilters(Map<String, String> classAliasMappings, boolean useIntegerForBoolean,
            char likeEscapeCharacter) {
        this.classAliasMappings = classAliasMappings;
        this.useIntegerForBoolean = useIntegerForBoolean;
        this.likeEscapeCharacter = String.valueOf(likeEscapeCharacter);
    }

    private Object processValue(Object fieldValue) {
        if (fieldValue instanceof String) {
            // 1) escape ' character by adding another ' character
            fieldValue = "'" + QueryBuilder.escapeString((String) fieldValue) + "'";
        } else if (fieldValue instanceof EnumToObjectConvertible) {
            fieldValue = ((EnumToObjectConvertible) fieldValue).fromEnum();
        } else if (fieldValue instanceof Boolean) {
            if (useIntegerForBoolean) {
                fieldValue = (Boolean) fieldValue ? 1 : 0;
            }
        }
        return fieldValue;
    }

    private StringBuilder appendFilterClause(final StringBuilder clause, final FilterOption filterOption) {
        final FilterOperationType type = filterOption.getFilterOperationType();
        StringBuilder completeField = null;
        if (filterOption.getPersistentClass() != null) {
            completeField = new StringBuilder(classAliasMappings.get(filterOption.getPersistentClass().getName()))
                    .append('.').append(
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
            case BETWEEN:
                // 1) escape ' character by adding another ' character
                final Object from = filterOption.getFrom() instanceof String
                        ? "'" + QueryBuilder.escapeString((String) filterOption.getFrom()) + "'"
                        : filterOption.getFrom();
                // 1) escape ' character by adding another ' character
                final Object to = filterOption.getTo() instanceof String
                        ? "'" + QueryBuilder.escapeString((String) filterOption.getTo()) + "'" : filterOption.getTo();
                clause.append("(").append(from).append(" <= ").append(completeField);
                clause.append(" AND ").append(completeField).append(" <= ").append(to).append(")");
                break;
            case LIKE:
                // 1) escape ' character by adding another ' character
                // 2) protect escape character if this character is used in data
                // 3) escape % character (sql query wildcard) by adding escape character
                // 4) escape _ character (sql query wildcard) by adding escape character
                clause.append(completeField).append(" LIKE '%")
                        .append(QueryBuilder.escapeTerm((String) filterOption.getValue(), likeEscapeCharacter))
                        .append("%'");
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

    /**
     * generate a HQL/SQL condition given the filters
     *
     * @return a tuple containing the genereted where clause and the fields it filters on
     */
    public Pair<String, Set<String>> generate(List<FilterOption> filters) {
        Set<String> specificFilters = new HashSet<>();
        FilterOption previousFilter = null;
        StringBuilder filtersStringBuilder = new StringBuilder();
        for (final FilterOption filterOption : filters) {
            if (previousFilter != null) {
                final FilterOperationType prevOp = previousFilter.getFilterOperationType();
                final FilterOperationType currOp = filterOption.getFilterOperationType();
                // Auto add AND if previous operator was normal op or ')' and that current op is normal op or '(' :
                if ((isNormalOperator(prevOp) || prevOp == R_PARENTHESIS)
                        && (isNormalOperator(currOp) || currOp == L_PARENTHESIS)) {
                    filtersStringBuilder.append(" AND ");
                }
            }
            final StringBuilder aliasBuilder = appendFilterClause(filtersStringBuilder, filterOption);
            if (aliasBuilder != null) {
                specificFilters.add(aliasBuilder.toString());
            }
            previousFilter = filterOption;
        }
        return pair(filtersStringBuilder.toString(), specificFilters);
    }
}
