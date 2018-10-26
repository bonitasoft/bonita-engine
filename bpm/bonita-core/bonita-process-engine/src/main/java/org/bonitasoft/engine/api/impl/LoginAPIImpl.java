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
package org.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.impl.transaction.platform.GetDefaultTenantInstance;
import org.bonitasoft.engine.api.impl.transaction.platform.GetTenantInstance;
import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.login.SLoginException;
import org.bonitasoft.engine.core.login.TechnicalUser;
import org.bonitasoft.engine.exception.TenantStatusException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.UnknownUserException;
import org.bonitasoft.engine.platform.model.STenant;
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
import org.bonitasoft.engine.transaction.TransactionService;
import org.springframework.util.CollectionUtils;

/**
 * @author Matthieu Chaffotte
 * @author Zhang Bole
 */
public class LoginAPIImpl extends AbstractLoginApiImpl implements LoginAPI {

    @Override
    @CustomTransactions
    @AvailableWhenTenantIsPaused
    public APISession login(final String userName, final String password) throws LoginException {
        try {
            return loginInternal(userName, password, null);
        } catch (final LoginException e) {
            throw e;
        } catch (final Exception e) {
            throw new LoginException(e);
        }
    }

    @CustomTransactions
    @AvailableWhenTenantIsPaused
    protected APISession login(final String userName, final String password, final Long tenantId) throws LoginException {
        try {
            return loginInternal(userName, password, tenantId);
        } catch (final LoginException e) {
            throw e;
        } catch (final Exception e) {
            throw new LoginException(e);
        }
    }

    @Override
    @CustomTransactions
    @AvailableWhenTenantIsPaused
    public APISession login(final Map<String, Serializable> credentials) throws LoginException, UnknownUserException {
        checkCredentialsAreNotNullOrEmpty(credentials);
        try {
            final Long tenantId = NumberUtils.isNumber(String.valueOf(credentials.get(AuthenticationConstants.BASIC_TENANT_ID))) ? NumberUtils.toLong(String
                    .valueOf(credentials.get(AuthenticationConstants.BASIC_TENANT_ID))) : null;
            return loginInternal(tenantId, credentials);
        } catch (final LoginException e) {
            throw e;
        } catch (final Exception e) {
            throw new LoginException(e);
        }
    }

    protected APISession loginInternal(final String userName, final String password, final Long tenantId) throws Exception {
        checkUsernameAndPassword(userName, password);
        final Map<String, Serializable> credentials = new HashMap<>();
        credentials.put(AuthenticationConstants.BASIC_USERNAME, userName);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        return loginInternal(tenantId, credentials);
    }

    protected APISession loginInternal(final Long tenantId, final Map<String, Serializable> credentials) throws Exception {
        final String userName = credentials.get(AuthenticationConstants.BASIC_USERNAME) != null ? String.valueOf(credentials
                .get(AuthenticationConstants.BASIC_USERNAME)) : null;
        final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        final STenant sTenant = getTenant(tenantId, platformServiceAccessor);

        final TenantServiceAccessor serviceAccessor = getTenantServiceAccessor(sTenant.getId());
        checkThatWeCanLogin(userName, sTenant, serviceAccessor.getTechnicalUser());
        final LoginService loginService = serviceAccessor.getLoginService();
        final TransactionService transactionService = platformServiceAccessor.getTransactionService();
        SessionAccessor sessionAccessor = serviceAccessor.getSessionAccessor();

        final Map<String, Serializable> credentialsWithResolvedTenantId = new HashMap<>(credentials);
        credentialsWithResolvedTenantId.put(AuthenticationConstants.BASIC_TENANT_ID, sTenant.getId());
        sessionAccessor.setTenantId(sTenant.getId());
        try {
            final SSession sSession = transactionService.executeInTransaction(() -> loginService.login(credentialsWithResolvedTenantId));
            return ModelConvertor.toAPISession(sSession, sTenant.getName());
        } catch (Exception e) {
            //avoid brut force... (should be done differently, but it is the behavior since 6.0.0)
            Thread.sleep(3000);
            throw e;
        }
    }

    protected STenant getTenant(final Long tenantId, final PlatformServiceAccessor platformServiceAccessor) throws SBonitaException {
        final PlatformService platformService = platformServiceAccessor.getPlatformService();
        final TransactionExecutor platformTransactionExecutor = platformServiceAccessor.getTransactionExecutor();
        // first call before create session: put the platform in cache if necessary
        final TransactionContentWithResult<STenant> getTenant;
        if (tenantId == null) {
            getTenant = new GetDefaultTenantInstance(platformService);
        } else {
            getTenant = new GetTenantInstance(tenantId, platformService);
        }
        platformTransactionExecutor.execute(getTenant);
        return getTenant.getResult();
    }

    protected void checkUsernameAndPassword(final String userName, final String password) throws LoginException {
        if (userName == null || userName.isEmpty()) {
            throw new LoginException("User name is null or empty !! ");
        }
        if (password == null || password.isEmpty()) {
            throw new LoginException("Password is null or empty !!");
        }
    }

    protected void checkCredentialsAreNotNullOrEmpty(final Map<String, Serializable> credentials) throws LoginException {
        if (CollectionUtils.isEmpty(credentials)) {
            throw new LoginException("Credentials are null or empty !!");
        }
    }

    protected void checkThatWeCanLogin(final String userName, final STenant sTenant, TechnicalUser technicalUser) throws LoginException {
        if (!sTenant.isActivated()) {
            throw new LoginException("Tenant " + sTenant.getName() + " is not activated !!");
        }
        if (sTenant.isPaused()) {
            final String technicalUserName = technicalUser.getUserName();

            if (!technicalUserName.equals(userName)) {
                throw new TenantStatusException("Tenant with ID " + sTenant.getId()
                        + " is in pause, unable to login with other user than the technical user.");
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
        try {
            serviceAccessor.getLoginService().logout(session.getId());
        } catch (final SSessionNotFoundException sbe) {
            throw new SessionNotFoundException(sbe);
        } catch (final SLoginException sbe) {
            throw new LogoutException(sbe);
        }
    }

}
