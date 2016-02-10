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
package org.bonitasoft.engine.persistence.search;

/**
 * @author Emmanuel Duchastenier
 */
public enum FilterOperationType {
    IN, BETWEEN, EQUALS, LIKE, GREATER, LESS, GREATER_OR_EQUALS, LESS_OR_EQUALS, DIFFERENT, L_PARENTHESIS, R_PARENTHESIS, AND, OR;

    public static boolean isNormalOperator(final FilterOperationType type) {
        return type == IN || type == BETWEEN || type == EQUALS || type == LIKE || type == GREATER || type == LESS || type == GREATER_OR_EQUALS
                || type == LESS_OR_EQUALS || type == DIFFERENT;
    }

    public static boolean isLinkOperator(final FilterOperationType type) {
        return type == L_PARENTHESIS || type == R_PARENTHESIS || type == AND || type == OR;
    }
}
