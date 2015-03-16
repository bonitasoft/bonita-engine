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

import org.bonitasoft.engine.profile.Profile;


/**
 * Contains fields that can be used by {@link ApplicationCreator} and {@link ApplicationUpdater}
 *
 * @author Elias Ricken de Medeiros
 * @see ApplicationCreator
 * @see ApplicationUpdater
 */
public enum ApplicationField {

    /**
     * References the {@link Application} token
     *
     * @see Application
     */
    TOKEN,

    /**
     * References the {@link Application} display name
     *
     * @see Application
     */
    DISPLAY_NAME,

    /**
     * References the {@link Application} version
     *
     * @see Application
     */
    VERSION,

    /**
     * References the {@link Application} description
     *
     * @see Application
     */
    DESCRIPTION,

    /**
     * References the {@link Application} icon path
     *
     * @see Application
     */
    ICON_PATH,

    /**
     * References the {@link Application} state
     *
     * @see Application
     */
    STATE,

    /**
     * References the identifier of the {@link Profile} associated to the {@link Application}
     *
     * @see Application
     * @see Profile
     */
    PROFILE_ID,

    /**
     * References the identifier of the {@link ApplicationPage} defined as the {@link Application} home page
     * @see org.bonitasoft.engine.business.application.ApplicationPage
     * @see org.bonitasoft.engine.business.application.Application
     */
    HOME_PAGE_ID;

}
