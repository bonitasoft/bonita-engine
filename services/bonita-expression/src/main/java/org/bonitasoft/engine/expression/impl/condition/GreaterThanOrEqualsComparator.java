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
package org.bonitasoft.engine.expression.impl.condition;

/**
 * @author Elias Ricken de Medeiros
 */
public class GreaterThanOrEqualsComparator implements BinaryComparator {

    private final InequalityComparator comparator;

    public GreaterThanOrEqualsComparator(InequalityComparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public <T> Boolean evaluate(final T left, final T right) throws SComparisonException {
        Integer compare = comparator.compareTo(left, right);
        return compare == null ? null : compare >= 0;
    }

}
