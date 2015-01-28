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
package org.bonitasoft.engine.business.application.impl.filter;

/**
 * @author Elias Ricken de Medeiros
 */
public class SelectRange {
    private int startIndex;
    private int maxResults;

    public SelectRange(int startIndex, int maxResults) {
        this.startIndex = startIndex;
        this.maxResults = maxResults;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectRange)) return false;

        SelectRange that = (SelectRange) o;

        if (maxResults != that.maxResults) return false;
        if (startIndex != that.startIndex) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = startIndex;
        result = 31 * result + maxResults;
        return result;
    }
}
