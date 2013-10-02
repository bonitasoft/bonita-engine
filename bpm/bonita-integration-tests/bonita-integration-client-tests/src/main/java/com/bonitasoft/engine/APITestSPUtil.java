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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.IdentityAPI;
import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.MonitoringAPI;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.api.ReportingAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.bpm.breakpoint.Breakpoint;
import com.bonitasoft.engine.bpm.breakpoint.BreakpointCriterion;
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator;
import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.reporting.Report;
import com.bonitasoft.engine.reporting.ReportSearchDescriptor;

public class APITestSPUtil extends APITestUtil {

    protected static final Logger LOGGER = LoggerFactory.getLogger(APITestSPUtil.class);

    private LogAPI logAPI;

    private MonitoringAPI monitoringAPI;

    private PlatformMonitoringAPI platformMonitoringAPI;

    private ReportingAPI reportingAPI;

    protected PlatformMonitoringAPI getPlatformMonitoringAPI() {
        return platformMonitoringAPI;
    }

    protected void setReportingAPI(final ReportingAPI reportingAPI) {
        this.reportingAPI = reportingAPI;
    }

    protected void setPlatformMonitoringAPI(final PlatformMonitoringAPI platformMonitoringAPI) {
        this.platformMonitoringAPI = platformMonitoringAPI;
    }

    protected MonitoringAPI getMonitoringAPI() {
        return monitoringAPI;
    }

    protected void setMonitoringAPI(final MonitoringAPI monitoringAPI) {
        this.monitoringAPI = monitoringAPI;
    }

    @Override
    public ProcessAPI getProcessAPI() {
        return (ProcessAPI) super.getProcessAPI();
    }

    @Override
    public IdentityAPI getIdentityAPI() {
        return (IdentityAPI) super.getIdentityAPI();
    }

    @Override
    public ProfileAPI getProfileAPI() {
        return (ProfileAPI) super.getProfileAPI();
    }

    public ReportingAPI getReportingAPI() {
        return reportingAPI;
    }

    protected void loginWith(final String userName, final String password, final long tenantId) throws BonitaException {
        setSession(SPBPMTestUtil.loginTenant(userName, password, tenantId));
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
        setMonitoringAPI(TenantAPIAccessor.getMonitoringAPI(getSession()));
        setPlatformMonitoringAPI(TenantAPIAccessor.getPlatformMonitoringAPI(getSession()));
    }

    @Override
    protected void loginWith(final String userName, final String password) throws BonitaException {
        setSession(SPBPMTestUtil.loginOnDefaultTenant(userName, password));
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
        setMonitoringAPI(TenantAPIAccessor.getMonitoringAPI(getSession()));
        setPlatformMonitoringAPI(TenantAPIAccessor.getPlatformMonitoringAPI(getSession()));
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
        setMonitoringAPI(TenantAPIAccessor.getMonitoringAPI(getSession()));
        setPlatformMonitoringAPI(TenantAPIAccessor.getPlatformMonitoringAPI(getSession()));
        logAPI = TenantAPIAccessor.getLogAPI(getSession());
    }

    protected void login(final long tenantId) throws BonitaException {
        setSession(SPBPMTestUtil.loginTenant(tenantId));
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
        setMonitoringAPI(TenantAPIAccessor.getMonitoringAPI(getSession()));
        setPlatformMonitoringAPI(TenantAPIAccessor.getPlatformMonitoringAPI(getSession()));
    }

    @Override
    protected void logout() throws BonitaException {
        SPBPMTestUtil.logoutTenant(getSession());
        setSession(null);
        setIdentityAPI(null);
        setProcessAPI(null);
        setProfileAPI(null);
        setMonitoringAPI(null);
        setPlatformMonitoringAPI(null);
        setReportingAPI(null);
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

    protected void deleteSupervisor(final Serializable id) throws BonitaException {
        final Map<String, Serializable> deleteParameters = new HashMap<String, Serializable>();
        deleteParameters.put(SUPERVISOR_ID_KEY, id);
        getCommandAPI().execute("deleteSupervisor", deleteParameters);
    }

    protected ManualTaskCreator buildManualTaskCreator(final long parentTaskId, final String taskName, final String displayName, final long assignTo,
            final String description, final Date dueDate, final TaskPriority priority) {
        final ManualTaskCreator taskCreator = new ManualTaskCreator(parentTaskId, taskName);
        taskCreator.setDisplayName(displayName);
        taskCreator.setAssignTo(assignTo);
        taskCreator.setDescription(description);
        taskCreator.setDueDate(dueDate);
        taskCreator.setPriority(priority);
        return taskCreator;
    }

    public List<String> checkExistenceOfBreakpoints() throws CommandNotFoundException, CommandExecutionException, CommandParameterizationException {
        final List<String> messages = new ArrayList<String>();
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10000);
        parameters.put("sort", BreakpointCriterion.DEFINITION_ID_ASC);
        final List<Breakpoint> breakpoints = (List<Breakpoint>) getCommandAPI().execute("getBreakpoints", parameters);
        if (breakpoints.size() > 0) {
            final StringBuilder bpBuilder = new StringBuilder("Breakpoints are still present: ");
            for (final Breakpoint breakpoint : breakpoints) {
                bpBuilder.append(breakpoint.getElementName()).append(", ");
                getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpoint.getId()));
            }
            messages.add(bpBuilder.toString());
        }
        return messages;
    }

    public List<String> checkExistenceOfReports() throws SearchException {
        final List<String> messages = new ArrayList<String>();
        // only for non-default tenants:
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000).filter(ReportSearchDescriptor.PROVIDED, false);
        final SearchResult<Report> reportSR = getReportingAPI().searchReports(build.done());
        final List<Report> reports = reportSR.getResult();
        if (reportSR.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Some Reports are still present: ");
            for (final Report report : reports) {
                messageBuilder.append(report.getName()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public void assignAndExecuteStep(final ActivityInstance activityInstance, final User user) throws BonitaException {
        assignAndExecuteStep(activityInstance.getId(), user.getId());
    }

    public void waitForUserTaskAndExecuteIt(final String taskName, final ProcessInstance processInstance, final User user) throws Exception {
        final ActivityInstance waitForUserTask = waitForUserTask(taskName, processInstance);
        assignAndExecuteStep(waitForUserTask, user);
    }

}
