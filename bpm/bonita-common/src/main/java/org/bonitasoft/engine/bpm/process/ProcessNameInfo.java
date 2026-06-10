/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.bpm.process;

import java.io.Serializable;
import java.util.List;

/**
 * Aggregates the deployed versions of all processes sharing the same (name, displayName) combination.
 * <p>
 * This is the result item of
 * {@link org.bonitasoft.engine.api.ProcessManagementAPI#searchProcessNames(org.bonitasoft.engine.search.SearchOptions)}:
 * process deployment infos are grouped so that no two items share the same (name, displayName) pair, and each item
 * carries the list of versions matching the search criteria.
 *
 * @since 11.1.0
 */
public interface ProcessNameInfo extends Serializable {

    /**
     * @return the technical name shared by all the versions of this group.
     */
    String getName();

    /**
     * @return the display name shared by all the versions of this group.
     */
    String getDisplayName();

    /**
     * @return the deployed versions of this (name, displayName) group, in ascending lexical order (so {@code "10.0"}
     *         precedes {@code "2.0"}; the order is not semantic-version aware). The search term selects which groups
     *         are returned (it is not applied per version); the activationState filter, if any, is applied to the
     *         versions.
     */
    List<String> getVersions();
}
