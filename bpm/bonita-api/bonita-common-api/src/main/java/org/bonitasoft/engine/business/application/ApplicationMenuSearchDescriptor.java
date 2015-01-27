/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
