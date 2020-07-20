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

import static org.bonitasoft.engine.persistence.QueryBuilder.escapeTerm;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.L_PARENTHESIS;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.R_PARENTHESIS;
import static org.bonitasoft.engine.persistence.search.FilterOperationType.isNormalOperator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bonitasoft.engine.persistence.search.FilterOperationType;

class QueryGeneratorForFilters {

    private Map<String, String> classAliasMappings;
    private String likeEscapeCharacter;
    private int parameterCounter = 1;
    private Map<String, Object> parameters = new HashMap<>();

    QueryGeneratorForFilters(Map<String, String> classAliasMappings, char likeEscapeCharacter) {
        this.classAliasMappings = classAliasMappings;
        this.likeEscapeCharacter = String.valueOf(likeEscapeCharacter);
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
        switch (type) {
            case EQUALS:
                if (fieldValue == null) {
                    clause.append(completeField).append(" IS NULL");
                } else {
                    clause.append(completeField).append(" = ").append(createParameter(fieldValue));
                }
                break;
            case GREATER:
                clause.append(completeField).append(" > ").append(createParameter(fieldValue));
                break;
            case GREATER_OR_EQUALS:
                clause.append(completeField).append(" >= ").append(createParameter(fieldValue));
                break;
            case LESS:
                clause.append(completeField).append(" < ").append(createParameter(fieldValue));
                break;
            case LESS_OR_EQUALS:
                clause.append(completeField).append(" <= ").append(createParameter(fieldValue));
                break;
            case DIFFERENT:
                if (fieldValue == null) {
                    clause.append(completeField).append(" IS NOT NULL");
                } else {
                    clause.append(completeField).append(" != ").append(createParameter(fieldValue));
                }
                break;
            case BETWEEN:
                // eg. ('fromValue' <= p.myField AND p.myField <= 'toValue')
                clause.append("(").append(createParameter(filterOption.getFrom())).append(" <= ").append(completeField);
                clause.append(" AND ").append(completeField).append(" <= ")
                        .append(createParameter(filterOption.getTo())).append(")");
                break;
            case LIKE:
                clause.append(completeField).append(" LIKE ")
                        .append(createParameter(
                                "%" + escapeTerm((String) filterOption.getValue(), likeEscapeCharacter) + "%"))
                        .append(" ESCAPE '").append(likeEscapeCharacter).append("'");
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

    private String createParameter(Object fieldValue) {
        final String parameterName = "f" + parameterCounter++;
        parameters.put(parameterName, fieldValue);
        return ":" + parameterName;
    }

    /**
     * generate a HQL/SQL condition given the filters
     *
     * @return a tuple containing the genereted where clause and the fields it filters on
     */
    public QueryGeneratedFilters generate(List<FilterOption> filters) {
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
        return new QueryGeneratedFilters(filtersStringBuilder.toString(), specificFilters, parameters);
    }

    @Data
    @AllArgsConstructor
    static final class QueryGeneratedFilters {

        private String filters;
        private Set<String> specificFilters;
        private Map<String, Object> parameters;
    }
}
