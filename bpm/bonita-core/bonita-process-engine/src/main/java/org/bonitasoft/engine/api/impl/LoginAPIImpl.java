/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.api.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.impl.transaction.GetDefaultTenantInstance;
import org.bonitasoft.engine.api.impl.transaction.GetSUser;
import org.bonitasoft.engine.api.impl.transaction.GetTenantInstance;
import org.bonitasoft.engine.api.impl.transaction.Login;
import org.bonitasoft.engine.api.impl.transaction.Logout;
import org.bonitasoft.engine.api.impl.transaction.UpdateUser;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.LoginException;
import org.bonitasoft.engine.exception.LogoutException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.UserUpdateBuilder;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.APISessionImpl;
import org.bonitasoft.engine.session.model.SSession;

/**
 * @author Matthieu Chaffotte
 */
public class LoginAPIImpl implements LoginAPI {

    @Override
    public APISession login(final String userName, final String password) throws LoginException {
        final STenant sTenant = getTenant(null);
        return login(sTenant, userName, password);
    }

    @Override
    public APISession login(final long tenantId, final String userName, final String password) throws LoginException {
        final STenant sTenant = getTenant(tenantId);
        return login(sTenant, userName, password);
    }

    private STenant getTenant(final Long tenantId) throws LoginException {
        try {
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformService platformService = platformServiceAccessor.getPlatformService();
            STenant sTenant;
            if (tenantId == null) {
                final GetDefaultTenantInstance getDefaultTenant = new GetDefaultTenantInstance(platformService);
                final TransactionExecutor transactionExecutor = platformServiceAccessor.getTransactionExecutor();
                transactionExecutor.execute(getDefaultTenant);
                sTenant = getDefaultTenant.getResult();
            } else {
                final GetTenantInstance getTenantInstance = new GetTenantInstance(tenantId, platformService);
                final TransactionExecutor platformTransactionExecutor = platformServiceAccessor.getTransactionExecutor();
                platformTransactionExecutor.execute(getTenantInstance);
                sTenant = getTenantInstance.getResult();
            }
            if (!platformService.isTenantActivated(sTenant)) {
                throw new LoginException("Tenant " + sTenant.getName() + "is not activated");
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
    public void logout(final APISession session) throws LogoutException {
        final TenantServiceAccessor serviceAccessor = TenantServiceSingleton.getInstance(session.getTenantId());
        final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
        final Logout logout = new Logout(serviceAccessor.getLoginService(), session.getId());
        try {
            transactionExecutor.execute(logout);
        } catch (final SBonitaException sbe) {
            throw new LogoutException(sbe);
        }
    }

    private boolean checkTechnicalUserCredentials(final long tenantId, final String userName, final String password) throws BonitaHomeNotSetException,
            LoginException {
        final String technicalUserPropertiesPath = BonitaHomeServer.getInstance().getTenantConfFolder(tenantId) + File.separator + "bonita-server.xml";
        try {
            final Properties properties = PropertiesManager.getPropertiesFromXmlFile(new File(technicalUserPropertiesPath));
            final String techinicalUser = (String) properties.get("userName");
            final String techinicalPassword = (String) properties.get("userPassword");
            return userName.equals(techinicalUser) && password.equals(techinicalPassword);
        } catch (final IOException e) {
            throw new LoginException(e);
        }
    }

}
