/*******************************************************************************
 * Copyright (C) 2013 - 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class JAASAuthenticationServiceImpl implements AuthenticationService {

    protected JAASGenericAuthenticationServiceImpl jaasGenericAuthenticationServiceImpl;

    public JAASAuthenticationServiceImpl(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor) {
        this.jaasGenericAuthenticationServiceImpl = new JAASGenericAuthenticationServiceImpl(logger, sessionAccessor);
    }

    @Override
    public boolean checkUserCredentials(final String username, final String password) throws AuthenticationException {
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, username);
        // TODO in case we are in a CAS environment, maybe we can use the login from the CAS server directly because wa have the
        // login/mdp to provide... but that will not be a JAAS Standard...
        this.jaasGenericAuthenticationServiceImpl.checkUserCredentials(credentials);
        // if the authentication fails, an exception is thrown, so we assume that passing this point is a login success
        return true;
    }
}
