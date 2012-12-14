/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.LoginException;
import org.bonitasoft.engine.session.APISession;

/**
 * @author Matthieu Chaffotte
 */
public interface LoginAPI extends org.bonitasoft.engine.api.LoginAPI {

    /**
     * Connects the user in order to use API methods of a tenant.
     * 
     * @param tenantId
     *            the tenant identifier
     * @param userName
     *            the user name
     * @param password
     *            the password
     * @return the session to use with other tenant API methods
     * @throws LoginException
     *             occurs when an exception is thrown during the login
     */
    APISession login(long tenantId, String userName, String password) throws LoginException;

}
