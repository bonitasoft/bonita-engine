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
package org.bonitasoft.engine.search.impl;

import java.io.Serializable;

import org.bonitasoft.engine.exception.IncorrectParameterException;
import org.bonitasoft.engine.search.SearchFilterOperation;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SearchFilter implements Serializable {

    private static final long serialVersionUID = 2476946810762051485L;

    private String field;

    private SearchFilterOperation operation;

    private Serializable value;

    private Serializable from;

    private Serializable to;

    /**
     * @param field
     *        the field to filter on
     * @param operation
     *        the operation to filter on
     * @param value
     *        the value of the field to filter on
     * @see SearchFilterOperation
     */
    public SearchFilter(final String field, final SearchFilterOperation operation, final Serializable value) {
        this.field = field;
        this.operation = operation;
        this.value = value;
    }

    public SearchFilter(final String field, final Serializable from, final Serializable to) {
        this.field = field;
        operation = SearchFilterOperation.BETWEEN;
        this.from = from;
        this.to = to;
    }

    public SearchFilter(final SearchFilterOperation operation) throws IncorrectParameterException {
        this.operation = operation;
        if (!isUndefinedFieldNameAuthorized()) {
            throw new IncorrectParameterException(
                    "search operator can only be AND, OR, L_PARENTHESIS, R_PARENTHESIS on the one-parameter SearchFilter constructor");
        }
    }

    public boolean isUndefinedFieldNameAuthorized() {
        switch (operation) {
            case AND:
            case OR:
            case L_PARENTHESIS:
            case R_PARENTHESIS:
                return true;

            default:
                return false;
        }
    }

    /**
     * @return the field name
     */
    public String getField() {
        return field;
    }

    /**
     * @param field
     *        the field name to set
     */
    public void setField(final String field) {
        this.field = field;
    }

    /**
     * @return the operation
     */
    public SearchFilterOperation getOperation() {
        return operation;
    }

    /**
     * @param operation
     *        the operation to set
     */
    public void setOperation(final SearchFilterOperation operation) {
        this.operation = operation;
    }

    /**
     * @return the value
     */
    public Serializable getValue() {
        return value;
    }

    /**
     * @param value
     *        the value to set
     */
    public void setValue(final Serializable value) {
        this.value = value;
    }

    /**
     * @return the from
     */
    public Serializable getFrom() {
        return from;
    }

    /**
     * @return the to
     */
    public Serializable getTo() {
        return to;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (field == null ? 0 : field.hashCode());
        result = prime * result + (from == null ? 0 : from.hashCode());
        result = prime * result + (operation == null ? 0 : operation.hashCode());
        result = prime * result + (to == null ? 0 : to.hashCode());
        result = prime * result + (value == null ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SearchFilter other = (SearchFilter) obj;
        if (field == null) {
            if (other.field != null) {
                return false;
            }
        } else if (!field.equals(other.field)) {
            return false;
        }
        if (from == null) {
            if (other.from != null) {
                return false;
            }
        } else if (!from.equals(other.from)) {
            return false;
        }
        if (operation != other.operation) {
            return false;
        }
        if (to == null) {
            if (other.to != null) {
                return false;
            }
        } else if (!to.equals(other.to)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
