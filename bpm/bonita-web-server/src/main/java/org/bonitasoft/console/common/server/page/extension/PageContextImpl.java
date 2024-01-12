/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.page.extension;

import java.util.Locale;

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.page.PageContext;

/**
 * This class provide access to the data relative to the context in which the custom page is displayed
 *
 * @author Anthony Birembaut
 */
public class PageContextImpl implements PageContext {

    protected final APISession apiSession;

    protected final Locale locale;

    protected final String profileID;

    public PageContextImpl(final APISession apiSession, final Locale locale, final String profileID) {
        super();
        this.apiSession = apiSession;
        this.locale = locale;
        this.profileID = profileID;
    }

    /**
     * @return the engine {@link APISession}
     */
    public APISession getApiSession() {
        return apiSession;
    }

    /**
     * @return the user locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @return the ID of the profile in which the page is currently displayed
     */
    public String getProfileID() {
        return profileID;
    }

}
