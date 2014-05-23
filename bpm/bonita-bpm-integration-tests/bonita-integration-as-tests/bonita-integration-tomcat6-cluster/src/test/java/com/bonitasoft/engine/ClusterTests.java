/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.api.TenantIsPausedException;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.platform.TenantCreator;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
public class ClusterTests extends CommonAPISPTest {

    static Logger LOGGER = LoggerFactory.getLogger(ClusterTests.class);

    private User user;

    @Before
    public void beforeTest() throws Exception {
        login();
        user = createUser(USERNAME, PASSWORD);
        logout();
        // init the context here
        changeToNode2();
        PlatformSession platformSession = loginPlatform();
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.startNode();
        changeToNode1();
        loginWith(USERNAME, PASSWORD);
    }

    @After
    public void afterTest() throws Exception {
        deleteUser(user.getId());
        changeToNode1();
        logout();
    }

    protected void changeToNode1() throws Exception {
        setConnectionPort("8186");
        changeApis();
    }

    protected void changeToNode2() throws Exception {
        setConnectionPort("8187");
        changeApis();
    }

    private void setConnectionPort(final String port) {
        Map<String, String> parameters = new HashMap<String, String>(2);
        parameters.put("server.url", "http://localhost:" + port);
        parameters.put("application.name", "bonita");
        APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, parameters);

        LOGGER.info("changed to " + port);
    }

    private void changeApis() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
        setMonitoringAPI(TenantAPIAccessor.getMonitoringAPI(getSession()));
        setPlatformMonitoringAPI(TenantAPIAccessor.getPlatformMonitoringAPI(getSession()));
    }

    @Test
    @Ignore
    public void useSameSessionOnBothNodes() throws Exception {
        User createUser = getIdentityAPI().createUser("john", "bpm", "John", "Doe");
        changeToNode2();
        User userByUserName = getIdentityAPI().getUserByUserName("john");
        assertEquals(createUser, userByUserName);
        getIdentityAPI().deleteUser("john");

    }

    @Test
    @Ignore
    public void classLoaderClustered() throws Exception {

        // Input expression
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("executeConnectorOnActivityInstance",
                "1.0");
        designProcessDefinition.addActor(ACTOR_NAME);
        UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step0", ACTOR_NAME);
        addUserTask.addShortTextData("text", new ExpressionBuilder().createConstantStringExpression("default"));
        addUserTask
                .addConnector("aConnector", "org.bonitasoft.connector.testConnectorWithOutput", "1.0", ConnectorEvent.ON_ENTER)
                .addInput("input1", new ExpressionBuilder().createConstantStringExpression("inputValue"))
                .addOutput(
                        new OperationBuilder().createSetDataOperation("text", new ExpressionBuilder().createInputExpression("output1", String.class.getName())));

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition.done());

        addConnectorImplemWithDependency(businessArchiveBuilder, "/org/bonitasoft/engine/connectors/TestConnectorWithOutput.impl",
                "TestConnectorWithOutput.impl",
                TestConnectorWithOutput.class, "TestConnectorWithOutput.jar");

        final ProcessDefinition processDefinition = deployAndEnableWithActor(businessArchiveBuilder.done(), ACTOR_NAME, user);

        // start it on node 2
        changeToNode2();
        getProcessAPI().startProcess(processDefinition.getId());

        List<HumanTaskInstance> waitForPendingTasks = waitForPendingTasks(user.getId(), 1);
        assertEquals("inputValue", getProcessAPI().getActivityDataInstance("text", waitForPendingTasks.get(0).getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    private void addConnectorImplemWithDependency(final BusinessArchiveBuilder bizArchive, final String implemPath, final String implemName,
            final Class<? extends AbstractConnector> dependencyClassName, final String dependencyJarName) throws IOException {
        bizArchive.addConnectorImplementation(new BarResource(implemName, IOUtils.toByteArray(BPMRemoteTests.class.getResourceAsStream(implemPath))));
        bizArchive.addClasspathResource(new BarResource(dependencyJarName, IOUtil.generateJar(dependencyClassName)));
    }

    /*
     * Check that works are executed on node that started it
     */
    @Test
    @Ignore
    public void clusteredWorkServiceIT() throws Exception {

        // Input expression
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("executeConnectorOnActivityInstance",
                "1.0");
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addStartEvent("start");
        // create 10 tasks that set a data with the node name
        for (int i = 1; i <= 10; i++) {
            designProcessDefinition.addShortTextData("data" + i, null);
            designProcessDefinition.addAutomaticTask("autoStep" + i).addOperation(
                    new OperationBuilder().createSetDataOperation(
                            "data" + i,
                            new ExpressionBuilder().createGroovyScriptExpression("getNodeName", "return System.getProperty(\"node.name\");",
                                    String.class.getName())));
            designProcessDefinition.addUserTask("step" + i, ACTOR_NAME);
            designProcessDefinition.addTransition("start", "autoStep" + i);
            designProcessDefinition.addTransition("autoStep" + i, "step" + i);
        }

        ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        // wait the all automatic task finish
        waitForPendingTasks(user.getId(), 10);

        // check that at least one data is set to "Node1" and at least one to "Node2"
        List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 20);
        boolean node1Ok = false;
        boolean node2Ok = false;

        for (DataInstance dataInstance : processDataInstances) {
            node1Ok |= "Node1".equals(dataInstance.getValue());
            node2Ok |= "Node2".equals(dataInstance.getValue());
        }
        assertTrue("no data has 'Node1' as value: no work were executed on node1", node1Ok);
        assertFalse("a data has 'Node2' as value: a work was executed on node2", node2Ok);

        disableAndDeleteProcess(processDefinition);
    }

    /*
     * Check that:
     * when pause a tenant on one node:
     * * normal user is logged out on both nodes
     * * technical user can't access API not accessible in pause
     * * processes with timers are not executed in both nodes
     * When resume
     * * processes are executed
     */
    @Test
    @Ignore
    public void should_pause_tenant_have_effect_on_all_nodes() throws Exception {
        final String systemProperty = "bonita.process.executed";

        // create an other tenant for later
        logout();
        PlatformSession loginPlatform = loginPlatform();
        PlatformAPI platformAPI = (PlatformAPI) getPlatformAPI(loginPlatform);
        long otherTenantId = platformAPI.createTenant(new TenantCreator("tenantToCheckSysProp", "tenantToCheckSysProp", "testIconName",
                "testIconPath", "install", "install"));
        platformAPI.activateTenant(otherTenantId);
        logoutPlatform(loginPlatform);
        loginWith(USERNAME, PASSWORD);

        ProcessDefinition processDefinition = deployProcessThatSetASystemPropertyOnTheNode(systemProperty);
        // start an instance on both node
        System.out.println("[test] start process on node 1");
        getProcessAPI().startProcess(processDefinition.getId());
        changeToNode2();
        System.out.println("[test] start process on node 2");
        getProcessAPI().startProcess(processDefinition.getId());
        System.out.println("[test] pause on node 2");
        getTenantManagementAPI().pause();
        try {
            System.out.println("[test] try to call getNumberOfProcess on node 2");
            getProcessAPI().getNumberOfProcessInstances();
            fail("should not be logged with a normal user anymore");
        } catch (InvalidSessionException e) {
            // ok
        }
        changeToNode1();
        try {
            System.out.println("[test] try to call getNumberOfProcess on node 1");
            getProcessAPI().getNumberOfProcessInstances();
            fail("should not be logged with a normal user anymore");
        } catch (InvalidSessionException e) {
            // ok
        }
        login();
        // should work on both nodes
        try {
            System.out.println("[test] try to call getNumberOfProcess with tech user on node 1");
            getProcessAPI().getNumberOfProcessInstances();
            fail("should not be able to acces this method in pause");
        } catch (TenantIsPausedException e) {
            // ok
        }
        changeToNode2();
        // should work on both nodes
        try {
            System.out.println("[test] try to call getNumberOfProcess with tech user on node 2");
            getProcessAPI().getNumberOfProcessInstances();
            fail("should not be able to acces this method in pause");
        } catch (TenantIsPausedException e) {
            // ok
        }
        logout();

        Thread.sleep(6000);// wait 6 secondes (more than 5 ) then change using an other tenant that the system property did not change

        loginOnTenantWith("install", "install", otherTenantId);
        System.out.println("[test] deploy process on an other tenant");
        final ProcessDefinition processDefinitionOnTheOtherTenant = deployProcessThatSetASystemPropertyOnTheNode(systemProperty);
        // check not executed on node 1
        System.out.println("[test] check if executed on node 1");
        assertFalse(isExecuted(systemProperty, processDefinitionOnTheOtherTenant));
        changeToNode2();
        // check not executed on node 2
        System.out.println("[test] check if executed on node 2");
        assertFalse(isExecuted(systemProperty, processDefinitionOnTheOtherTenant));

        logout();
        login();
        System.out.println("[test] resume on node 2");
        getTenantManagementAPI().resume();
        // wait that quartz launch its jobs
        Thread.sleep(3000);
        logout();
        loginOnTenantWith("install", "install", otherTenantId);

        Thread.sleep(10000);
        // check executed on node 2 and/or 1 (because matching of events result in non deterministic target node)
        System.out.println("[test] check if executed on node 1 or node 2");
        boolean executedOnNode2 = isExecuted(systemProperty, processDefinitionOnTheOtherTenant);

        changeToNode1();
        boolean executedOnNode1 = isExecuted(systemProperty, processDefinitionOnTheOtherTenant);
        assertTrue(executedOnNode1 || executedOnNode2);

        logout();
        loginPlatform = loginPlatform();
        platformAPI = (PlatformAPI) getPlatformAPI(loginPlatform);
        platformAPI.deactiveTenant(otherTenantId);
        platformAPI.deleteTenant(otherTenantId);
        logoutPlatform(loginPlatform);
        loginWith(USERNAME, PASSWORD);

        disableAndDeleteProcess(processDefinition);
    }

    private boolean isExecuted(final String systemProperty, final ProcessDefinition processDefinitionOnTheOtherTenant) throws ExpressionEvaluationException,
            InvalidExpressionException {
        String evaluateExpressionOnProcessDefinition = (String) getProcessAPI().evaluateExpressionOnProcessDefinition(
                new ExpressionBuilder().createGroovyScriptExpression("getSystemProp", "return System.getProperty(\"" + systemProperty + "\")",
                        String.class.getName()), Collections.<String, Serializable> emptyMap(), processDefinitionOnTheOtherTenant.getId());
        return Boolean.valueOf(evaluateExpressionOnProcessDefinition);
    }

    private ProcessDefinition deployProcessThatSetASystemPropertyOnTheNode(final String systemProperty) throws InvalidExpressionException, BonitaException,
            InvalidProcessDefinitionException {
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("executeConnectorOnActivityInstance",
                "1.0");
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addStartEvent("start");
        designProcessDefinition.addShortTextData("data", new ExpressionBuilder().createConstantStringExpression("none"));
        designProcessDefinition.addIntermediateCatchEvent("timer1").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(2500));
        designProcessDefinition.addIntermediateCatchEvent("timer2").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(2500));
        designProcessDefinition.addAutomaticTask("autoStep").addOperation(
                new OperationBuilder().createSetDataOperation(
                        "data",
                        new ExpressionBuilder().createGroovyScriptExpression("getNodeName",
                                "System.setProperty('" + systemProperty + "','true');return System.getProperty(\"node.name\");",
                                String.class.getName())));
        designProcessDefinition.addUserTask("step", ACTOR_NAME);
        designProcessDefinition.addTransition("start", "timer1");
        designProcessDefinition.addTransition("timer1", "timer2");
        designProcessDefinition.addTransition("timer2", "autoStep");
        designProcessDefinition.addTransition("autoStep", "step");

        return deployAndEnableWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
    }

    @Test
    public void should_pause_tenant_then_stop_start_node_dont_restart_elements() throws Exception {
        // given: 1 tenant that is paused
        final long tenantId = createAndActivateTenant("MyTenant_");

        loginWith(USERNAME, PASSWORD);

        ProcessDefinitionBuilder pdb = new ProcessDefinitionBuilder().createNewInstance("loop process def", "1.0");
        pdb.addAutomaticTask("step1").addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(100));
        DesignProcessDefinition dpd = pdb.done();
        ProcessDefinition pd = deployAndEnableProcess(dpd);
        ProcessInstance pi = getProcessAPI().startProcess(pd.getId());

        logout();
        // when: we stop and start the node
        stopPlatform();
        changeToNode2();
        loginWith(USERNAME, PASSWORD);
        // then: work service is not runnning
        waitForProcessToFinishAndBeArchived(pi);

        // cleanup
        disableAndDeleteProcess(pd);
        logout();
        changeToNode1();
        startPlatform();
    }

}
