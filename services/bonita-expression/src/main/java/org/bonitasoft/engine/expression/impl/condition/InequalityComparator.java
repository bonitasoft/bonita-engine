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
public class InequalityComparator {

    public <T> Integer compareTo(T left, T right) throws SComparisonException {
        if (left == null || right == null) {
            return null;
        }
        if (!(left instanceof Comparable) || !(right instanceof Comparable)) {
            throw new SComparisonException("The following class must implement java.lang.Comparable: " + left.getClass().getName());
        }
        return compare((Comparable) left, (Comparable) right);
    }

    private <T extends Comparable<Object>, R extends Comparable<Object>> Integer compare(final T left, final R right) {
        return left.compareTo(right);
    }

}
