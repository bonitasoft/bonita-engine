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
package org.bonitasoft.engine.profile;

import org.bonitasoft.engine.search.SearchOptions;

/**
 * Defines the fields that can be used in the {@link org.bonitasoft.engine.search.SearchOptions} when searching for {@link Profile}s
 * 
 * @author Julien Mege
 * @author Celine Souchet
 * @see SearchOptions
 * @see Profile
 * @see org.bonitasoft.engine.api.ProfileAPI#searchProfiles(SearchOptions)
 */
public final class ProfileSearchDescriptor {

    /**
     * Used to filter or order by the {@link Profile} identifier
     * 
     * @see ProfileEntry
     */
    public static final String ID = "id";

    /**
     * Used to filter or order by the {@link Profile} name
     *
     * @see ProfileEntry
     */
    public static final String NAME = "name";

    /**
     * Used to filter or order by the flag {@link Profile#isDefault()}
     *
     * @see ProfileEntry
     */
    public static final String IS_DEFAULT = "isDefault";

    /**
     * Used to filter or order by the flag {@link ProfileEntry#getName()}
     *
     * @see ProfileEntry
     */
    public static final String PROFILE_ENTRY_NAME = "profileentry.name";

}
