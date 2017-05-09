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

package org.bonitasoft.engine.operation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bonitasoft.engine.commons.Container;

/**
 * Represents a Evaluation Context to reference / retrieve Business Data.
 * 
 * @author Elias Ricken de Medeiros
 */
public class BusinessDataContext {

    /**
     * name of the business data to retrieve
     */
    private String name;

    /**
     * Container on which to look for the business data (PROCESS of FLOWNODE)
     */
    private Container container;

    public BusinessDataContext(final String name, final Container container) {
        this.name = name;
        this.container = container;
    }

    public String getName() {
        return name;
    }

    public Container getContainer() {
        return container;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BusinessDataContext that = (BusinessDataContext) o;
        return new EqualsBuilder()
                .append(name, that.name)
                .append(container, that.container)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(container)
                .toHashCode();
    }
}
