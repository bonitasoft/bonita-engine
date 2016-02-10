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
package org.bonitasoft.engine.search.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SearchFields;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.search.Sort;
import org.bonitasoft.engine.search.impl.SearchFilter;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class SearchEntityDescriptor {

    public FilterOption getEntityFilter(final SearchFilter filter) {
        final String key = filter.getField();
        final FieldDescriptor fieldDescriptor = getEntityKeys().get(key);
        if (fieldDescriptor == null && !filter.isUndefinedFieldNameAuthorized()) {
            throw new IllegalArgumentException("the field '" + key + "' is unknown for the entity searched using " + this.getClass().getSimpleName());
        }
        return constructFilterOption(filter, fieldDescriptor);
    }

    public OrderByOption getEntityOrder(final Sort sort) throws SBonitaReadException {
        final FieldDescriptor fieldDescriptor = getEntityKeys().get(sort.getField());
        if (fieldDescriptor == null) {
            throw new SBonitaReadException("Invalid sort key: " + sort.getField());
        }
        final OrderByType type = OrderByType.valueOf(sort.getOrder().name());
        return new OrderByOption(fieldDescriptor.getPersistentClass(), fieldDescriptor.getValue(), type);
    }

    public SearchFields getEntitySearchTerm(final String searchString) {
        final StringTokenizer tokens = new StringTokenizer(searchString, " ");
        final ArrayList<String> terms = new ArrayList<String>(tokens.countTokens());
        while (tokens.hasMoreTokens()) {
            final String term = tokens.nextToken();
            terms.add(term);
        }

        return new SearchFields(terms, getAllFields());
    }

    protected abstract Map<String, FieldDescriptor> getEntityKeys();

    protected abstract Map<Class<? extends PersistentObject>, Set<String>> getAllFields();

    /**
     * Override this method to have specific conversion behavior from client filter value to server filter value .
     *
     * @param filterField
     *        The field to filter
     * @param filterValue
     *        The initial value
     * @return the converted filter value
     * @since 6.4.0
     */
    protected Serializable convertFilterValue(final String filterField, final Serializable filterValue) {
        return filterValue;
    }

    public FilterOption constructFilterOption(final SearchFilter filter, final FieldDescriptor fieldDescriptor) {
        final Class<? extends PersistentObject> clazz = fieldDescriptor != null ? fieldDescriptor.getPersistentClass() : null;
        final String fieldName = fieldDescriptor != null ? fieldDescriptor.getValue() : null;
        final Serializable value = convertFilterValue(filter.getField(), filter.getValue());
        switch (filter.getOperation()) {
            case BETWEEN:
                return new FilterOption(clazz, fieldName, convertFilterValue(filter.getField(), filter.getFrom()), convertFilterValue(filter.getField(),
                        filter.getTo()));
            case DIFFERENT:
                return new FilterOption(clazz, fieldName, value, FilterOperationType.DIFFERENT);
            case EQUALS:
                return new FilterOption(clazz, fieldName, value, FilterOperationType.EQUALS);
            case GREATER_OR_EQUAL:
                return new FilterOption(clazz, fieldName, value, FilterOperationType.GREATER_OR_EQUALS);
            case GREATER_THAN:
                return new FilterOption(clazz, fieldName, value, FilterOperationType.GREATER);
            case LESS_OR_EQUAL:
                return new FilterOption(clazz, fieldName, value, FilterOperationType.LESS_OR_EQUALS);
            case LESS_THAN:
                return new FilterOption(clazz, fieldName, value, FilterOperationType.LESS);
            case AND:
                return new FilterOption(FilterOperationType.AND);
            case OR:
                return new FilterOption(FilterOperationType.OR);
            case L_PARENTHESIS:
                return new FilterOption(FilterOperationType.L_PARENTHESIS);
            case R_PARENTHESIS:
                return new FilterOption(FilterOperationType.R_PARENTHESIS);
            default:
                return null;
        }
    }

}
