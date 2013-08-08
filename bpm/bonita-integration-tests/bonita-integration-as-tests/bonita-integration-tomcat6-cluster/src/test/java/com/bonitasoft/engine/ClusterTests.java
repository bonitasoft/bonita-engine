/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

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
        login();
        logout();
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
        setConnectionPort("7180");
        changeApis();
    }

    protected void changeToNode2() throws Exception {
        setConnectionPort("7280");
        changeApis();
    }

    private void setConnectionPort(String port) throws Exception {
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
    public void useSameSessionOnBothNodes() throws Exception {
        User createUser = getIdentityAPI().createUser("john", "bpm", "John", "Doe");
        changeToNode2();
        User userByUserName = getIdentityAPI().getUserByUserName("john");
        assertEquals(createUser, userByUserName);
        getIdentityAPI().deleteUser("john");

    }

    @Test
    public void classLoaderClustered() throws Exception {

        // Input expression
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("executeConnectorOnActivityInstance",
                "1.0");
        designProcessDefinition.addActor("actor");
        UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step0", "actor");
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

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser("actor", user.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());

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

}
