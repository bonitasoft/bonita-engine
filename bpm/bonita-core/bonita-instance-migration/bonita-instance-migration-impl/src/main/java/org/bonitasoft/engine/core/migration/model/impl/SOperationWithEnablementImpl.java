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
package org.bonitasoft.engine.core.migration.model.impl;

import org.bonitasoft.engine.core.migration.model.SOperationWithEnablement;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 */
public class SOperationWithEnablementImpl implements SOperationWithEnablement {

    private static final long serialVersionUID = -2346808884014653919L;

    private final SExpression expression;

    private final SOperation operation;

    /**
     * @param expression
     * @param operation
     */
    public SOperationWithEnablementImpl(final SExpression expression, final SOperation operation) {
        super();
        this.expression = expression;
        this.operation = operation;
    }

    @Override
    public SOperation getOperation() {
        return operation;
    }

    @Override
    public SExpression getEnablement() {
        return expression;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (expression == null ? 0 : expression.hashCode());
        result = prime * result + (operation == null ? 0 : operation.hashCode());
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
        final SOperationWithEnablementImpl other = (SOperationWithEnablementImpl) obj;
        if (expression == null) {
            if (other.expression != null) {
                return false;
            }
        } else if (!expression.equals(other.expression)) {
            return false;
        }
        if (operation == null) {
            if (other.operation != null) {
                return false;
            }
        } else if (!operation.equals(other.operation)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "OperationWithEnablementImpl [expression=" + expression + ", operation=" + operation + "]";
    }

}
