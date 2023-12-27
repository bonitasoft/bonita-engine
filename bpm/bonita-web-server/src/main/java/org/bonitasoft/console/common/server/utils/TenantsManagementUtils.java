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
package org.bonitasoft.console.common.server.utils;

import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.profile.ProfileCriterion;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;

/**
 * Tenant management utils class
 *
 * @author Anthony Birembaut
 */
public class TenantsManagementUtils {

    public static long defaultTenantId = -1;

    /**
     * Check for user's profile
     */
    public static boolean hasProfileForUser(final APISession apiSession)
            throws InvalidSessionException, BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException {
        return !getProfileApi(apiSession).getProfilesForUser(apiSession.getUserId(), 0, 1, ProfileCriterion.ID_ASC)
                .isEmpty();
    }

    private static ProfileAPI getProfileApi(final APISession session)
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return TenantAPIAccessor.getProfileAPI(session);
    }

    /**
     * Get default tenant ID
     *
     * @throws DefaultTenantIdException
     *         If default tenant id couldn't be retrieved
     */
    public static long getDefaultTenantId() {
        if (defaultTenantId == -1) { // Lazy init
            try {
                final APISession session = TenantAPIAccessor.getLoginAPI().login(getTechnicalUserUsername(),
                        getTechnicalUserPassword());
                defaultTenantId = session.getTenantId();
                TenantAPIAccessor.getLoginAPI().logout(session);
            } catch (final Exception e) {
                throw new DefaultTenantIdException(e);
            }
        }
        return defaultTenantId;
    }

    public static String getTechnicalUserUsername() {
        return PropertiesFactory.getPlatformTenantConfigProperties().defaultTenantUserName();
    }

    public static String getTechnicalUserPassword() {
        return PropertiesFactory.getPlatformTenantConfigProperties().defaultTenantPassword();
    }

    public static boolean isDefaultTenantPaused() throws Exception {
        APISession apiSession = null;
        try {
            apiSession = TenantAPIAccessor.getLoginAPI().login(getTechnicalUserUsername(), getTechnicalUserPassword());
            return TenantAPIAccessor.getTenantAdministrationAPI(apiSession).isPaused();
        } finally {
            if (apiSession != null) {
                TenantAPIAccessor.getLoginAPI().logout(apiSession);
            }
        }
    }
}
