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
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

/**
 * @author Elias Ricken de Medeiros
 * @author Julien Reboul
 */
public class JAASGenericAuthenticationServiceImpl implements GenericAuthenticationService {

    private static final String LOGIN_CONTEXT_PREFIX = "BonitaAuthentication";

    public static final String CALLER_PRINCIPAL = "CallerPrincipal";

    private final TechnicalLoggerService logger;

    private final ReadSessionAccessor sessionAccessor;

    private AuthenticatorDelegate authenticatorDelegate;

    public JAASGenericAuthenticationServiceImpl(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor) {
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
    }

    @Override
    public String checkUserCredentials(final Map<String, Serializable> credentials) throws AuthenticationException {
        Map<String, Serializable> jaasCredentials = tryToAuthenticate(credentials);
        LoginContext loginContext = createContext(new AuthenticationCallbackHandler(jaasCredentials));
        login(loginContext);
        return extractUserFromSubjet(loginContext);
    }

    /**
     * @param credentials
     */
    protected Map<String, Serializable> tryToAuthenticate(final Map<String, Serializable> credentials) {
        if (authenticatorDelegate != null) {
            return authenticatorDelegate.authenticate(credentials);
        }
        return credentials;
    }

    /**
     * attempts a login on the given {@link LoginContext}. see JAAS documentation for more information
     * 
     * @param loginContext
     *            the {@link LoginContext} to use to login
     * @throws AuthenticationException
     *             if the authentication fails
     */
    protected void login(final LoginContext loginContext) throws AuthenticationException {
        try {
            loginContext.login();
        } catch (final LoginException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "", e);
            }
            throw new AuthenticationException(e);
        }
    }

    /**
     * creates {@link LoginContext} to use to authenticate
     * 
     * @param authenticationCallbackHandler
     *            the callback handler to use when managing callback in underlying {@link LoginModule}
     * @return the created {@link LoginContext}
     * @throws AuthenticationException
     *             if a problem occurs during context creation
     */
    protected LoginContext createContext(final AuthenticationCallbackHandler authenticationCallbackHandler) throws AuthenticationException {
        try {
            return new LoginContext(getLoginContext(), authenticationCallbackHandler);
        } catch (final Exception e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(JAASGenericAuthenticationServiceImpl.class, TechnicalLogSeverity.WARNING, "error on context creation :" + e);
            }
            throw new AuthenticationException(e);
        }
    }

    /**
     * it seems that there is a gap in the JAAS protocol to retrieve the user associated with the succeeded authentication.
     * Implementations seem to use the Principal called "CallerPrincipal" that should be a group of principal where user should be present
     * (yes, it is a lot of should...) <br>
     * <br>
     * It is at least what CAS is giving us. It is then acceptable.
     * 
     * @param loginContext
     *            the login context to extract user from
     * @return the user if it had succeed, null otherwise
     */
    protected String extractUserFromSubjet(final LoginContext loginContext) {
        try {
            Subject subject = loginContext.getSubject();
            Set<Principal> principals = subject.getPrincipals();
            for (Principal principal : principals) {
                if (StringUtils.equals(principal.getName(), CALLER_PRINCIPAL) && isGroupPrincipal(principal)) {
                    Group group = (Group) principal;
                    @SuppressWarnings("unchecked")
                    Enumeration<Principal> enumeration = (Enumeration<Principal>) group.members();
                    if (enumeration.hasMoreElements()) {
                        return enumeration.nextElement().getName();
                    }
                }
            }
        } catch (Exception e) {
            // since it is a supposition that the principal holds the user name,
            // error should happen if something goes wrong and we don't want to manage it yet
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING, "impossible to retrieve username from credentials", e);
            }
        }
        return null;
    }

    /**
     * @param principal
     * @return
     */
    protected boolean isGroupPrincipal(final Principal principal) {
        return Group.class.isAssignableFrom(principal.getClass());
    }

    private String getLoginContext() throws STenantIdNotSetException {
        return LOGIN_CONTEXT_PREFIX + "-" + sessionAccessor.getTenantId();
    }

    /**
     * @return the authenticatorDelegate
     */
    public AuthenticatorDelegate getAuthenticatorDelegate() {
        return authenticatorDelegate;
    }

    /**
     * @param authenticatorDelegate
     *            the authenticatorDelegate to set
     */
    public void setAuthenticatorDelegate(AuthenticatorDelegate authenticatorDelegate) {
        this.authenticatorDelegate = authenticatorDelegate;
    }
}
