package com.bonitasoft.engine.process;

import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.engine.SPBPMTestUtil;

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
                session = SPBPMTestUtil.loginTenant(username, password, tenantId);
            } else {
                session = SPBPMTestUtil.loginDefaultTenant(username, password);
            }
        } else {
            if (tenantId > 0) {
                session = SPBPMTestUtil.loginTenant(tenantId);
            } else {
                session = SPBPMTestUtil.loginDefaultTenant();
            }
        }
        setSession(session);
        run();
        SPBPMTestUtil.logoutTenant(session);
    }

}
