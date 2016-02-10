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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.bonitasoft.engine.persistence.search.FilterOperationType;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class FilterOption implements Serializable {

    private static final long serialVersionUID = -5043588187864921664L;

    private Class<? extends PersistentObject> persistentClass;

    private String fieldName;

    private Object value;

    private Object to;

    private Object from;

    private Collection<?> in;

    private FilterOperationType operationType;

    public FilterOption(final Class<? extends PersistentObject> persistentClass, final String fieldName, final Object value,
            final FilterOperationType operatorType) {
        this(persistentClass, fieldName);
        this.value = value;
        operationType = operatorType;
    }

    public FilterOption(final Class<? extends PersistentObject> persistentClass, final String fieldName, final Object value) {
        // EQUALS operation by default:
        this(persistentClass, fieldName, value, FilterOperationType.EQUALS);
    }

    public FilterOption(final Class<? extends PersistentObject> persistentClass, final String fieldName) {
        this.persistentClass = persistentClass;
        this.fieldName = fieldName;
    }

    public FilterOption(final Class<? extends PersistentObject> persistentClass, final String fieldName, final Object from, final Object to) {
        // EQUALS operation by default:
        this(persistentClass, fieldName, null, FilterOperationType.BETWEEN);
        this.from = from;
        this.to = to;
    }

    public FilterOption(final FilterOperationType operatorType) {
        operationType = operatorType;
    }

    public Class<? extends PersistentObject> getPersistentClass() {
        return persistentClass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }

    public Object getFrom() {
        return from;
    }

    public void setFrom(final Object from) {
        this.from = from;
    }

    public Collection<?> getIn() {
        return in;
    }

    public void setIn(final Collection<?> in) {
        this.in = in;
    }

    public void setFilterOperationType(final FilterOperationType operatorType) {
        operationType = operatorType;
    }

    public Object getTo() {
        return to;
    }

    public FilterOption equalsTo(final Object value) {
        this.value = value;
        operationType = FilterOperationType.EQUALS;
        return this;
    }

    public FilterOption like(final Object value) {
        this.value = value;
        operationType = FilterOperationType.LIKE;
        return this;
    }

    public FilterOption between(final Object from, final Object to) {
        this.from = from;
        this.to = to;
        operationType = FilterOperationType.BETWEEN;
        return this;
    }

    public FilterOption greaterThan(final Object value) {
        from = value;
        operationType = FilterOperationType.GREATER;
        return this;
    }

    public FilterOption lessThan(final Object value) {
        to = value;
        operationType = FilterOperationType.LESS;
        return this;
    }

    public FilterOption greaterThanOrEquals(final Object value) {
        from = value;
        operationType = FilterOperationType.GREATER_OR_EQUALS;
        return this;
    }

    public FilterOption lessThanOrEquals(final Object value) {
        to = value;
        operationType = FilterOperationType.LESS_OR_EQUALS;
        return this;
    }

    public FilterOption in(final Collection<?> values) {
        in = values;
        operationType = FilterOperationType.IN;
        return this;
    }

    public FilterOption in(final Object... value) {
        in = Arrays.asList(value);
        operationType = FilterOperationType.IN;
        return this;
    }

    public FilterOperationType getFilterOperationType() {
        return operationType;
    }

    public static FilterOption leftParenthesis() {
        return new FilterOption(FilterOperationType.L_PARENTHESIS);
    }

    public static FilterOption rightParenthesis() {
        return new FilterOption(FilterOperationType.R_PARENTHESIS);
    }

    public static FilterOption or() {
        return new FilterOption(FilterOperationType.OR);
    }

    public static FilterOption and() {
        return new FilterOption(FilterOperationType.AND);
    }

    public Collection<?> getValues() {
        return in;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterOption)) return false;

        FilterOption that = (FilterOption) o;

        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) return false;
        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        if (in != null ? !in.equals(that.in) : that.in != null) return false;
        if (operationType != that.operationType) return false;
        if (persistentClass != null ? !persistentClass.equals(that.persistentClass) : that.persistentClass != null)
            return false;
        if (to != null ? !to.equals(that.to) : that.to != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = persistentClass != null ? persistentClass.hashCode() : 0;
        result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (in != null ? in.hashCode() : 0);
        result = 31 * result + (operationType != null ? operationType.hashCode() : 0);
        return result;
    }
}
