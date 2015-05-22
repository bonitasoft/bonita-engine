/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 */

package com.bonitasoft.engine.business.application;

import org.bonitasoft.engine.business.application.ApplicationField;
import org.bonitasoft.engine.page.Page;

/**
 * @author Elias Ricken de Medeiros
 * @since 7.0.0
 */
public class ApplicationCreatorExt extends org.bonitasoft.engine.business.application.ApplicationCreator {

    /**
     * Creates an instance of <code>ApplicationCreatorExt</code> containing mandatory information.
     * <p>The created {@code Application} will used the default layout and the default theme. If you want to set a specific layout/theme, use
     * {@link #ApplicationCreatorExt(String, String, String, long, long)} instead.</p>
     *
     * @param token the {@link org.bonitasoft.engine.business.application.Application} token. The token will be part of application URL. It cannot be null or
     *        empty and should contain only alpha numeric
     *        characters and the following special characters '-', '.', '_' or '~'.
     * @param displayName the <code>Application</code> display name. It cannot be null or empty
     * @param version the <code>Application</code> version
     * @see org.bonitasoft.engine.business.application.Application
     */
    public ApplicationCreatorExt(final String token, final String displayName, final String version) {
        super(token, displayName, version);
    }

    /**
     * Creates an instance of <code>ApplicationCreatorExt</code> containing mandatory information and a specific layout.
     *
     * @param token the {@link org.bonitasoft.engine.business.application.Application} token. The token will be part of application URL. It cannot be null or
     *        empty and should contain only alpha numeric
     *        characters and the following special characters '-', '.', '_' or '~'.
     * @param displayName the <code>Application</code> display name. It cannot be null or empty
     * @param version the <code>Application</code> version
     * @param layoutId the identifier of the {@link Page} to be used as {@code Application} layout
     * @param themeId
     * @see org.bonitasoft.engine.business.application.Application
     * @see Page
     */
    public ApplicationCreatorExt(final String token, final String displayName, final String version, final long layoutId, final long themeId) {
        this(token, displayName, version);
        getFields().put(ApplicationField.LAYOUT_ID, layoutId);
        getFields().put(ApplicationField.THEME_ID, themeId);
    }

}
