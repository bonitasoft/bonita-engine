/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.business.application;

/**
 * Allows to define which {@link org.bonitasoft.engine.business.application.Application} fields will be updated
 * 
 * @author Elias Ricken de Medeiros
 * @since 7.0.0
 */
public class ApplicationUpdaterExt extends org.bonitasoft.engine.business.application.ApplicationUpdater {

    /**
     * Defines the identifier of the new {@link org.bonitasoft.engine.page.Page} used as the {@link org.bonitasoft.engine.business.application.Application}
     * layout.
     *
     * @param layoutId the identifier of {@code Page} used as layout
     * @return the current {@code ApplicationUpdater}
     * @see org.bonitasoft.engine.page.Page
     * @see org.bonitasoft.engine.business.application.Application
     */
    public ApplicationUpdaterExt setLayoutId(final Long layoutId) {
        getFields().put(org.bonitasoft.engine.business.application.ApplicationField.LAYOUT_ID, layoutId);
        return this;
    }

}
