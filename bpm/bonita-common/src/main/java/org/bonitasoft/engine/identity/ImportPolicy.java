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
package org.bonitasoft.engine.identity;

/**
 * Define how to handle conflicts when an organization is imported.
 * <br>
 * Check {@link org.bonitasoft.engine.api.OrganizationAPI#importOrganization(String)} and {@link org.bonitasoft.engine.api.OrganizationAPI#importOrganization(String, ImportPolicy)} to see the usage.
 *
 * @see org.bonitasoft.engine.api.OrganizationAPI
 * @author Baptiste Mesta
 * @since 6.0.0
 */
public enum ImportPolicy {

    /**
     * Existing items in current organization are updated to have the values of the item in the given organization
     */
    MERGE_DUPLICATES,

    /**
     * If an item already exists the import fail and is reverted to previous state
     */
    FAIL_ON_DUPLICATES,

    /**
     * Existing items are kept
     */
    IGNORE_DUPLICATES;

}
