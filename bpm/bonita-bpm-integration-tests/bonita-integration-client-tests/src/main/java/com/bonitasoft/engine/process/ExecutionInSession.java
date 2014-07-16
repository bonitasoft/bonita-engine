/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.process;

import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.engine.BPMTestSPUtil;

/**
 * @author Baptiste Mesta
 */
public abstract class ExecutionInSession {

    private APISession session;

    private final String password;

    private final String username;

    private final long tenantId;

    public APISession getSession() {
        return session;
    }

    public ExecutionInSession() {
        username = null;
        password = null;
        tenantId = -1;
    }

    public ExecutionInSession(final String username, final String password) {
        this.username = username;
        this.password = password;
        tenantId = -1;
    }

    public ExecutionInSession(final String username, final String password, final long tenantId) {
        this.username = username;
        this.password = password;
        this.tenantId = tenantId;
    }

    public ExecutionInSession(final long tenantId) {
        this.tenantId = tenantId;
        username = null;
        password = null;
    }

    public abstract void run() throws Exception;

    public void setSession(final APISession session) {
        this.session = session;
    }

    public void executeInSession() throws Exception {
        final APISession session;
        if (username != null) {
            if (tenantId > 0) {
                session = BPMTestSPUtil.loginOnTenant(username, password, tenantId);
            } else {
                session = BPMTestSPUtil.loginOnDefaultTenant(username, password);
            }
        } else {
            if (tenantId > 0) {
                session = BPMTestSPUtil.loginOnTenantWithDefaultTechnicalLogger(tenantId);
            } else {
                session = BPMTestSPUtil.loginOnDefaultTenantWithDefaultTechnicalLogger();
            }
        }
        setSession(session);
        run();
        BPMTestSPUtil.logoutOnTenant(session);
    }

}
