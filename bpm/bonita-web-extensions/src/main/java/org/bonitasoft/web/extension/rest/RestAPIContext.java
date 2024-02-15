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
package org.bonitasoft.web.extension.rest;

import java.util.Locale;

import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.ResourceProvider;

/**
 * This class provide access to the data relative to the context in which the Rest API extension is called
 *
 * @since 7.2.0
 */
public interface RestAPIContext {

    /**
     * The {@link APIClient} is used to access business data and Bonita APIs such as:
     * <ul>
     * <li>{@link IdentityAPI},</li>
     * <li>{@link ProcessAPI},</li>
     * <li>...</li>
     * </ul>
     *
     * @return an engine {@link APIClient} logged to the current {@link APISession}
     */
    APIClient getApiClient();

    /**
     * @return Current engine {@link APISession}
     */
    APISession getApiSession();

    /**
     * @return Current selected {@link Locale} in BonitaBPM Portal
     */
    Locale getLocale();

    /**
     * @return a {@link ResourceProvider} to retrieve resources location
     */
    ResourceProvider getResourceProvider();

}
