/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.connector;

import java.lang.reflect.Proxy;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.PermissionAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProcessConfigurationAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.api.impl.ClientInterceptor;
import org.bonitasoft.engine.api.impl.ServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ConnectorAPIAccessorImpl implements APIAccessor {

    private static final long serialVersionUID = 3365911149008207537L;

    private final long tenantId;

    private APISession apiSession;

    public ConnectorAPIAccessorImpl(final long tenantId) {
        super();
        this.tenantId = tenantId;
    }

    protected APISession getAPISession() {
        if (apiSession == null) {
            final TenantServiceAccessor tenantServiceAccessor = TenantServiceSingleton.getInstance(tenantId);
            final SessionAccessor sessionAccessor = tenantServiceAccessor.getSessionAccessor();
            final SessionService sessionService = tenantServiceAccessor.getSessionService();
            try {
                final SSession session = sessionService.createSession(tenantId, ConnectorAPIAccessorImpl.class.getSimpleName());// FIXME get the
                sessionAccessor.setSessionInfo(session.getId(), tenantId);
                apiSession = ModelConvertor.toAPISession(session, null);
            } catch (final BonitaRuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new BonitaRuntimeException(e);
            }
        }
        return apiSession;
    }

    @Override
    public IdentityAPI getIdentityAPI() {
        return getAPI(IdentityAPI.class, getAPISession());
    }

    @Override
    public ProcessAPI getProcessAPI() {
        return getAPI(ProcessAPI.class, getAPISession());
    }

    @Override
    public CommandAPI getCommandAPI() {
        return getAPI(CommandAPI.class, getAPISession());
    }

    @Override
    public ProfileAPI getProfileAPI() {
        return getAPI(ProfileAPI.class, getAPISession());
    }

    @Override
    public ThemeAPI getThemeAPI() {
        return getAPI(ThemeAPI.class, getAPISession());
    }

    @Override
    public PermissionAPI getPermissionAPI() {
        return getAPI(PermissionAPI.class, getAPISession());
    }

    @Override
    public PageAPI getCustomPageAPI() {
        return getAPI(PageAPI.class, getAPISession());
    }

    @Override
    public ApplicationAPI getLivingApplicationAPI() {
        return getAPI(ApplicationAPI.class, getAPISession());
    }

    @Override
    public ProcessConfigurationAPI getProcessConfigurationAPI() {
        return getAPI(ProcessConfigurationAPI.class, getAPISession());
    }

    @Override
    public BusinessDataAPI getBusinessDataAPI() {
        return getAPI(BusinessDataAPI.class, getAPISession());
    }

    private static ServerAPI getServerAPI() {
        return ServerAPIFactory.getServerAPI(false);
    }

    private static <T> T getAPI(final Class<T> clazz, final APISession session) {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor sessionInterceptor = new ClientInterceptor(clazz.getName(), serverAPI, session);
        return (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { clazz }, sessionInterceptor);
    }

}
