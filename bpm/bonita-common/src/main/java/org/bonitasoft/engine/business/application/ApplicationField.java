/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import org.bonitasoft.engine.profile.Profile;

/**
 * Contains fields that can be used by {@link ApplicationCreator} and {@link ApplicationUpdater}
 *
 * @author Elias Ricken de Medeiros
 * @see ApplicationCreator
 * @see ApplicationUpdater
 * @since 7.0.0
 * @deprecated This class should no longer be used. Since 9.0.0, Applications should be updated at startup.
 */
@Deprecated(since = "10.2.0")
public enum ApplicationField {

    /**
     * References the {@link IApplication} token
     *
     * @see IApplication
     */
    TOKEN,

    /**
     * References the {@link IApplication} display name
     *
     * @see IApplication
     */
    DISPLAY_NAME,

    /**
     * References the {@link IApplication} version
     *
     * @see IApplication
     */
    VERSION,

    /**
     * References the {@link IApplication} description
     *
     * @see IApplication
     */
    DESCRIPTION,

    /**
     * References the {@link IApplication} icon path
     *
     * @see IApplication
     * @deprecated since 7.13.0, use {@link #ICON_CONTENT} and {@link #ICON_FILE_NAME} instead
     */
    @Deprecated(since = "7.13.0")
    ICON_PATH,

    /**
     * byte array content of the icon of the {@link IApplication}
     *
     * @since 7.13.0
     */
    ICON_CONTENT,

    /**
     * Filename of the icon of the {@link IApplication}
     *
     * @since 7.13.0
     */
    ICON_FILE_NAME,

    /**
     * References the {@link IApplication} state
     *
     * @see IApplication
     */
    STATE,

    /**
     * References the identifier of the {@link Profile} associated to the {@link IApplication}
     *
     * @see IApplication
     * @see Profile
     */
    PROFILE_ID,

    /**
     * References the identifier of the {@link ApplicationPage} defined as the {@link Application} home page
     *
     * @see org.bonitasoft.engine.business.application.ApplicationPage
     * @see org.bonitasoft.engine.business.application.Application
     */
    HOME_PAGE_ID(Application.class),

    /**
     * References the identifier of the {@link org.bonitasoft.engine.page.Page} defined as the {@link Application}
     * layout.
     *
     * @see org.bonitasoft.engine.page.Page
     * @see org.bonitasoft.engine.business.application.Application
     * @since 7.0.0
     */
    LAYOUT_ID(Application.class),

    /**
     * References the identifier of the {@link org.bonitasoft.engine.page.Page} defined as the {@link Application}
     * theme.
     *
     * @see org.bonitasoft.engine.page.Page
     * @see org.bonitasoft.engine.business.application.Application
     * @since 7.0.0
     */
    THEME_ID(Application.class);

    /** The class which support this type of field */
    private Class<? extends IApplication> supportingClass;

    /**
     * Private Constructor for fields which are suitable for all application types.
     */
    private ApplicationField() {
        this(IApplication.class);
    }

    /**
     * Private Constructor for fields which are suitable only for a particular application type (e.g. Legacy, but not
     * Link).
     *
     * @param appropriateClazz the class which support this type of field.
     */
    private ApplicationField(Class<? extends IApplication> appropriateClazz) {
        supportingClass = appropriateClazz;
    }

    /**
     * Test whether this application field is suitable for a particular application type
     *
     * @param clazz the application type to test (usually {@link Application} for legacy applications of
     *        {@link ApplicationLink})
     * @return
     */
    public boolean isForClass(Class<? extends IApplication> clazz) {
        return supportingClass.isAssignableFrom(clazz);
    }

}
