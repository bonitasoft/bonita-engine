/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.api.ProcessAPI;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.connectors.TestExternalConnector;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.wait.WaitProcessToFinishAndBeArchived;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

/**
 * @author Baptiste Mesta
 */
public class ClusterTests extends CommonAPISPIT {

    static Logger LOGGER = LoggerFactory.getLogger(ClusterTests.class);

    private User user;

    @Before
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        // init the context here
        changeToNode2();
        final PlatformSession platformSession = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        if (!platformAPI.isNodeStarted()) {
            platformAPI.startNode();
        }
        changeToNode1();
                    loginOnDefaultTenantWith(USERNAME, PASSWORD);
    }

    @After
    public void afterTest () throws Exception {
            deleteUser(user);
            changeToNode1();
        logoutOnTenant();
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
        final Map<String, String> parameters = new HashMap<String, String>(2);
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
    public void useSameSessionOnBothNodes() throws Exception {
        final User createUser = getIdentityAPI().createUser("john", "bpm", "John", "Doe");
        changeToNode2();
        final User userByUserName = getIdentityAPI().getUserByUserName("john");
        assertEquals(createUser, userByUserName);
        getIdentityAPI().deleteUser("john");

    }

    @Test
    public void classLoaderClustered() throws Exception {

        // Input expression
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("executeConnectorOnActivityInstance",
                "1.0");
        designProcessDefinition.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step0", ACTOR_NAME);
        addUserTask.addShortTextData("text", new ExpressionBuilder().createConstantStringExpression("default"));
        addUserTask
                .addConnector("aConnector", "org.bonitasoft.connector.testConnectorWithOutput", "1.0", ConnectorEvent.ON_ENTER)
                .addInput("input1", new ExpressionBuilder().createConstantStringExpression("inputValue"))
                .addOutput(
                        new OperationBuilder().createSetDataOperation("text", new ExpressionBuilder().createInputExpression("output1", String.class.getName())));

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition.done());

        addConnectorImplemWithDependency(businessArchiveBuilder, "/org/bonitasoft/engine/connectors/TestConnectorWithOutput.impl",
                "TestConnectorWithOutput.impl", TestConnectorWithOutput.class, "TestConnectorWithOutput.jar");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, user);

        // start it on node 2
        changeToNode2();
        getProcessAPI().startProcess(processDefinition.getId());

        final List<HumanTaskInstance> waitForPendingTasks = waitForPendingTasks(user.getId(), 1);
        assertEquals("inputValue", getProcessAPI().getActivityDataInstance("text", waitForPendingTasks.get(0).getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    private void addConnectorImplemWithDependency(final BusinessArchiveBuilder bizArchive, final String implemPath, final String implemName,
            final Class<? extends AbstractConnector> dependencyClassName, final String dependencyJarName) throws IOException {
        bizArchive.addConnectorImplementation(new BarResource(implemName, IOUtils.toByteArray(this.getClass().getResourceAsStream(implemPath))));
        bizArchive.addClasspathResource(new BarResource(dependencyJarName, IOUtil.generateJar(dependencyClassName)));
    }

    /*
     * Check that works are executed on node that started it
     */
    @Test
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

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        // wait the all automatic task finish
        waitForPendingTasks(user.getId(), 10);

        // check that at least one data is set to "Node1" and at least one to "Node2"
        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 20);
        boolean node1Ok = false;
        boolean node2Ok = false;

        for (final DataInstance dataInstance : processDataInstances) {
            node1Ok |= "Node1".equals(dataInstance.getValue());
            node2Ok |= "Node2".equals(dataInstance.getValue());
        }
        assertTrue("no data has 'Node1' as value: no work were executed on node1", node1Ok);
        assertFalse("a data has 'Node2' as value: a work was executed on node2", node2Ok);

        disableAndDeleteProcess(processDefinition);
    }

    /*
 * Check that works are executed on node that started it
 */
    @Test
    public void executeAProcessInCluster() throws Exception {

        // Input expression
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("executeConnectorOnActivityInstance",
                "1.0");
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addConnector("connectorOnEnter1", "org.bonitasoft.connector.testExternalConnector", "1.0", ConnectorEvent.ON_ENTER);
        designProcessDefinition.addConnector("connectorOnEnter2","org.bonitasoft.connector.testExternalConnector","1.0",ConnectorEvent.ON_ENTER).addInput("param1", new ExpressionBuilder().createConstantStringExpression("plop"));
        designProcessDefinition.addConnector("connectorOnFinish","org.bonitasoft.connector.testExternalConnector","1.0",ConnectorEvent.ON_FINISH).addInput("param1",new ExpressionBuilder().createConstantStringExpression("plop"));

        designProcessDefinition.addStartEvent("start");
        // create 10 tasks that set a data with the node name
        for (int i = 1; i <= 10; i++) {
            designProcessDefinition.addShortTextData("data" + i, null);
            designProcessDefinition.addAutomaticTask("autoStep" + i).addOperation(
                    new OperationBuilder().createSetDataOperation(
                            "data" + i,
                            new ExpressionBuilder().createGroovyScriptExpression("getNodeName", "return System.getProperty(\"node.name\");",
                                    String.class.getName())));
            designProcessDefinition.addTransition("start", "autoStep" + i);
        }

        ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndConnector(designProcessDefinition, ACTOR_NAME, user, "TestExternalConnector.impl",
                TestExternalConnector.class,
                "TestExternalConnector.jar");
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitProcessToFinishAndBeArchived(processInstance, getProcessAPI());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void pause_tenant_should_be_effective_on_all_nodes() throws Exception {
        changeToNode1();
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();

        getTenantManagementAPI().pause();

        assertThat(getTenantManagementAPI().isPaused()).isTrue();

        changeToNode2();

        assertThat(getTenantManagementAPI().isPaused()).isTrue();

        getTenantManagementAPI().resume();
    }

    @Test
    public void should_pause_tenant_then_stop_start_node_dont_restart_elements() throws Exception {
        // given: 2 node with 1 node having running processes

        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder pdb = new ProcessDefinitionBuilder().createNewInstance("loop process def", "1.0");
        pdb.addAutomaticTask("step1").addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(100));
        final DesignProcessDefinition dpd = pdb.done();
        final ProcessDefinition pd = deployAndEnableProcess(dpd);
        final ProcessInstance pi = getProcessAPI().startProcess(pd.getId());

        logoutOnTenant();
        // when: we stop node 1
        stopPlatform();
        changeToNode2();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        try {
            // then: node2 should finish the work
            waitProcessToFinishAndBeArchived(pi, getProcessAPI());
        } finally {
            // cleanup
            disableAndDeleteProcess(pd);
            logoutOnTenant();
            changeToNode1();
            startPlatform();
            loginOnDefaultTenantWith(USERNAME, PASSWORD);
        }
    }

    public void waitProcessToFinishAndBeArchived(final ProcessInstance processInstance, ProcessAPI processAPI) throws Exception {
        final boolean waitUntil = new WaitProcessToFinishAndBeArchived(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstance, processAPI).waitUntil();
        if (!waitUntil) {
            printFlowNodes(processInstance);
            printArchivedFlowNodes(processInstance);
        }
        assertTrue("Process was not finished", waitUntil);
    }

}
