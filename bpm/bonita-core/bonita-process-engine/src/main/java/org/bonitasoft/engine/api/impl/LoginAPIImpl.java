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

import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.impl.transaction.identity.UpdateUser;
import org.bonitasoft.engine.api.impl.transaction.platform.GetDefaultTenantInstance;
import org.bonitasoft.engine.api.impl.transaction.platform.GetTenantInstance;
import org.bonitasoft.engine.api.impl.transaction.platform.Logout;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
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
public class LoginAPIImpl extends
        AbstractLoginApiImpl implements LoginAPI {

    @Override
    @CustomTransactions
    public APISession login(final String userName, final String password) throws LoginException {
        checkUsernameAndPassword(userName, password);

        try {
            return login(userName, password, null);
        } catch (Throwable e) {
            throw new LoginException(e);
        }
    }

    protected void checkUsernameAndPassword(final String userName, final String password) throws LoginException {
        if (userName == null || userName.isEmpty()) {
            throw new LoginException("User name is null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new LoginException("Password is null or empty");
        }
    }

    protected APISession login(final String userName, final String password, final Long tenantId) throws Throwable {
        final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        final PlatformService platformService = platformServiceAccessor.getPlatformService();
        final TransactionExecutor platformTransactionExecutor = platformServiceAccessor.getTransactionExecutor();
        // first call before create session: put the platform in cache if necessary
        putPlatformInCacheIfNecessary(platformServiceAccessor, platformService);
        TransactionContentWithResult<STenant> getTenant;
        if (tenantId == null) {
            getTenant = new GetDefaultTenantInstance(platformService);
        } else {
            getTenant = new GetTenantInstance(tenantId, platformService);
        }
        platformTransactionExecutor.execute(getTenant);
        final STenant sTenant = getTenant.getResult();
        if (!platformService.isTenantActivated(sTenant)) {
            throw new LoginException("Tenant " + sTenant.getName() + " is not activated");
        }

        final long localTenantId = sTenant.getId();
        final TenantServiceAccessor serviceAccessor = getTenantServiceAccessor(localTenantId);
        final LoginService loginService = serviceAccessor.getLoginService();
        final IdentityService identityService = serviceAccessor.getIdentityService();
        final IdentityModelBuilder identityModelBuilder = serviceAccessor.getIdentityModelBuilder();
        final TransactionExecutor tenantTransactionExecutor = serviceAccessor.getTransactionExecutor();

        final TransactionContentWithResult<SSession> txContent = new LoginAndRetrieveUser(loginService, identityService, identityModelBuilder, localTenantId,
                userName, password);
        try {
            tenantTransactionExecutor.execute(txContent);
        } catch (BonitaRuntimeException e) {
            throw e.getCause();
        }

        return ModelConvertor.toAPISession(txContent.getResult(), sTenant.getName());
    }

    private class LoginAndRetrieveUser implements TransactionContentWithResult<SSession> {

        private final LoginService loginService;

        private final IdentityService identityService;

        private final long tenantId;

        private final String userName;

        private final String password;

        private final IdentityModelBuilder identityModelBuilder;

        private SSession session;

        public LoginAndRetrieveUser(final LoginService loginService, final IdentityService identityService, final IdentityModelBuilder identityModelBuilder,
                final long tenantId, final String userName, final String password) {
            this.loginService = loginService;
            this.identityService = identityService;
            this.identityModelBuilder = identityModelBuilder;
            this.tenantId = tenantId;
            this.userName = userName;
            this.password = password;

        }

        @Override
        public SSession getResult() {
            return session;
        }

        @Override
        public void execute() throws SBonitaException {
            SessionAccessor sessionAccessor = null;
            try {
                session = loginService.login(tenantId, userName, password);
                if (!session.isTechnicalUser()) {
                    sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
                    sessionAccessor.setSessionInfo(session.getId(), tenantId);
                    SUser sUser = identityService.getUserByUserName(userName);
                    final UserUpdateBuilder userUpdateBuilder = identityModelBuilder.getUserUpdateBuilder();
                    final long lastConnection = System.currentTimeMillis();
                    final EntityUpdateDescriptor updateDescriptor = userUpdateBuilder.updateLastConnection(lastConnection).done();
                    final UpdateUser updateUser = new UpdateUser(identityService, sUser.getId(), updateDescriptor, null, null, null);
                    updateUser.execute();
                }
            } catch (Exception e) {
                throw new BonitaRuntimeException(e);
            } finally {
                if (sessionAccessor != null) {
                    sessionAccessor.deleteSessionId();
                }
            }
        }

    }

    protected TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }

    @Override
    @CustomTransactions
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
