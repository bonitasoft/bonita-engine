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
package org.bonitasoft.engine.business.application;

import org.bonitasoft.engine.search.SearchOptions;

/**
 * Defines the fields that can be used in the {@link org.bonitasoft.engine.search.SearchOptions} when searching for {@link Application}s
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see org.bonitasoft.engine.search.SearchOptions
 * @see Application
 * @see org.bonitasoft.engine.api.ApplicationAPI#searchApplications(SearchOptions)
 */
public class ApplicationSearchDescriptor {

    /**
     * Used to filter or order by the <code>Application</code> identifier
     */
    public static final String ID = "id";

    /**
     * Used to filter or order by the <code>Application</code> token
     */
    public static final String TOKEN = "token";

    /**
     * Used to filter or order by the <code>Application</code> display name
     */
    public static final String DISPLAY_NAME = "displayName";

    /**
     * Used to filter or order by the <code>Application</code> version
     */
    public static final String VERSION = "version";

    /**
     * Used to filter or order by the <code>Application</code> icon path
     */
    public static final String ICON_PATH = "iconPath";

    /**
     * Used to filter or order by the <code>Application</code> creation date
     */
    public static final String CREATION_DATE = "creationDate";

    /**
     * Used to filter or order by the identifier of the user that created the <code>Application</code>
     */
    public static final String CREATED_BY = "createdBy";

    /**
     * Used to filter or order by the <code>Application</code> last update date
     */
    public static final String LAST_UPDATE_DATE = "lastUpdateDate";

    /**
     * Used to filter or order by the identifier of the user that last updated the <code>Application</code>
     */
    public static final String UPDATED_BY = "updatedBy";

    /**
     * Used to filter or order by the <code>Application</code> state. The possible values are {@link ApplicationState#ACTIVATED#name()} and
     * {@link ApplicationState#DEACTIVATED#name()}
     *
     * @see ApplicationState
     */
    public static final String STATE = "state";

    /**
     * Used to filter or order by the identifier of {@link org.bonitasoft.engine.profile.Profile} associated to the
     * {@link org.bonitasoft.engine.business.application.Application}.
     *
     * @see org.bonitasoft.engine.profile.Profile
     * @see org.bonitasoft.engine.business.application.Application
     */
    public static final String PROFILE_ID = "profileId";

}
