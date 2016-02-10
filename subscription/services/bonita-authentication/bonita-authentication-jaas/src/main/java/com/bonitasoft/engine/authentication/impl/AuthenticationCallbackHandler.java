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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.engine.authentication.AuthenticationConstants;

import com.bonitasoft.engine.authentication.impl.cas.CASCallbackHandlerDelegate;

public class AuthenticationCallbackHandler implements CallbackHandler {

    private final Map<String, Serializable> credentials;

    protected CASCallbackHandlerDelegate casCallbackHandlerDelegate = new CASCallbackHandlerDelegate();

    public AuthenticationCallbackHandler(final Map<String, Serializable> credentials) {
        if (credentials == null) {
            throw new IllegalArgumentException("Authentication Credentials cannot be null");
        }
        this.credentials = credentials;
    }

    @Override
    public void handle(final Callback[] callbacks) {
        for (final Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                final NameCallback nc = (NameCallback) callback;
                handleName(nc);
            } else if (callback instanceof PasswordCallback) {
                final PasswordCallback pc = (PasswordCallback) callback;
                handlePassword(pc);
            }
        }
    }

    /**
     * @param pc
     */
    protected void handlePassword(final PasswordCallback pc) {
        if (StringUtils.equals(pc.getPrompt(), AuthenticationConstants.CAS_TICKET)) {
            String password = casCallbackHandlerDelegate.getCASTicket(credentials);
            if (password != null) {
                pc.setPassword(password.toCharArray());
            }
        } else {
            String password = String.valueOf(credentials.get(AuthenticationConstants.BASIC_PASSWORD));
            pc.setPassword(password.toCharArray());
        }
    }

    /**
     * @param nc
     */
    protected void handleName(final NameCallback nc) {
        if (StringUtils.equals(nc.getPrompt(), AuthenticationConstants.CAS_SERVICE)) {
            String userName = casCallbackHandlerDelegate.getCASService(credentials);
            if (userName != null) {
                nc.setName(userName);
            }
        } else if (credentials.get(AuthenticationConstants.BASIC_USERNAME) != null) {
            String userName = String.valueOf(credentials.get(AuthenticationConstants.BASIC_USERNAME));
            nc.setName(userName);
        }
    }

}
