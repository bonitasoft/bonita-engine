/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl;

import java.io.Serializable;
import java.util.Map;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

/**
 * @author Elias Ricken de Medeiros
 */
public class JAASAuthenticationServiceImpl implements AuthenticationService {

    private static final String LOGIN_CONTEXT_PREFIX = "BonitaAuthentication";

    private final TechnicalLoggerService logger;

    private final ReadSessionAccessor sessionAccessor;

    public JAASAuthenticationServiceImpl(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor) {
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
    }

    @Override
    public boolean checkUserCredentials(Map<String, Serializable> credentials) throws AuthenticationException {
        LoginContext loginContext = null;
        try {
            loginContext = new LoginContext(getLoginContext(), new AuthenticationCallbackHandler(credentials));
        } catch (final Exception e) {
            throw new AuthenticationException(e);
        }
        try {
            loginContext.login();
        } catch (final LoginException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "", e);
            }
            return false;
        }
        try {
            loginContext.logout();
        } catch (final LoginException e) {
            throw new AuthenticationException(e);
        }
        return true;
    }

    private String getLoginContext() throws TenantIdNotSetException {
        return LOGIN_CONTEXT_PREFIX + "-" + sessionAccessor.getTenantId();
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.engine.authentication.AuthenticationService#logout(java.util.Map)
     */
    @Override
    public void logout(Map<String, Serializable> credentials) throws AuthenticationException {
        LoginContext loginContext = null;
        try {
            loginContext = new LoginContext(getLoginContext(), new AuthenticationCallbackHandler(credentials));
        } catch (final Exception e) {
            throw new AuthenticationException(e);
        }
        try {
            loginContext.logout();
        } catch (final LoginException e) {
            throw new AuthenticationException(e);
        }
    }
}
