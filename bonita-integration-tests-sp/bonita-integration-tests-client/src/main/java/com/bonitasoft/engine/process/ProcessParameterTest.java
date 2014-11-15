/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.connectors.TestConnectorWithModifiedOutput;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.process.ProcessManagementIT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import com.bonitasoft.engine.bpm.parameter.ParameterCriterion;
import com.bonitasoft.engine.bpm.parameter.ParameterInstance;
import com.bonitasoft.engine.bpm.parameter.ParameterNotFoundException;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class ProcessParameterTest extends CommonAPISPTest {

    private static final String PROCESS_VERSION = "1.0";

    private static final String PROCESS_NAME = "ProcessManagementTest";

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @Test
    public void getNoParametersWhenAddingNoParameters() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final int numberOfParamters = getProcessAPI().getNumberOfParameterInstances(definition.getId());
        assertEquals(0, numberOfParamters);

        deleteProcess(definition);
    }

    @Test
    public void getNumberOfParameters() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("key1", String.class.getCanonicalName()).addParameter("key2", String.class.getCanonicalName())
                .addParameter("key3", String.class.getCanonicalName()).addParameter("key4", String.class.getCanonicalName()).addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final int numberOfParamters = getProcessAPI().getNumberOfParameterInstances(definition.getId());
        assertEquals(4, numberOfParamters);

        deleteProcess(definition);
    }

    @Test(expected = RetrieveException.class)
    public void getNumberOfParametersThrowsAnExceptionBecauseTheProcessDoesNotExist() {
        getProcessAPI().getNumberOfParameterInstances(45);
    }

    @Test
    public void getNoParameters() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_DESC);
        assertEquals(0, parameters.size());

        deleteProcess(definition);
    }

    @Test
    public void getParameters() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("key1", String.class.getCanonicalName()).addParameter("key.2", String.class.getCanonicalName())
                .addUserTask("userTask1", null);

        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key1", "engine");
        params.put("key.2", "bos");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_ASC);
        assertEquals(2, parameters.size());
        final ParameterInstance firstParameter = parameters.get(0);
        assertEquals("key.2", firstParameter.getName());
        assertEquals("bos", firstParameter.getValue());
        final ParameterInstance secondParameter = parameters.get(1);
        assertEquals("key1", secondParameter.getName());
        assertEquals("engine", secondParameter.getValue());

        deleteProcess(definition);
    }

    @Test
    public void getParameter() throws BonitaException {
        final String parameterValue = "a very important piece of information";
        final String parameterName = "myParam1";
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("getParameter", "1.0");
        processBuilder.addParameter("myParam1", String.class.getCanonicalName()).addDescription("Parameter description");
        processBuilder.addParameter("myParam2", String.class.getCanonicalName()).addUserTask("userTask1", null);

        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put(parameterName, parameterValue);
        params.put("myParam2", "an unused parameter");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final ParameterInstance parameter = getProcessAPI().getParameterInstance(definition.getId(), parameterName);
        assertEquals(parameterName, parameter.getName());
        assertEquals(parameterValue, parameter.getValue());
        assertEquals("Parameter description", parameter.getDescription());

        deleteProcess(definition);
    }

    @Test
    public void setProcessDataDefaultValueWithParameterValue() throws Exception {
        final User user = createUser("jules", "his_password");
        final String parameterName = "anotherParam";
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("setDataDefaultValueWithParameter", "9.23");
        processBuilder.addActor(ACTOR_NAME);
        final String aTask = "userTask1";
        final String dataName = "aData";
        processBuilder
                .addParameter(parameterName, String.class.getCanonicalName())
                .addData(dataName, String.class.getName(),
                        new ExpressionBuilder().createParameterExpression("takes value of default parameter value", parameterName, String.class.getName()))
                .addUserTask(aTask, ACTOR_NAME);

        final DesignProcessDefinition design = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(design);
        final Map<String, String> params = new HashMap<String, String>(1);
        final String paramValue = "4 is the answer";
        params.put(parameterName, paramValue);
        businessArchive.setParameters(params);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(aTask, processInstance.getId());
        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstance.getId());
        assertEquals(paramValue, dataInstance.getValue());

        disableAndDeleteProcess(processDefinition);

        deleteUser(user.getId());
    }

    @Test
    public void deployWithNullParam() throws BonitaException {
        final String parameterName = "myParam1";
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("getParameter", "1.0");
        processBuilder.addParameter("myParam1", String.class.getCanonicalName()).addParameter("myParam2", String.class.getCanonicalName())
                .addUserTask("userTask1", null);

        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put(parameterName, null);
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        deleteProcess(definition);
    }

    @Test(expected = RetrieveException.class)
    public void getParameterOfAnUnknownProcess() throws BonitaException {
        getProcessAPI().getParameterInstance(123456789l, "unknown");
    }

    @Test(expected = ParameterNotFoundException.class)
    public void getUnknownParameter() throws BonitaException {
        final String parameterValue = "a very important piece of information";
        final String parameterName = "myParam1";
        final String wrongParameterName = "wrongParameterName";
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("getUnknownParameter", "1.0");
        processBuilder.addParameter("myParam1", String.class.getCanonicalName()).addParameter("myParam2", String.class.getCanonicalName())
                .addUserTask("userTask1", null);

        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put(parameterName, parameterValue);
        params.put("myParam2", "an unused parameter");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        try {
            getProcessAPI().getParameterInstance(definition.getId(), wrongParameterName);
        } finally {
            deleteProcess(definition);
        }
    }

    @Test
    public void updateParameter() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("key1", String.class.getCanonicalName()).addParameter("key.2", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key1", "engine");
        params.put("key.2", "bos");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        getProcessAPI().updateParameterInstanceValue(definition.getId(), "key.2", "bee");

        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_ASC);
        assertEquals(2, parameters.size());
        final ParameterInstance firstParameter = parameters.get(0);
        assertEquals("key.2", firstParameter.getName());
        assertEquals("bee", firstParameter.getValue());

        deleteProcess(definition);
    }

    @Test(expected = ParameterNotFoundException.class)
    public void updateFailsWhenParameterDoesNotExist() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key1", "engine");
        params.put("key.2", "bos");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        try {
            getProcessAPI().updateParameterInstanceValue(definition.getId(), "key_2", "bee");
        } finally {
            deleteProcess(definition);
        }
    }

    @Test
    public void updateAParameterWhichHasNotGotAnyDefaultValueWhichResolvesTheProcess() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());
        getProcessAPI().updateParameterInstanceValue(definition.getId(), "bear", "sleepy");
        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());
        deleteProcess(definition);
    }

    @Test
    public void updateAParameterWhichHasNotGotAnyDefaultValue() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        getProcessAPI().updateParameterInstanceValue(definition.getId(), "bear", "sleepy");
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());
        deleteProcess(definition);
    }

    @Test(expected = UpdateException.class)
    public void updateFailsWhenProcessDefinitionDoesNotExist() throws BonitaException {
        getProcessAPI().updateParameterInstanceValue(12, "key1", "bee");
    }

    @Test
    public void sortParametersByNameAsc() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_ASC);
        assertEquals(4, parameters.size());
        assertEquals("bear", parameters.get(0).getName());
        assertEquals("bee", parameters.get(1).getName());
        assertEquals("donkey", parameters.get(2).getName());
        assertEquals("squirrel", parameters.get(3).getName());

        deleteProcess(definition);
    }

    @Test
    public void sortParametersByNameDesc() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_DESC);
        assertEquals(4, parameters.size());
        assertEquals("squirrel", parameters.get(0).getName());
        assertEquals("donkey", parameters.get(1).getName());
        assertEquals("bee", parameters.get(2).getName());
        assertEquals("bear", parameters.get(3).getName());

        deleteProcess(definition);
    }

    @Test
    public void getPageOne() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 2, ParameterCriterion.NAME_DESC);
        assertEquals(2, parameters.size());
        assertEquals("squirrel", parameters.get(0).getName());
        assertEquals("donkey", parameters.get(1).getName());

        deleteProcess(definition);
    }

    @Test
    public void getPageTwo() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 2, 2, ParameterCriterion.NAME_DESC);
        assertEquals(2, parameters.size());
        assertEquals("bee", parameters.get(0).getName());
        assertEquals("bear", parameters.get(1).getName());

        deleteProcess(definition);
    }

    @Test
    public void getPageTwoOutOfBound() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameterInstances = getProcessAPI().getParameterInstances(definition.getId(), 8, 8, ParameterCriterion.NAME_ASC);
        assertEquals(0, parameterInstances.size());
        deleteProcess(definition);
    }

    @Test
    public void unresolvedDependencies() throws Exception {
        final long processDefinitionId = createProcessWithUnresolvedParametersAndDeployIt();
        new ExecutionInSession() {

            @Override
            public void run() throws Exception {
                try {
                    final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinitionId);
                    assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());
                } finally {
                    deleteProcess(processDefinitionId);
                }
            }
        }.executeInSession();

    }

    private long createProcessWithUnresolvedParametersAndDeployIt() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        return definition.getId();
    }

    @Test
    public void emptyParameterIsAValidValue() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("emptyParameterIsAValidValue", "1.7");
        processBuilder.addParameter("Astronaut", String.class.getCanonicalName()).addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>(1);
        params.put("Astronaut", "");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());

        deleteProcess(definition);
    }

    @Test
    public void resolvedDependencies() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());

        deleteProcess(definition);
    }

    @Test
    public void updateAParameterWithNullValueAndTheProcessIsUnresolved() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("bee", "busy");
        params.put("bear", "bos");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, deploymentInfo.getConfigurationState());

        getProcessAPI().updateParameterInstanceValue(definition.getId(), "bear", null);
        deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, deploymentInfo.getConfigurationState());

        getProcessAPI().updateParameterInstanceValue(definition.getId(), "bear", "honey");
        deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, deploymentInfo.getConfigurationState());

        deleteProcess(definition);
    }

    @Test
    public void showResolvedAndUnresolvedParameters() throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addDescription("description").addParameter("bear", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_DESC);
        assertEquals(2, parameters.size());
        final ParameterInstance parameter1 = parameters.get(0);
        assertEquals("bee", parameter1.getName());
        assertEquals("busy", parameter1.getValue());
        assertEquals("description", parameter1.getDescription());
        assertEquals(String.class.getCanonicalName(), parameter1.getType());
        final ParameterInstance parameter2 = parameters.get(1);
        assertEquals("bear", parameter2.getName());
        assertNull(parameter2.getValue());
        assertNull(parameter2.getDescription());
        assertEquals(String.class.getCanonicalName(), parameter2.getType());
        deleteProcess(definition);
    }

    @Test
    public void testImportParameters() throws Exception {
        final InputStream stream = ProcessManagementIT.class.getResourceAsStream("info.properties");
        assertNotNull(stream);
        final byte[] parametersXML = IOUtils.toByteArray(stream);
        final String paraName1 = "name";
        final String paraName2 = "age";

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addParameter(paraName1, String.class.getCanonicalName());
        processDefinitionBuilder.addParameter(paraName2, Integer.class.getCanonicalName());
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        final long pDefinitionId = processDefinition.getId();
        getProcessAPI().importParameters(pDefinitionId, parametersXML);

        final ParameterInstance parameter = getProcessAPI().getParameterInstance(pDefinitionId, "name");
        assertEquals("zhangsan", parameter.getValue());
        final ParameterInstance parameterAge = getProcessAPI().getParameterInstance(pDefinitionId, "age");
        assertEquals("17", parameterAge.getValue());

        deleteProcess(processDefinition);
    }

    @Test
    public void testExportZipProcessContentUnderHome() throws Exception {
        final String delivery = "Delivery men";
        final String johnName = "john";
        final User user = createUser(johnName, "bpm");

        final String valueOfInput = "valueOfInput";
        final String inputName = "input1";
        final String connectorId = "org.bonitasoft.connector.testConnectorWithOutput";
        final String connectorVersion = "1.0";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput);
        final String paraName = "Para1";
        final Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put(paraName, "abc");

        final ProcessDefinitionBuilderExt designProcessDefinition = buildProcessWithOutputConnectorAndParameter(delivery, inputName, connectorId,
                connectorVersion, input1Expression, paraName);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorWithOutputAndParameter(designProcessDefinition, delivery,
                user, paraMap);
        final long proDefId = processDefinition.getId();
        ProcessInstance processInstance = getProcessAPI().startProcess(proDefId);
        assertNotNull(waitForUserTask("step2", processInstance.getId()));

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(proDefId);
        assertNotNull(processDeploymentInfo);
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        assertEquals("abc", getProcessAPI().getParameterInstance(proDefId, paraName).getValue());
        final ConnectorImplementationDescriptor connector = getProcessAPI().getConnectorImplementation(proDefId, connectorId, connectorVersion);
        assertEquals(TestConnectorWithOutput.class.getName(), connector.getImplementationClassName());
        assertEquals(connectorVersion, connector.getVersion());
        assertEquals(ActivationState.ENABLED, getProcessAPI().getProcessDeploymentInfo(proDefId).getActivationState());

        getProcessAPI().updateParameterInstanceValue(proDefId, paraName, "bcd");
        assertEquals("bcd", getProcessAPI().getParameterInstance(proDefId, paraName).getValue());

        final String implSourchFile = "/org/bonitasoft/engine/connectors/TestConnectorWithModifiedOutput.impl";
        final Class<TestConnectorWithModifiedOutput> implClass = TestConnectorWithModifiedOutput.class;
        final byte[] connectorImplementationArchive = generateZipByteArrayForConnector(implSourchFile, implClass);
        getProcessAPI().setConnectorImplementation(proDefId, connectorId, connectorVersion, connectorImplementationArchive);

        final ConnectorImplementationDescriptor connectorUpdate = getProcessAPI().getConnectorImplementation(proDefId, connectorId, connectorVersion);
        assertEquals(TestConnectorWithModifiedOutput.class.getName(), connectorUpdate.getImplementationClassName());
        assertEquals(connectorVersion, connectorUpdate.getVersion());
        assertEquals(ConfigurationState.RESOLVED, getProcessAPI().getProcessDeploymentInfo(proDefId).getConfigurationState());

        final byte[] resbytes = getProcessAPI().exportBarProcessContentUnderHome(proDefId);
        final ByteArrayInputStream baiStream = new ByteArrayInputStream(resbytes);

        disableAndDeleteProcess(processDefinition);

        final BusinessArchive businessArchive = BusinessArchiveFactory.readBusinessArchive(baiStream);
        final ProcessDefinition processDef = getProcessAPI().deploy(businessArchive);
        final long processDefinitionId = processDef.getId();
        getProcessAPI().enableProcess(processDefinitionId);
        processInstance = getProcessAPI().startProcess(processDefinitionId);
        waitForUserTask("step2", processInstance.getId());

        assertEquals("bcd", getProcessAPI().getParameterInstance(processDefinitionId, paraName).getValue());
        final ConnectorImplementationDescriptor connectorRedeploy = getProcessAPI().getConnectorImplementation(processDefinitionId, connectorId,
                connectorVersion);
        assertEquals(TestConnectorWithModifiedOutput.class.getName(), connectorRedeploy.getImplementationClassName());
        assertEquals(connectorVersion, connectorRedeploy.getVersion());
        assertEquals(ConfigurationState.RESOLVED, getProcessAPI().getProcessDeploymentInfo(processDefinitionId).getConfigurationState());

        disableAndDeleteProcess(processDef);
        deleteUser(user.getId());
    }

    private ProcessDefinitionBuilderExt buildProcessWithOutputConnectorAndParameter(final String delivery, final String inputName, final String connectorId,
            final String connectorVersion, final Expression input1Expression, final String paraName) {
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnector", connectorId, connectorVersion, ConnectorEvent.ON_ENTER)
                .addInput(inputName, input1Expression);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addParameter(paraName, String.class.getCanonicalName());
        return designProcessDefinition;
    }

    @Test
    public void testParametersAreWellTyped() throws Exception {
        final String actor = "acting";
        final User jack = createUserAndLogin("jack", "leaking_caldron");
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance(
                "testCantResolveDataInExpressionInDataDefaultValue", "1");
        processBuilder.addActor(actor).addDescription("Process to test archiving mechanism");
        processBuilder.addDoubleData("aData", null);
        processBuilder.addParameter("integerValue", String.class.getName());
        processBuilder.addParameter("booleanValue", String.class.getName());
        processBuilder.addParameter("doubleValue", String.class.getName());
        processBuilder.addUserTask("humanTask", actor);

        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("integerValue", "15");
        parameters.put("booleanValue", "true");
        parameters.put("doubleValue", "1.1");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndParameters(processBuilder.done(), actor, jack, parameters);

        final Expression integerParameter = new ExpressionBuilder().createParameterExpression("integerExpression", "integerValue", Integer.class.getName());
        final Expression isIntegerValueInteger = new ExpressionBuilder().createGroovyScriptExpression("testIntegerValueToBeInteger",
                "integerValue instanceof Integer", Boolean.class.getName(), integerParameter);

        final Expression booleanParameter = new ExpressionBuilder().createParameterExpression("booleanParameter", "booleanValue", Boolean.class.getName());
        final Expression isBooleanValueBoolean = new ExpressionBuilder().createGroovyScriptExpression("testBooleanValueToBeBoolean",
                "booleanValue instanceof Boolean", Boolean.class.getName(), booleanParameter);

        final Expression doubleParameter = new ExpressionBuilder().createParameterExpression("doubleExpression", "doubleValue", Double.class.getName());
        final Expression isDoubleValueDouble = new ExpressionBuilder().createGroovyScriptExpression("testDoubleValueToBeDouble",
                "doubleValue instanceof Double", Boolean.class.getName(), doubleParameter);

        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(0);
        assertThat((Boolean) getProcessAPI().evaluateExpressionOnProcessDefinition(isIntegerValueInteger, inputValues, processDefinition.getId()), is(true));
        assertThat((Boolean) getProcessAPI().evaluateExpressionOnProcessDefinition(isBooleanValueBoolean, inputValues, processDefinition.getId()), is(true));
        assertThat((Boolean) getProcessAPI().evaluateExpressionOnProcessDefinition(isDoubleValueDouble, inputValues, processDefinition.getId()), is(true));

        disableAndDeleteProcess(processDefinition);
        deleteUser(jack.getId());
    }

    // TODO move these methods to an util class
    private byte[] generateZipByteArrayForConnector(final String implSourceFile, final Class<?> implClass) throws IOException {
        // generate byte arrays of .impl and .jar files
        InputStream stream = null;
        ByteArrayOutputStream baos = null;
        ZipOutputStream zos = null;
        try {
            stream = BPMRemoteTests.class.getResourceAsStream(implSourceFile);
            assertNotNull(stream);
            final String baseName = implSourceFile.substring(implSourceFile.lastIndexOf('/') + 1, implSourceFile.lastIndexOf('.'));
            final byte[] byteArray = IOUtils.toByteArray(stream);
            final byte[] data = IOUtil.generateJar(implClass);
            // read bytes of files to zip file byte array
            baos = new ByteArrayOutputStream();
            zos = new ZipOutputStream(baos);
            ZipEntry entry = new ZipEntry(baseName + ".impl");
            entry.setSize(byteArray.length);
            zos.putNextEntry(entry);
            zos.write(byteArray);
            zos.closeEntry();
            entry = new ZipEntry(baseName + ".jar");
            entry.setSize(data.length);
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
            return baos.toByteArray();
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (zos != null) {
                zos.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
    }

}
