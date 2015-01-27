/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.business.application;

import org.bonitasoft.engine.bpm.BaseElement;


/**
 * Represents an {@link Application} menu
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see Application
 */
public interface ApplicationMenu extends BaseElement {

    /**
     * Retrieves the {@code ApplicationMenu} display name
     *
     * @return the {@code ApplicationMenu} display name
     */
    String getDisplayName();

    /**
     * Retrieves the identifier of related {@link ApplicationPage}. If the {@code ApplicationMenu} is not related to an {@code ApplicationPage}, this method will return null.
     *
     * @return the identifier of related {@code ApplicationPage} or null if the menu is not related to {@code ApplicationPage}
     * @see ApplicationPage
     */
    Long getApplicationPageId();

    /**
     * Retrieves the identifier of related {@link Application}
     *
     * @return the identifier of related {@code Application}
     * @see Application
     */
    long getApplicationId();

    /**
     * Retrieves the identifier of the parent {@code ApplicationMenu}. If the menu does not have a parent menu, this method will return null.
     *
     * @return the identifier of the parent {@code ApplicationMenu} or null if the menu has no parent.
     */
    Long getParentId();

    /**
     * Retrieves the {@code ApplicationMenu} index
     *
     * @return the {@code ApplicationMenu} index
     */
    int getIndex();

}
