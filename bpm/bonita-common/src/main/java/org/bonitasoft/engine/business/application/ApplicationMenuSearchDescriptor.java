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
 * Defines the fields that can be used in the {@link SearchOptions} when searching for {@link ApplicationMenu}s
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see SearchOptions
 * @see ApplicationMenu
 * @see ApplicationAPI#searchApplicationMenus(SearchOptions)
 */
public class ApplicationMenuSearchDescriptor {

    /**
     * Used to filter or order by {@link ApplicationMenu} identifier
     *
     * @see ApplicationMenu
     */
    public static final String ID = "id";

    /**
     * Used to filter or order by {@link ApplicationMenu} display name
     *
     * @see ApplicationMenu
     */
    public static final String DISPLAY_NAME = "displayName";

    /**
     * Used to filter or order by the identifier of {@link ApplicationPage} related to the {@link ApplicationMenu}
     *
     * @see ApplicationMenu
     * @see ApplicationPage
     */
    public static final String APPLICATION_PAGE_ID = "applicationPageId";

    /**
     * Used to filter or order by the identifier of {@link Application} containing the {@link ApplicationMenu}
     *
     * @see ApplicationMenu
     * @see Application
     */
    public static final String APPLICATION_ID = "applicationId";

    /**
     * Used to filter or order by {@link ApplicationMenu} index
     *
     * @see ApplicationMenu
     */
    public static final String INDEX = "index";

    /**
     * Used to filter or order by the identifier of parent {@link ApplicationMenu}
     *
     * @see ApplicationMenu
     */
    public static final String PARENT_ID = "parentId";

}
