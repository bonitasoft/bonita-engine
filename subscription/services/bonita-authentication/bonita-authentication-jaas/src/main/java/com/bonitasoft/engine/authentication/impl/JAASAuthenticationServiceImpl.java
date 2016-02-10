/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class JAASAuthenticationServiceImpl implements GenericAuthenticationService {

    protected JAASGenericAuthenticationServiceImpl jaasGenericAuthenticationServiceImpl;

    public JAASAuthenticationServiceImpl(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor) {
        this.jaasGenericAuthenticationServiceImpl = new JAASGenericAuthenticationServiceImpl(logger, sessionAccessor);
    }

    @Override
    public String checkUserCredentials(Map<String, Serializable> credentials) throws AuthenticationException {
        return this.jaasGenericAuthenticationServiceImpl.checkUserCredentials(credentials);
    }
}
