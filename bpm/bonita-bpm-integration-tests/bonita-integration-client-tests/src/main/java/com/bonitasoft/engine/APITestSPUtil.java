/*******************************************************************************
 * Copyright (C) 2009-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.connectors.TestConnector3;
import org.bonitasoft.engine.connectors.TestConnectorEngineExecutionContext;
import org.bonitasoft.engine.connectors.TestConnectorThatThrowException;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.platform.StartNodeException;
import org.bonitasoft.engine.platform.StopNodeException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.IdentityAPI;
import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.MonitoringAPI;
import com.bonitasoft.engine.api.PageAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.api.ReportingAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.api.ThemeAPI;
import com.bonitasoft.engine.bpm.breakpoint.Breakpoint;
import com.bonitasoft.engine.bpm.breakpoint.BreakpointCriterion;
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.monitoring.MonitoringException;
import com.bonitasoft.engine.reporting.Report;
import com.bonitasoft.engine.reporting.ReportSearchDescriptor;

public class APITestSPUtil extends APITestUtil {

    protected static final Logger LOGGER = LoggerFactory.getLogger(APITestSPUtil.class);

    private LogAPI logAPI;

    private MonitoringAPI monitoringAPI;

    private PlatformMonitoringAPI platformMonitoringAPI;

    private ReportingAPI reportingAPI;

    private ThemeAPI themeAPI;

    private TenantManagementAPI tenantManagementAPI;

    private PageAPI pageAPI;

    @Override
    public PlatformLoginAPI getPlatformLoginAPI() throws BonitaException {
        return PlatformAPIAccessor.getPlatformLoginAPI();
    }

    @Override
    public PlatformAPI getPlatformAPI(final PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return PlatformAPIAccessor.getPlatformAPI(session);
    }

    @Override
    public LoginAPI getLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getLoginAPI();
    }

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
    public ThemeAPI getThemeAPI() {
        return themeAPI;
    }

    protected void setThemeAPI(final ThemeAPI themeAPI) {
        this.themeAPI = themeAPI;
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

    public LogAPI getLogAPI() {
        return logAPI;
    }

    public TenantManagementAPI getTenantManagementAPI() {
        return tenantManagementAPI;
    }

    public void setTenantManagementAPI(final TenantManagementAPI tenantManagementAPI) {
        this.tenantManagementAPI = tenantManagementAPI;
    }

    public void loginOnTenantWith(final String userName, final String password, final long tenantId) throws BonitaException {
        setSession(BPMTestSPUtil.loginOnTenant(userName, password, tenantId));
        setAPIs();
    }

    @Override
    public void loginWith(final String userName, final String password) throws BonitaException {
        setSession(BPMTestSPUtil.loginOnDefaultTenant(userName, password));
        setAPIs();
    }

    @Override
    public void login() throws BonitaException {
        setSession(BPMTestSPUtil.loginOnDefaultTenantWithDefaultTechnicalLogger());
        setAPIs();
    }

    protected void loginOnTenantWithTechnicalLogger(final long tenantId) throws BonitaException {
        setSession(BPMTestSPUtil.loginOnTenantWithTechnicalLogger(tenantId));
        setAPIs();
    }

    private void setAPIs() throws BonitaException {
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setThemeAPI(TenantAPIAccessor.getThemeAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
        setPageAPI(TenantAPIAccessor.getPageAPI(getSession()));
        setMonitoringAPI(TenantAPIAccessor.getMonitoringAPI(getSession()));
        setPlatformMonitoringAPI(TenantAPIAccessor.getPlatformMonitoringAPI(getSession()));
        setTenantManagementAPI(TenantAPIAccessor.getTenantManagementAPI(getSession()));
        logAPI = TenantAPIAccessor.getLogAPI(getSession());
    }

    protected void setPageAPI(final PageAPI pageAPI) {
        this.pageAPI = pageAPI;
    }

    public PageAPI getPageAPI() {
        return pageAPI;
    }

    @Override
    public void logout() throws BonitaException {
        BPMTestSPUtil.logoutOnTenant(getSession());
        setSession(null);
        setIdentityAPI(null);
        setProcessAPI(null);
        setProfileAPI(null);
        setThemeAPI(null);
        setMonitoringAPI(null);
        setPlatformMonitoringAPI(null);
        setReportingAPI(null);
        setCommandAPI(null);
        setTenantManagementAPI(null);
        logAPI = null;
    }

    public void stopPlatform() throws BonitaException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, StopNodeException,
            StartNodeException {
        final PlatformSession loginPlatform = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        platformAPI.stopNode();
        logoutPlatform(loginPlatform);
    }

    public void startPlatform() throws BonitaException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, StopNodeException,
            StartNodeException {
        final PlatformSession loginPlatform = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        platformAPI.startNode();
        logoutPlatform(loginPlatform);
    }

    public void stopAndStartPlatform() throws BonitaException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, StopNodeException,
            StartNodeException {
        final PlatformSession loginPlatform = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        platformAPI.stopNode();
        platformAPI.startNode();
        logoutPlatform(loginPlatform);
    }

    public long createAndActivateTenant(final String uniqueName) throws BonitaException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(uniqueName);
        stringBuilder.append("_");
        stringBuilder.append(System.currentTimeMillis());
        String tenantUniqueName = stringBuilder.toString();

        return BPMTestSPUtil.createAndActivateTenantWithDefaultTechnicalLogger(tenantUniqueName);
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

    protected ManualTaskCreator buildManualTaskCreator(final long parentTaskId, final String taskName, final long assignTo, final String description,
            final Date dueDate, final TaskPriority priority) {
        final ManualTaskCreator taskCreator = new ManualTaskCreator(parentTaskId, taskName);
        taskCreator.setDisplayName(taskName);
        taskCreator.setAssignTo(assignTo);
        taskCreator.setDescription(description);
        taskCreator.setDueDate(dueDate);
        taskCreator.setPriority(priority);
        return taskCreator;
    }

    public List<String> checkNoActiveTransactions() throws MonitoringException {
        final List<String> messages = new ArrayList<String>();
        long numberOfActiveTransactions = getMonitoringAPI().getNumberOfActiveTransactions();
        if (numberOfActiveTransactions != 0) {
            // retry 50 ms after because the might still be some jobs/works that run
            try {
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                throw new MonitoringException("interrupted while sleeping");
            }
            numberOfActiveTransactions = getMonitoringAPI().getNumberOfActiveTransactions();
            if (numberOfActiveTransactions != 0) {
                messages.add("There are " + numberOfActiveTransactions + " active transactions.");
            }
        }
        return messages;
    }

    public List<String> checkNoBreakpoints() throws CommandNotFoundException, CommandExecutionException, CommandParameterizationException {
        final List<String> messages = new ArrayList<String>();
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10000);
        parameters.put("sort", BreakpointCriterion.DEFINITION_ID_ASC);
        @SuppressWarnings("unchecked")
        final List<Breakpoint> breakpoints = (List<Breakpoint>) getCommandAPI().execute("getBreakpoints", parameters);
        if (!breakpoints.isEmpty()) {
            final StringBuilder bpBuilder = new StringBuilder("Breakpoints are still present: ");
            for (final Breakpoint breakpoint : breakpoints) {
                bpBuilder.append(breakpoint.getElementName()).append(", ");
                getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpoint.getId()));
            }
            messages.add(bpBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoReports() throws SearchException {
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

    /**
     * @return
     * @throws BonitaException
     */
    public Collection<? extends String> checkNoDataMappings() throws BonitaException {
        final Integer count = new Integer(getReportingAPI().selectList("SELECT count(*) FROM data_mapping").split("\n")[1]);
        if (count == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList("There is some data mapping present: " + count);
    }

    public void assignAndExecuteStep(final ActivityInstance activityInstance, final User user) throws BonitaException {
        assignAndExecuteStep(activityInstance.getId(), user.getId());
    }

    @Override
    public HumanTaskInstance waitForUserTaskAndExecuteIt(final String taskName, final long processInstanceId, final User user) throws Exception {
        final ActivityInstance waitForUserTask = waitForUserTask(taskName, processInstanceId);
        assignAndExecuteStep(waitForUserTask, user);
        return (HumanTaskInstance) waitForUserTask;
    }

    public ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final long userId)
            throws BonitaException {
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        getProcessAPI().addUserToActor(actorName, processDefinition, userId);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployProcessWithActorAndTestConnector3(final ProcessDefinitionBuilderExt processDefinitionBuilderExt, final String actorName,
            final User user) throws BonitaException, IOException {
        final List<BarResource> connectorImplementations = Arrays.asList(buildBarResource("/org/bonitasoft/engine/connectors/TestConnector3.impl",
                "TestConnector3.impl"));
        final List<BarResource> generateConnectorDependencies = Arrays.asList(buildBarResource(TestConnector3.class, "TestConnector3.jar"),
                buildBarResource(VariableStorage.class, "VariableStorage.jar"));
        return deployProcessWithActorAndConnector(processDefinitionBuilderExt, actorName, user, connectorImplementations, generateConnectorDependencies);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorEngineExecutionContext(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployProcessWithActorAndConnector(processDefinitionBuilderExt, actorName, user,
                "/org/bonitasoft/engine/connectors/TestConnectorEngineExecutionContext.impl", "TestConnectorEngineExecutionContext.impl",
                TestConnectorEngineExecutionContext.class, "TestConnectorEngineExecutionContext.jar");
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorWithCustomType(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployProcessWithActorAndConnector(processDefinitionBuilderExt, actorName, user,
                Arrays.asList(buildBarResource("/org/bonitasoft/engine/connectors/TestConnectorWithCustomType.impl", "TestConnectorWithCustomType.impl")),
                Arrays.asList(buildBarResource("/org/bonitasoft/engine/connectors/connector-with-custom-type.bak", "connector-with-custom-type.jar")));
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorLongToExecute(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployProcessWithActorAndConnector(processDefinitionBuilderExt, actorName, user,
                Arrays.asList(buildBarResource("/org/bonitasoft/engine/connectors/TestConnectorLongToExecute.impl",
                        "TestConnectorLongToExecute.impl")), Collections.<BarResource> emptyList());
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorThatThrowException(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final String actor, final User user) throws BonitaException, IOException {
        return deployProcessWithActorAndTestConnectorThatThrowExceptionAndParameter(processDefinitionBuilderExt, actor, user, null);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorThatThrowExceptionAndParameter(
            final ProcessDefinitionBuilderExt processDefinitionBuilderExt, final String actorName, final User user, final Map<String, String> parameters)
            throws BonitaException, IOException {
        return deployProcessWithActorAndConnectorAndParameter(processDefinitionBuilderExt, actorName, user, parameters,
                "/org/bonitasoft/engine/connectors/TestConnectorThatThrowException.impl", "TestConnectorThatThrowException.impl",
                TestConnectorThatThrowException.class, "TestConnectorThatThrowException.jar");
    }

    public ProcessDefinition deployProcessWithActorAndTestConnector(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final String actor, final User user) throws BonitaException, IOException {
        return deployProcessWithActorAndTestConnectorAndParameter(processDefinitionBuilderExt, actor, user, null);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorAndParameter(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final String actorName, final User user, final Map<String, String> parameters) throws BonitaException, IOException {
        final List<BarResource> connectorImplementations = Arrays.asList(buildBarResource("/org/bonitasoft/engine/connectors/TestConnector.impl",
                "TestConnector.impl"));
        final List<BarResource> generateConnectorDependencies = Arrays.asList(buildBarResource(TestConnector.class, "TestConnector.jar"),
                buildBarResource(VariableStorage.class, "VariableStorage.jar"));
        return deployProcessWithActorAndConnectorAndParameter(processDefinitionBuilderExt, actorName, user, connectorImplementations,
                generateConnectorDependencies, parameters);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorWithOutput(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final String actor, final User user) throws BonitaException, IOException {
        return deployProcessWithActorAndTestConnectorThatThrowExceptionAndParameter(processDefinitionBuilderExt, actor, user, null);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorWithOutputAndParameter(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final String actorName, final User user, final Map<String, String> parameters) throws BonitaException, IOException {
        return deployProcessWithActorAndConnectorAndParameter(processDefinitionBuilderExt, actorName, user, parameters,
                "/org/bonitasoft/engine/connectors/TestConnectorWithOutput.impl", "TestConnectorWithOutput.impl", TestConnectorWithOutput.class,
                "TestConnectorWithOutput.jar");
    }

    public ProcessDefinition deployProcessWithActorAndConnector(final ProcessDefinitionBuilderExt processDefinitionBuilderExt, final String actorName,
            final User user, final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = buildBusinessArchiveWithConnector(processDefinitionBuilderExt, connectorImplementations,
                generateConnectorDependencies);
        return deployAndEnableWithActor(businessArchiveBuilder.done(), actorName, user);
    }

    public ProcessDefinition deployProcessWithConnector(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies)
            throws InvalidBusinessArchiveFormatException, BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = buildBusinessArchiveWithConnector(processDefinitionBuilderExt, connectorImplementations,
                generateConnectorDependencies);
        return deployAndEnableProcess(businessArchiveBuilder.done());

    }

    private ProcessDefinition deployProcessWithActorAndConnector(final ProcessDefinitionBuilderExt processDefinitionBuilderExt, final String actorName,
            final User user, final String path, final String name, final Class<? extends AbstractConnector> clazz,
            final String jarName) throws BonitaException, IOException {
        return deployProcessWithActorAndConnector(processDefinitionBuilderExt, actorName, user, Arrays.asList(buildBarResource(path, name)),
                Arrays.asList(buildBarResource(clazz, jarName)));
    }

    public ProcessDefinition deployProcessWithActorAndConnectorAndParameter(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final String actorName, final User user, final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies,
            final Map<String, String> parameters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = buildBusinessArchiveWithConnector(processDefinitionBuilderExt, connectorImplementations,
                generateConnectorDependencies);
        if (parameters != null) {
            businessArchiveBuilder.setParameters(parameters);
        }
        return deployAndEnableWithActor(businessArchiveBuilder.done(), actorName, user);
    }

    private ProcessDefinition deployProcessWithActorAndConnectorAndParameter(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final String actorName, final User user, final Map<String, String> parameters, final String path, final String name,
            final Class<? extends AbstractConnector> clazz, final String jarName) throws BonitaException, IOException {
        return deployProcessWithActorAndConnectorAndParameter(processDefinitionBuilderExt, actorName, user, Arrays.asList(buildBarResource(path, name)),
                Arrays.asList(buildBarResource(clazz, jarName)), parameters);
    }

    private BusinessArchiveBuilder buildBusinessArchiveWithConnector(final ProcessDefinitionBuilderExt processDefinitionBuilderExt,
            final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies) throws InvalidProcessDefinitionException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefinitionBuilderExt.done());
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }
        return businessArchiveBuilder;
    }

    public BarResource buildBarResource(final String path, final String name) throws IOException {
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream(path);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        return new BarResource(name, byteArray);
    }
}
