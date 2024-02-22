/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.data;

/**
 * A <code>SimpleBusinessDataReference</code> is a reference of a
 * {@link org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition} which is not multiple.
 *
 * @author Matthieu Chaffotte
 */
public interface SimpleBusinessDataReference extends BusinessDataReference {

    /**
     * Returns the identifier of the business data.
     * It can be null, if no business data is attached to the reference.
     *
     * @return the identifier of the business data
     */
    Long getStorageId();

    /**
     * Returns the identifier of the business data.
     * It can be null, if no business data is attached to the reference.
     *
     * @return the identifier of the business data as String
     */
    String getStorageIdAsString();

}
