/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.search;

import java.io.Serializable;

/**
 * @author Matthieu Chaffotte
 */
public class Sort implements Serializable {

    private static final long serialVersionUID = 8165006330270193316L;

    private final Order order;

    private final String field;

    public Sort(final Order order, final String field) {
        super();
        this.order = order;
        this.field = field;
    }

    public Order getOrder() {
        return order;
    }

    public String getField() {
        return field;
    }

}
