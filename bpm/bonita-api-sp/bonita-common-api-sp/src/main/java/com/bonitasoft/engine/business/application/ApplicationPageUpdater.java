/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationPageUpdater {

    /**
     * Contains fields used by {@code com.bonitasoft.engine.business.application.ApplicationPageUpdater}
     * */
    public enum ApplicationPageField {
        /**
         * References the {@link ApplicationPage} token
         * @see com.bonitasoft.engine.business.application.ApplicationPage
         * */
        TOKEN,

        /**
         * References the identifier of related {@link com.bonitasoft.engine.page.Page} {@link ApplicationPage} token
         * @see com.bonitasoft.engine.page.Page
         * @see com.bonitasoft.engine.business.application.ApplicationPage
         * */
        PAGE_ID;
    }


    private final Map<ApplicationPageField, Serializable> fields;

    /**
     * Creates an instance of {@code com.bonitasoft.engine.business.application.ApplicationPageUpdater}
     */
    public ApplicationPageUpdater() {
        fields = new HashMap<ApplicationPageField, Serializable>(2);
    }

    /**
     * Defines the new value for the {@link Application} token.  It cannot be empty or null and should contain only alpha numeric
     *         characters and the following special characters '-', '.', '_' or '~'.
     *
     * @param token the new value for the {@code Application} token
     * @return the current {@code ApplicationUpdater}
     * @see Application
     */
    public ApplicationPageUpdater setApplicationPageToken(String token) {
        fields.put(ApplicationPageField.TOKEN, token);
        return this;
    }

    public ApplicationPageUpdater setPageId(Long pageId) {
        fields.put(ApplicationPageField.PAGE_ID, pageId);
        return this;
    }

    public Map<ApplicationPageField, Serializable> getFields() {
        return fields;
    }
}
