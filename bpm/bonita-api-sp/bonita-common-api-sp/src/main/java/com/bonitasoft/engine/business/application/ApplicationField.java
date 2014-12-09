/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

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
     * @see com.bonitasoft.engine.business.application.ApplicationPage
     * @see com.bonitasoft.engine.business.application.Application
     */
    HOME_PAGE_ID;

}
