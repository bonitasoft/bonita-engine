/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.api.NoSessionRequired;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.session.APISession;

/**
 * @author Matthieu Chaffotte
 */
public interface LoginAPI extends org.bonitasoft.engine.api.LoginAPI {

    static final String CLIENT_PROGRAM_NAME = "program_name";

    static final String CLIENT_PROGRAM_TOKEN = "program_token";

    /**
     * Connects the user in order to use API methods of a tenant.
     * 
     * @param tenantId
     *        the tenant identifier
     * @param userName
     *        the user name
     * @param password
     *        the password
     * @return the session to use with other tenant API methods
     * @throws LoginException
     *         occurs when an exception is thrown during the login (userName does not exist, or couple (userName, password) is incorrect)
     * @throws TenantIsPausedException
     *         if the tenant is paused. No login is allowed for users other than the technical user.
     * @since 6.0
     */
    @NoSessionRequired
    APISession login(long tenantId, String userName, String password) throws LoginException, TenantIsPausedException;

    /**
     * Connects the user in order to use API methods of a tenant.
     * 
     * @param tenantId
     *        the tenant identifier
     * @param credentials
     *        the credentials to login with. Can be username / password, SSO ticket, ... depending on the implementation.
     * @return the session to use with other tenant API methods
     * @throws LoginException
     *         occurs when an exception is thrown during the login (userName does not exist, or couple (userName, password) is incorrect)
     * @since 6.0
     * @throws TenantIsPausedException
     *         if the tenant is paused. No login is allowed for users other than the technical user.
     */
    @NoSessionRequired
    APISession login(long tenantId, Map<String, Serializable> credentials) throws LoginException, TenantIsPausedException;

}
