/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.io.IOException;

import org.bonitasoft.engine.api.impl.LoginAPIImpl;
import org.bonitasoft.engine.api.impl.transaction.platform.GetDefaultTenantInstance;
import org.bonitasoft.engine.api.impl.transaction.platform.GetTenantInstance;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.engine.api.LoginAPI;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class LoginAPIExt extends LoginAPIImpl implements LoginAPI {

    @Override
    public APISession login(final String userName, final String password) throws LoginException {
        if (!LicenseChecker.getInstance().checkLicence()) {
            throw new LoginException("The node is not started: " + LicenseChecker.getInstance().getErrorMessage());
        }
        return super.login(userName, password);
    }

    @Override
    public APISession login(final long tenantId, final String userName, final String password) throws LoginException {
        if (!LicenseChecker.getInstance().checkLicence()) {
            throw new LoginException("The node is not started");
        }
        checkUsernameAndPassword(userName, password);
        final STenant sTenant = getTenant(tenantId);
        return login(userName, password, sTenant);
    }

    @Override
    protected TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }

    private STenant getTenant(final Long tenantId) throws LoginException {
        try {
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformService platformService = platformServiceAccessor.getPlatformService();
            final TransactionExecutor transactionExecutor = platformServiceAccessor.getTransactionExecutor();
            TransactionContentWithResult<STenant> getTenant;
            if (tenantId == null) {
                getTenant = new GetDefaultTenantInstance(platformService);
            } else {
                getTenant = new GetTenantInstance(tenantId, platformService);
            }
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

}
