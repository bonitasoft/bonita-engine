/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * @author Elias Ricken de Medeiros
 */
public class MockLoginModule implements LoginModule {

    private static final String NAME_PROMPT = "Name: ";

    private static final String PASSWORD_PROMPT = "Password: ";

    private CallbackHandler callbackHandler = null;

    private String id;

    public MockLoginModule() {
    }

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        this.callbackHandler = callbackHandler;
        subject.getPrincipals().add(new Group() {

            Principal userPrincipal = new Principal() {

                @Override
                public String getName() {
                    return "admin";
                }
            };

            @Override
            public String getName() {
                return "CallerPrincipal";
            }

            @Override
            public boolean removeMember(final Principal user) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Enumeration<? extends Principal> members() {
                final Vector<Principal> principals = new Vector<Principal>();
                principals.add(userPrincipal);
                return principals.elements();
            }

            @Override
            public boolean isMember(final Principal member) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean addMember(final Principal user) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }

    @Override
    public boolean login() throws LoginException {
        try {
            String name = null;
            String password = null;
            final List<Callback> callbacks = new ArrayList<Callback>();
            final NameCallback nameCallback = new NameCallback(NAME_PROMPT);
            final PasswordCallback passwordCallback = new PasswordCallback(PASSWORD_PROMPT, false);
            callbacks.add(nameCallback);
            callbacks.add(passwordCallback);
            if (!callbacks.isEmpty()) {
                callbackHandler.handle(callbacks.toArray(new Callback[0]));
                name = nameCallback.getName();
                password = new String(passwordCallback.getPassword());
                passwordCallback.clearPassword();
            }
            if (name != null) {
                if ("admin".equals(name) && "bpm".equals(password)) {
                    id = name;
                }
            }
            if (id == null) {
                final String message = "Invalid username e/or password";
                throw new FailedLoginException(message);
            }
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            final LoginException le = new LoginException();
            le.initCause(e);
            throw le;
        }
    }

    @Override
    public boolean commit() {
        return true;
    }

    @Override
    public boolean abort() {
        return true;
    }

    @Override
    public boolean logout() {
        return true;
    }

}
