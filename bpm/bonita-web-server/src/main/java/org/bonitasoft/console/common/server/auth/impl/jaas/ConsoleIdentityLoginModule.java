/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.auth.impl.jaas;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Destroyable;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.bonitasoft.console.common.server.login.credentials.LoginDatastore;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;

/**
 * This {@link LoginModule} is used to verify a user identity against Bonita authentication service.
 *
 * @author Qixiang Zhang
 */
public class ConsoleIdentityLoginModule implements LoginModule {

    /**
     * User name prompt
     */
    private static final String NAME_PROMPT = "Name: ";

    /**
     * User password prompt
     */
    private static final String PASSWORD_PROMPT = "Password: ";

    /**
     * javax security auth login password
     */
    protected static final String JAVAX_SECURITY_AUTH_LOGIN_PASSWORD = "javax.security.auth.login.password";

    /**
     * javax security auth login name
     */
    protected static final String JAVAX_SECURITY_AUTH_LOGIN_NAME = "javax.security.auth.login.name";

    /**
     * Property key for the debug flag. Defined to be "debug".
     * Property Value. If set, should be either "true" or "false". Default is
     * "false".
     */
    public static final String DEBUG_OPTION_NAME = "debug";

    /**
     * The subject to be authenticated
     */
    private Subject subject = null;

    /**
     * A CallbackHandler for communicating with the end user (prompting
     * for usernames and passwords, for example)
     */
    private CallbackHandler callbackHandler = null;

    /**
     * State shared with other configured LoginModules.
     */
    private Map<String, Object> sharedState;

    /**
     * Debug flag
     */
    private boolean debug = false;

    /**
     * Principal id
     */
    private String id;

    /**
     * Initialize this LoginModule. This method is called by the LoginContext
     * after this LoginModule has been instantiated. The purpose of this method is
     * to initialize this LoginModule with the relevant information. If this
     * LoginModule does not understand any of the data stored in sharedState or
     * options parameters, they can be ignored.
     *
     * @param subject
     *        the Subject to be authenticated.
     * @param callbackHandler
     *        a CallbackHandler for communicating with the end user (prompting
     *        for usernames and passwords, for example).
     * @param sharedState
     *        state shared with other configured LoginModules.
     * @param options
     *        options specified in the login Configuration for this particular
     *        LoginModule.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void initialize(final Subject subject, final CallbackHandler callbackHandler,
            final Map<String, ?> sharedState, final Map<String, ?> options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = (Map<String, Object>) sharedState;
        final String debugFlag = (String) options.get(DEBUG_OPTION_NAME);
        if (debugFlag != null) {
            this.debug = Boolean.valueOf(debugFlag);
        }
    }

    /**
     * Method to authenticate a Subject (phase 1). The implementation of this
     * method authenticates a Subject. For example, it may prompt for Subject
     * information such as a username and password and then attempt to verify the
     * password. This method saves the result of the authentication attempt as
     * private state within the LoginModule.
     *
     * @return true if the authentication succeeded, or false if this LoginModule
     *         should be ignored.
     * @throws LoginException
     *         if the authentication fails
     */
    @Override
    public boolean login() throws LoginException {
        if (this.debug) {
            System.err.println("[" + ConsoleIdentityLoginModule.class.getName() + "] login() - preparing - step 1");
        }
        try {

            final Map<String, Object> loggingsArgs = getSharedState();

            final Map<String, Callback> callbacks = getPromptCallbacks(loggingsArgs);

            if (!callbacks.isEmpty()) {
                if (this.debug) {
                    System.err.println(
                            "[" + ConsoleIdentityLoginModule.class.getName() + "] login() - callback - step 2");
                }
                this.callbackHandler.handle(callbacks.values().toArray(new Callback[0]));
                adjustLoggingsArgs(callbacks, loggingsArgs);
            }

            if (isDebug()) {
                System.err.println(
                        "[" + ConsoleIdentityLoginModule.class.getName() + "] login() - authenticating - step 3");
            }
            final APISession aAPISession = (loggingsArgs.containsKey(JAVAX_SECURITY_AUTH_LOGIN_NAME))
                    ? doLogin(loggingsArgs)
                    : null;

            if (isDebug()) {
                System.err.println(
                        "[" + ConsoleIdentityLoginModule.class.getName() + "] login() - storing data - step 4");
            }

            if (aAPISession != null) {
                this.id = (String) getSharedState().get(JAVAX_SECURITY_AUTH_LOGIN_NAME);
            }

            if (isDebug()) {
                System.err.println("[" + ConsoleIdentityLoginModule.class.getName() + "] login() - returning - step 5");
            }
            if (this.id == null) {
                throw new FailedLoginException("id is null");
            }
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            final LoginException le = new LoginException();
            le.initCause(e);
            throw le;
        }
    }

    /**
     * Log user in
     *
     * @param loggingsArgs
     * @return
     * @throws BonitaException
     */
    protected APISession doLogin(final Map<String, Object> loggingsArgs) throws BonitaException {
        final LoginDatastore loginDatastore = new LoginDatastore();
        return loginDatastore.login(String.valueOf(loggingsArgs.get(JAVAX_SECURITY_AUTH_LOGIN_NAME)),
                String.valueOf(loggingsArgs.get(JAVAX_SECURITY_AUTH_LOGIN_PASSWORD)));
    }

    /**
     * Get callback to prompt authentication to user
     *
     * @param loggingsArgs
     * @return
     */
    protected Map<String, Callback> getPromptCallbacks(final Map<String, Object> loggingsArgs) {
        final Map<String, Callback> callbacks = new HashMap<>();

        // login
        if (!loggingsArgs.containsKey(JAVAX_SECURITY_AUTH_LOGIN_NAME)) {
            callbacks.put(NAME_PROMPT, new NameCallback(NAME_PROMPT));
        }

        // password
        if (!loggingsArgs.containsKey(JAVAX_SECURITY_AUTH_LOGIN_PASSWORD)) {
            callbacks.put(PASSWORD_PROMPT, new PasswordCallback(PASSWORD_PROMPT, false));
        }

        return callbacks;
    }

    /**
     * Adjust loggings arguments depending on callbacks results
     *
     * @param callbacks
     * @param loggingsArgs
     */
    protected void adjustLoggingsArgs(final Map<String, Callback> callbacks, final Map<String, Object> loggingsArgs) {
        if (!loggingsArgs.containsKey(JAVAX_SECURITY_AUTH_LOGIN_NAME)) {
            // update name
            if (callbacks.get(NAME_PROMPT) instanceof NameCallback) {
                loggingsArgs.put(JAVAX_SECURITY_AUTH_LOGIN_NAME, ((NameCallback) callbacks.get(NAME_PROMPT)).getName());
            }
        }

        if (!loggingsArgs.containsKey(JAVAX_SECURITY_AUTH_LOGIN_PASSWORD)) {
            // update password
            if (callbacks.get(PASSWORD_PROMPT) instanceof PasswordCallback) {
                PasswordCallback pwdCallback = ((PasswordCallback) callbacks.get(PASSWORD_PROMPT));
                loggingsArgs.put(JAVAX_SECURITY_AUTH_LOGIN_PASSWORD, String.valueOf(pwdCallback.getPassword()));
                pwdCallback.clearPassword();
            }
        }
    }

    /**
     * Method to commit the authentication process (phase 2). This method is
     * called if the LoginContext's overall authentication succeeded (the relevant
     * REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules succeeded). If
     * this LoginModule's own authentication attempt succeeded (checked by
     * retrieving the private state saved by the login method), then this method
     * associates relevant Principals and Credentials with the Subject located in
     * the LoginModule. If this LoginModule's own authentication attempted failed,
     * then this method removes/destroys any state that was originally saved.
     *
     * @return true if this method succeeded, or false if this LoginModule should
     *         be ignored.
     * @throws LoginException
     *         if the commit fails
     */
    @Override
    public boolean commit() throws LoginException {
        if (this.id == null) {
            throw new FailedLoginException("id is null");
        }
        final Set<Principal> principals = this.subject.getPrincipals();
        principals.add(new ConsolePrincipal(this.id));
        return true;
    }

    /**
     * Method to abort the authentication process (phase 2). This method is called
     * if the LoginContext's overall authentication failed. (the relevant
     * REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules did not succeed).
     * If this LoginModule's own authentication attempt succeeded (checked by
     * retrieving the private state saved by the login method), then this method
     * cleans up any state that was originally saved.
     *
     * @return true if this method succeeded, or false if this LoginModule should
     *         be ignored.
     * @throws LoginException
     *         if the abort fails
     */
    @Override
    public boolean abort() throws LoginException {
        if (this.debug) {
            System.err.println("[" + ConsoleIdentityLoginModule.class.getName() + "] abort()");
        }
        if (this.id == null) {
            return false;
        }
        this.subject = null;
        this.id = null;
        return true;
    }

    /**
     * Method which logs out a Subject. An implementation of this method might
     * remove/destroy a Subject's Principals and Credentials.
     *
     * @return true if this method succeeded, or false if this LoginModule should
     *         be ignored.
     * @throws LoginException
     *         if the logout fails
     */
    @Override
    public boolean logout() throws LoginException {
        if (this.id != null) {
            if (this.debug) {
                System.err
                        .println("[" + ConsoleIdentityLoginModule.class.getName() + "] logout() - removing principals");
            }
            // Remove only principals added by our commit method
            final Set<Principal> principals = new HashSet<>(this.subject.getPrincipals());
            for (final Principal p : principals) {
                if (p instanceof ConsolePrincipal) {
                    if (this.debug) {
                        System.err.println("[" + ConsoleIdentityLoginModule.class.getName()
                                + "] logout() - removing principal: " + p);
                    }
                    this.subject.getPrincipals().remove(p);
                }
            }
            if (this.debug) {
                System.err.println("[" + ConsoleIdentityLoginModule.class.getName()
                        + "] logout() - destroying/removing credentials");
            }
            // Remove/destroy only credentials added by our commit method
            final Set<Object> credentials = new HashSet<>(this.subject.getPublicCredentials());
            for (final Object o : credentials) {
                if (o instanceof Destroyable) {
                    if (this.debug) {
                        System.err.println("[" + ConsoleIdentityLoginModule.class.getName()
                                + "] logout() - destroying credential: " + o);
                    }
                    // Bug: only from this module !!
                    // ((Destroyable) o).destroy();
                }
                if (!this.subject.isReadOnly()) {
                    if (this.debug) {
                        System.err.println("[" + ConsoleIdentityLoginModule.class.getName()
                                + "] logout() - removing credential: " + o);
                    }
                    this.subject.getPublicCredentials().remove(o);
                }
            }
        }
        return true;
    }

    protected Map<String, Object> getSharedState() {
        return sharedState;
    }

    protected boolean isDebug() {
        return debug;
    }
}
