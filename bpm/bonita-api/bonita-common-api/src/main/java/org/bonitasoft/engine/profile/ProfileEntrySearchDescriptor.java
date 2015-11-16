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
 * Defines the fields that can be used in the {@link org.bonitasoft.engine.search.SearchOptions} when searching for {@link ProfileEntry}s
 * 
 * @author Celine Souchet
 * @see org.bonitasoft.engine.search.SearchOptions
 * @see ProfileEntry
 * @see org.bonitasoft.engine.api.ProfileAPI#searchProfileEntries(SearchOptions)
 */
public final class ProfileEntrySearchDescriptor {

    /**
     * Used to filter or order by the {@link ProfileEntry} identifier
     * @see ProfileEntry
     */
    public static final String ID = "id";

    /**
     * Used to filter or order by the {@link ProfileEntry} index
     * @see ProfileEntry
     */
    public static final String INDEX = "index";

    /**
     * Used to filter or order by the {@link ProfileEntry} name
     * @see ProfileEntry
     */
    public static final String NAME = "name";

    /**
     * Used to filter or order by the identifier of the related {@link Profile}
     * @see Profile
     */
    public static final String PROFILE_ID = "profileId";

    /**
     * Used to filter or order by the identifier of the parent {@link ProfileEntry}
     * @see ProfileEntry
     */
    public static final String PARENT_ID = "parentId";

    /**
     * Used to filter or order by the name of the page referenced by the profile {@link ProfileEntry}.
     * @see ProfileEntry#getPage()
     */
    public static final String PAGE = "page";

    /**
     * Used to filter or order by the flag {@link ProfileEntry#isCustom()}
     * @see ProfileEntry
     */
    public static final String CUSTOM = "custom";

}
