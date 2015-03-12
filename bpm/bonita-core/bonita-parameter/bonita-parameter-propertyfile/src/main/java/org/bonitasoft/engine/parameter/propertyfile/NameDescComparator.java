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
package org.bonitasoft.engine.parameter.propertyfile;

import java.util.Comparator;

import org.bonitasoft.engine.parameter.SParameter;

/**
 * @author Matthieu Chaffotte
 */
public class NameDescComparator implements Comparator<SParameter> {

    private final String nullValue;

    public NameDescComparator(final String nullValue) {
        this.nullValue = nullValue;
    }

    @Override
    public int compare(final SParameter o1, final SParameter o2) {
        final String name1 = o1.getName();
        final String name2 = o2.getName();
        if (nullValue.equals(name1)) {
            return -1;
        }
        if (nullValue.equals(name2)) {
            return 1;
        }
        return name2.compareTo(name1);
    }

}
