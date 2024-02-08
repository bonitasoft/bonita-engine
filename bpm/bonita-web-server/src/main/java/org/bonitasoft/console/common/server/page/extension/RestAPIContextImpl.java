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

import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.page.PageResourceProvider;
import org.bonitasoft.web.extension.rest.RestAPIContext;

public class RestAPIContextImpl implements RestAPIContext {

    private final APISession apiSession;
    private final Locale locale;
    private final PageResourceProvider resourceProvider;
    private final APIClient apiClient;

    public RestAPIContextImpl(final APISession apiSession, final APIClient apiClient, final Locale locale,
            PageResourceProvider resourceProvider) {
        this.apiSession = apiSession;
        this.locale = locale;
        this.resourceProvider = resourceProvider;
        this.apiClient = apiClient;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.console.common.server.page.RestAPIContext#getApiSession()
     */
    @Override
    public APISession getApiSession() {
        return apiSession;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.console.common.server.page.RestAPIContext#getLocale()
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.console.common.server.page.RestAPIContext#getResourceProvider()
     */
    @Override
    public PageResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    @Override
    public APIClient getApiClient() {
        return apiClient;
    }

}
