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
package org.bonitasoft.web.rest.server.api.system;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import org.bonitasoft.console.common.server.auth.AuthenticationManagerProperties;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.toolkit.client.common.json.JSonSerializer;
import org.bonitasoft.web.toolkit.client.common.session.SessionDefinition;
import org.bonitasoft.web.toolkit.client.common.session.SessionItem;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;

/**
 * @author Julien Mege
 */
public class APISession extends ConsoleAPI<SessionItem> {

    final BonitaVersion bonitaVersion = new BonitaVersion(new VersionFile());

    @Override
    protected SessionDefinition defineItemDefinition() {
        return (SessionDefinition) Definitions.get(SessionDefinition.TOKEN);
    }

    @Override
    public SessionItem get(final APIID unusedId) {
        final org.bonitasoft.engine.session.APISession apiSession = getEngineSession();
        final SessionItem session = new SessionItem();

        if (apiSession != null) {
            session.setAttribute(SessionItem.ATTRIBUTE_SESSIONID, String.valueOf(apiSession.getId()));
            session.setAttribute(SessionItem.ATTRIBUTE_USERID, String.valueOf(apiSession.getUserId()));
            session.setAttribute(SessionItem.ATTRIBUTE_USERNAME, apiSession.getUserName());
            session.setAttribute(SessionItem.ATTRIBUTE_IS_TECHNICAL_USER, String.valueOf(apiSession.isTechnicalUser()));
            session.setAttribute(SessionItem.ATTRIBUTE_IS_GUEST_USER,
                    String.valueOf(isGuestUser(apiSession.getUserName())));
            session.setAttribute(SessionItem.ATTRIBUTE_VERSION, getVersion());
            session.setAttribute(SessionItem.ATTRIBUTE_BRANDING_VERSION, getBrandingVersion());
            session.setAttribute(SessionItem.ATTRIBUTE_BRANDING_VERSION_WITH_DATE, getBrandingVersionWithDate());
            session.setAttribute(SessionItem.ATTRIBUTE_COPYRIGHT, getCopyright());
            session.setAttribute(SessionItem.ATTRIBUTE_CONF, getLogoutConfiguration(apiSession));
        }
        return session;
    }

    protected boolean isGuestUser(final String loggedInUsername) {
        return false;
    }

    protected AuthenticationManagerProperties getAuthenticationManagerProperties() {
        return AuthenticationManagerProperties.getProperties();
    }

    public String getLogoutConfiguration(final org.bonitasoft.engine.session.APISession apiSession) {
        if (!apiSession.isTechnicalUser() && isLogoutDisabled()) {
            return JSonSerializer.serialize(singletonList(AuthenticationManagerProperties.LOGOUT_DISABLED));
        }
        return JSonSerializer.serialize(emptyList());
    }

    /**
     * enable to know if the logout button is visible or not
     */
    protected boolean isLogoutDisabled() {
        return getAuthenticationManagerProperties().isLogoutDisabled();
    }

    public String getVersion() {
        return bonitaVersion.getVersion();
    }

    public String getBrandingVersion() {
        return bonitaVersion.getBrandingVersion();
    }

    public String getBrandingVersionWithDate() {
        return bonitaVersion.getBrandingVersionWithUpdate();
    }

    public String getCopyright() {
        return bonitaVersion.getCopyright();
    }
}
