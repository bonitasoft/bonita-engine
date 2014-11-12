/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import org.bonitasoft.engine.bpm.BaseElement;

import com.bonitasoft.engine.page.Page;

/**
 * Represents the association between a {@link Page} and an {@link Application}
 *
 * @author Elias Ricken de Medeiros
 */
public interface ApplicationPage extends BaseElement {

    /**
     * Retrieves the {@link Application} identifier
     *
     * @return the <code>Application<code> identifier
     * @see Application
     */
    long getApplicationId();

    /**
     * Retrieves the {@link Page} identifier
     *
     * @return the <code>Page</code> identifier
     * @see Page
     */
    long getPageId();

    /**
     * Retrieves the <code>ApplicationPage</code> token
     *
     * @return the <code>ApplicationPage</code> token
     */
    String getToken();

}
