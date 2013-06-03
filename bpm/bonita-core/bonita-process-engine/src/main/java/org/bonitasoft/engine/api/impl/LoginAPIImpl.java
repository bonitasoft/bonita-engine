/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import java.io.IOException;

import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.impl.transaction.identity.GetSUser;
import org.bonitasoft.engine.api.impl.transaction.identity.UpdateUser;
import org.bonitasoft.engine.api.impl.transaction.platform.GetDefaultTenantInstance;
import org.bonitasoft.engine.api.impl.transaction.platform.Login;
import org.bonitasoft.engine.api.impl.transaction.platform.Logout;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.UserUpdateBuilder;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Zhang Bole
 */
public class LoginAPIImpl implements LoginAPI {

    @Override
    public APISession login(final String userName, final String password) throws LoginException {
        checkUsernameAndPassword(userName, password);
        final STenant tenant = getDefaultTenant();
        return login(userName, password, tenant);
    }

    protected void checkUsernameAndPassword(final String userName, final String password) throws LoginException {
        if (userName == null || userName.isEmpty()) {
            throw new LoginException("User name is null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new LoginException("Password is null or empty");
        }
    }

    protected APISession login(final String userName, final String password, final STenant tenant) throws LoginException {
        SessionAccessor sessionAccessor = null;
        try {
            final long tenantId = tenant.getId();
            final TenantServiceAccessor serviceAccessor = getTenantServiceAccessor(tenantId);
            final LoginService loginService = serviceAccessor.getLoginService();
            final Login login = new Login(loginService, tenant.getId(), userName, password);
            final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
            transactionExecutor.execute(login);
            final SSession session = login.getResult();
            if (!session.isTechnicalUser()) {
                sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
                sessionAccessor.setSessionInfo(session.getId(), tenantId);
                final IdentityService identityService = serviceAccessor.getIdentityService();
                final GetSUser getSUser = new GetSUser(identityService, userName);
                transactionExecutor.execute(getSUser);
                final SUser sUser = getSUser.getResult();
                final IdentityModelBuilder identityModelBuilder = serviceAccessor.getIdentityModelBuilder();
                final UserUpdateBuilder userUpdateBuilder = identityModelBuilder.getUserUpdateBuilder();
                final long lastConnection = System.currentTimeMillis();
                final EntityUpdateDescriptor updateDescriptor = userUpdateBuilder.updateLastConnection(lastConnection).done();
                final UpdateUser updateUser = new UpdateUser(identityService, sUser.getId(), updateDescriptor, null, null, null);
                transactionExecutor.execute(updateUser);
            }
            return ModelConvertor.toAPISession(session, tenant.getName());
        } catch (final Exception sbe) {
            throw new LoginException(sbe);
        } finally {
            if (sessionAccessor != null) {
                sessionAccessor.deleteSessionId();
            }
        }
    }

    private STenant getDefaultTenant() throws LoginException {
        try {
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformService platformService = platformServiceAccessor.getPlatformService();
            final TransactionExecutor transactionExecutor = platformServiceAccessor.getTransactionExecutor();
            final GetDefaultTenantInstance getTenant = new GetDefaultTenantInstance(platformService);
            transactionExecutor.execute(getTenant);
            final STenant sTenant = getTenant.getResult();
            if (!platformService.isTenantActivated(sTenant)) {
                throw new LoginException("Tenant " + sTenant.getName() + " is not activated");
            }
            return sTenant;
        } catch (final SBonitaException sbe) {
            throw new LoginException(sbe);
        } catch (final BonitaHomeNotSetException bhnse) {
            throw new LoginException(bhnse);
        } catch (final InstantiationException ie) {
            throw new LoginException(ie);
        } catch (final IllegalAccessException iae) {
            throw new LoginException(iae);
        } catch (final ClassNotFoundException cnfe) {
            throw new LoginException(cnfe);
        } catch (final IOException ioe) {
            throw new LoginException(ioe);
        } catch (final BonitaHomeConfigurationException bhce) {
            throw new LoginException(bhce);
        } catch (final RuntimeException re) { // FIXME
            throw new LoginException(re);
        }
    }

    protected TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }

    @Override
    public void logout(final APISession session) throws LogoutException, SessionNotFoundException {
        final TenantServiceAccessor serviceAccessor = getTenantServiceAccessor(session.getTenantId());
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final Logout logout = new Logout(serviceAccessor.getLoginService(), session.getId());
        try {
            transactionExecutor.execute(logout);
        } catch (final SSessionNotFoundException sbe) {
            throw new SessionNotFoundException(sbe);
        } catch (final SBonitaException sbe) {
            throw new LogoutException(sbe);
        }
    }

}
