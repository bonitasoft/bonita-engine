/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.business.application;

import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.search.SearchOptions;

/**
 * Defines the fields that can be used in the {@link SearchOptions} when searching for {@link ApplicationPage}s
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see ApplicationPage
 * @see SearchOptions
 * @see ApplicationAPI#searchApplicationPages(SearchOptions)
 */
public class ApplicationPageSearchDescriptor {

    /**
     * Used to filter or order by {@link ApplicationPage} identifier
     *
     * @see ApplicationPage
     */
    public static final String ID = "id";

    /**
     * Used to filter or order by {@link ApplicationPage} token
     *
     * @see ApplicationPage
     */
    public static final String TOKEN = "token";

    /**
     * Used to filter or order by the identifier of {@link Application} associated to the {@link ApplicationPage}
     *
     * @see ApplicationPage
     * @see Application
     */
    public static final String APPLICATION_ID = "applicationId";

    /**
     * Used to filter or order by the identifier of {@link Page} referenced by the {@link ApplicationPage}
     *
     * @see ApplicationPage
     * @see Page
     */
    public static final String PAGE_ID = "pageId";

}
