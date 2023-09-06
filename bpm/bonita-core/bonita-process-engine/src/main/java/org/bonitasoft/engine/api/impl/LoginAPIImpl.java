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
package org.bonitasoft.engine.api.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.login.SLoginException;
import org.bonitasoft.engine.core.login.TechnicalUser;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.ServiceAccessor;
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
public class LoginAPIImpl implements LoginAPI {

    @Override
    @CustomTransactions
    @AvailableWhenTenantIsPaused
    public APISession login(final String userName, final String password) throws LoginException {
        try {
            return loginInternal(userName, password);
        } catch (final LoginException e) {
            throw e;
        } catch (final Exception e) {
            throw new LoginException(e);
        }
    }

    @CustomTransactions
    @AvailableWhenTenantIsPaused
    protected APISession login(final String userName, final String password, final Long tenantId)
            throws LoginException {
        try {
            return loginInternal(userName, password);
        } catch (final LoginException e) {
            throw e;
        } catch (final Exception e) {
            throw new LoginException(e);
        }
    }

    @Override
    @CustomTransactions
    @AvailableWhenTenantIsPaused
    public APISession login(final Map<String, Serializable> credentials) throws LoginException {
        checkCredentialsAreNotNullOrEmpty(credentials);
        try {
            return loginInternal(credentials);
        } catch (final LoginException e) {
            throw e;
        } catch (final Exception e) {
            throw new LoginException(e);
        }
    }

    protected APISession loginInternal(final String userName, final String password)
            throws Exception {
        checkUsernameAndPassword(userName, password);
        final Map<String, Serializable> credentials = new HashMap<>();
        credentials.put(AuthenticationConstants.BASIC_USERNAME, userName);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        return loginInternal(credentials);
    }

    protected APISession loginInternal(final Map<String, Serializable> credentials)
            throws Exception {
        final String userName = credentials.get(AuthenticationConstants.BASIC_USERNAME) != null
                ? String.valueOf(credentials.get(AuthenticationConstants.BASIC_USERNAME)) : null;
        final ServiceAccessor serviceAccessor = ServiceAccessorFactory.getInstance().createServiceAccessor();
        final STenant sTenant = getTenant(serviceAccessor);

        checkThatWeCanLogin(userName, sTenant, serviceAccessor.getTechnicalUser());
        final LoginService loginService = serviceAccessor.getLoginService();
        final TransactionService transactionService = serviceAccessor.getTransactionService();
        SessionAccessor sessionAccessor = serviceAccessor.getSessionAccessor();

        final Map<String, Serializable> credentialsWithResolvedTenantId = new HashMap<>(credentials);
        credentialsWithResolvedTenantId.put(AuthenticationConstants.BASIC_TENANT_ID, sTenant.getId());
        sessionAccessor.setTenantId(sTenant.getId());
        try {
            final SSession sSession = transactionService
                    .executeInTransaction(() -> loginService.login(credentialsWithResolvedTenantId));
            return ModelConvertor.toAPISession(sSession, sTenant.getName());
        } catch (Exception e) {
            //avoid brut force... (should be done differently, but it is the behavior since 6.0.0)
            Thread.sleep(3000);
            throw e;
        }
    }

    private STenant getTenant(final ServiceAccessor serviceAccessor)
            throws SBonitaException {
        final PlatformService platformService = serviceAccessor.getPlatformService();
        try {
            return serviceAccessor.getTransactionService().executeInTransaction(platformService::getDefaultTenant);
        } catch (SBonitaException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SBonitaRuntimeException(e);
        }
    }

    protected void checkUsernameAndPassword(final String userName, final String password) throws LoginException {
        if (userName == null || userName.isEmpty()) {
            throw new LoginException("User name is null or empty !! ");
        }
        if (password == null || password.isEmpty()) {
            throw new LoginException("Password is null or empty !!");
        }
    }

    protected void checkCredentialsAreNotNullOrEmpty(final Map<String, Serializable> credentials)
            throws LoginException {
        if (CollectionUtils.isEmpty(credentials)) {
            throw new LoginException("Credentials are null or empty !!");
        }
    }

    protected void checkThatWeCanLogin(final String userName, final STenant sTenant, TechnicalUser technicalUser)
            throws LoginException {
        if (sTenant.isDeactivated()) {
            throw new LoginException("Tenant " + sTenant.getName() + " is not activated !!");
        }
    }

    protected ServiceAccessor getServiceAccessor() {
        try {
            return ServiceAccessorFactory.getInstance().createServiceAccessor();
        } catch (BonitaHomeConfigurationException | IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @CustomTransactions
    public void logout(final APISession session) throws LogoutException, SessionNotFoundException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        try {
            serviceAccessor.getLoginService().logout(session.getId());
        } catch (final SSessionNotFoundException sbe) {
            throw new SessionNotFoundException(sbe);
        } catch (final SLoginException sbe) {
            throw new LogoutException(sbe);
        }
    }

}
