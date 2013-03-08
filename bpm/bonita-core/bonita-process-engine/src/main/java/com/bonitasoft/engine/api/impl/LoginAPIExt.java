/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.Date;

import org.bonitasoft.engine.api.impl.LoginAPIImpl;
import org.bonitasoft.engine.api.impl.transaction.GetSUser;
import org.bonitasoft.engine.api.impl.transaction.Login;
import org.bonitasoft.engine.api.impl.transaction.UpdateUser;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.LoginException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.UserUpdateBuilder;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.APISessionImpl;
import org.bonitasoft.engine.session.model.SSession;

import com.bonitasoft.engine.api.LoginAPI;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Matthieu Chaffotte
 */
public class LoginAPIExt extends LoginAPIImpl implements LoginAPI {

    @Override
    public APISession login(final long tenantId, final String userName, final String password) throws LoginException {
        final STenant sTenant = getTenant(tenantId);
        return login(sTenant, userName, password);
    }

    private APISession login(final STenant tenant, final String userName, final String password) throws LoginException {
        if (userName == null || userName.isEmpty()) {
            throw new LoginException("User name is null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new LoginException("Password is null or empty");
        }
        try {
            final TenantServiceAccessor serviceAccessor = TenantServiceSingleton.getInstance(tenant.getId());
            final LoginService loginService = serviceAccessor.getLoginService();
            final Login login = new Login(loginService, tenant.getId(), userName, password);
            final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
            transactionExecutor.execute(login);
            final IdentityService identityService = serviceAccessor.getIdentityService();
            final GetSUser getSUser = new GetSUser(identityService, userName);
            SUser sUser;
            try {
                transactionExecutor.execute(getSUser);
                sUser = getSUser.getResult();
                final IdentityModelBuilder identityModelBuilder = serviceAccessor.getIdentityModelBuilder();
                final UserUpdateBuilder userUpdateBuilder = identityModelBuilder.getUserUpdateBuilder();
                final long lastConnection = System.currentTimeMillis();
                final EntityUpdateDescriptor updateDescriptor = userUpdateBuilder.updateLastConnection(lastConnection).done();
                final UpdateUser updateUser = new UpdateUser(identityService, sUser.getId(), updateDescriptor, null, null, null);
                transactionExecutor.execute(updateUser);
            } catch (final SBonitaException sbe) {
                // XXX check if we have currently the technical user, else throw exception
            }
            final SSession sSession = login.getResult();
            final long id = sSession.getId();
            final Date creationDate = sSession.getCreationDate();
            final long duration = sSession.getDuration();
            final APISessionImpl apiSessionImpl = new APISessionImpl(id, creationDate, duration, userName, sSession.getUserId(), tenant.getName(),
                    tenant.getId());
            apiSessionImpl.setTechnicalUser(checkTechnicalUserCredentials(tenant.getId(), userName, password));
            return apiSessionImpl;
        } catch (final SBonitaException sbe) {
            throw new LoginException(sbe);
        } catch (final BonitaHomeNotSetException bhnse) {
            throw new LoginException(bhnse);
        }
    }

    @Override
    protected TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }
}
