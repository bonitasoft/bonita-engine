/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application;

    /**
     * Contains fields used by {@link ApplicationMenuCreator} and {@link ApplicationMenuUpdater}
     * @author Elias Ricken de Medeiros
     * @since 6.4
     * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.ApplicationMenuField} instead.
     * @see org.bonitasoft.engine.business.application.ApplicationMenuField
     */
    @Deprecated
    public enum ApplicationMenuField {

        /**
         * References the {@link ApplicationMenu} display name
         *
         * @see ApplicationMenu
         */
        DISPLAY_NAME,

        /**
         * References the identifier of {@link Application} related to the {@link ApplicationMenu}
         *
         * @see ApplicationMenu
         * @see Application
         */
        APPLICATION_ID,

        /**
         * References the identifier of {@link ApplicationPage} related to the {@link ApplicationMenu}
         *
         * @see ApplicationMenu
         * @see ApplicationPage
         */
        APPLICATION_PAGE_ID,

        /**
         * References the identifier of parent {@link ApplicationMenu}
         *
         * @see ApplicationMenu
         */
        PARENT_ID,

        /**
         * References the {@link ApplicationMenu} index
         *
         * @see ApplicationMenu
         */
        INDEX;

    }