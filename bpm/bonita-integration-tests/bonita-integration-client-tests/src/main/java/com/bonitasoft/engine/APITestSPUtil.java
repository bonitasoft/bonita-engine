/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.check.CheckNbOfHumanTasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.api.ReportingAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.log.Log;

import static org.junit.Assert.assertTrue;

public class APITestSPUtil extends APITestUtil {

    protected static final Logger LOGGER = LoggerFactory.getLogger(APITestSPUtil.class);

    private LogAPI logAPI;

    private ProfileAPI profileAPI;

    private ReportingAPI reportingAPI;

    public static int DEFAULT_REPEAT = 50;

    public static int DEFAULT_TIMEOUT = 2000;

    @Override
    public ProfileAPI getProfileAPI() {
        return profileAPI;
    }

    private void setProfileAPI(final ProfileAPI profileAPI) {
        this.profileAPI = profileAPI;
    }

    @Override
    public ReportingAPI getReportingAPI() {
        return reportingAPI;
    }

    private void setReportingAPI(final ReportingAPI reportingAPI) {
        this.reportingAPI = reportingAPI;
    }

    protected void loginWith(final String userName, final String password, final long tenantId) throws BonitaException {
        setSession(SPBPMTestUtil.loginTenant(userName, password, tenantId));
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
    }

    @Override
    protected void loginWith(final String userName, final String password) throws BonitaException {
        setSession(SPBPMTestUtil.loginOnDefaultTenant(userName, password));
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
        logAPI = TenantAPIAccessor.getLogAPI(getSession());
    }

    @Override
    protected void login() throws BonitaException {
        setSession(SPBPMTestUtil.loginOnDefaultTenant());
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
        logAPI = TenantAPIAccessor.getLogAPI(getSession());
    }

    protected void login(final long tenantId) throws BonitaException {
        setSession(SPBPMTestUtil.loginTenant(tenantId));
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
    }

    @Override
    protected void logout() throws BonitaException {
        SPBPMTestUtil.logoutTenant(getSession());
        setSession(null);
        setIdentityAPI(null);
        setProcessAPI(null);
        setProfileAPI(null);
        setCommandAPI(null);
    }

    public LogAPI getLogAPI() {
        return logAPI;
    }

    protected boolean containsLogWithActionType(final List<Log> logs, final String actionType, final int minimalFrequency) {
        int count = 0;
        final Iterator<Log> iterator = logs.iterator();
        while (iterator.hasNext() && count < minimalFrequency) {
            final Log log = iterator.next();
            if (actionType.equals(log.getActionType())) {
                count++;
            }
        }

        return count == minimalFrequency;
    }

    protected SearchResult<HumanTaskInstance> waitForHumanTasks(final int repeatEach, final int timeout, final int nbTasks, final String taskName,
            final long processInstanceId) throws Exception {
        final CheckNbOfHumanTasks checkNbOfHumanTasks = new CheckNbOfHumanTasks(repeatEach, timeout, true, nbTasks, new SearchOptionsBuilder(0, 10000)
                .filter(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstanceId).filter(HumanTaskInstanceSearchDescriptor.NAME, taskName)
                .done(), getProcessAPI());
        assertTrue(checkNbOfHumanTasks.waitUntil());
        return checkNbOfHumanTasks.getHumanTaskInstances();
    }

    protected void deleteSupervisor(final Serializable id) throws BonitaException {
        final Map<String, Serializable> deleteParameters = new HashMap<String, Serializable>();
        deleteParameters.put(SUPERVISOR_ID_KEY, id);
        getCommandAPI().execute("deleteSupervisor", deleteParameters);
    }

    @Override
    public ProcessAPI getProcessAPI() {
        return (ProcessAPI) super.getProcessAPI();
    }

}
