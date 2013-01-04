package com.bonitasoft.engine.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.model.ActivationState;
import org.bonitasoft.engine.bpm.model.ConfigurationState;
import org.bonitasoft.engine.bpm.model.ConnectorEvent;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDeploymentInfo;
import org.bonitasoft.engine.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.connectors.TestConnector2;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.process.ProcessManagementTest;
import org.bonitasoft.engine.process.TestStates;
import org.bonitasoft.engine.util.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.ParameterSorting;
import com.bonitasoft.engine.bpm.model.ParameterInstance;
import com.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import com.bonitasoft.engine.exception.InvalidParameterValueException;
import com.bonitasoft.engine.exception.ParameterNotFoundException;

public class ProcessParameterTest extends CommonAPISPTest {

    private static final String PROCESS_VERSION = "1.0";

    private static final String PROCESS_NAME = "ProcessManagementTest";

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @Test
    public void getNoParametersWhenAddingNoParameters() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final int numberOfParamters = getProcessAPI().getNumberOfParameterInstances(definition.getId());
        Assert.assertEquals(0, numberOfParamters);

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test
    public void getNumberOfParameters() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("key1", String.class.getCanonicalName()).addParameter("key2", String.class.getCanonicalName())
                .addParameter("key3", String.class.getCanonicalName()).addParameter("key4", String.class.getCanonicalName()).addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final int numberOfParamters = getProcessAPI().getNumberOfParameterInstances(definition.getId());
        Assert.assertEquals(4, numberOfParamters);

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void getNumberOfParametersThrowsAnExceptionBecauseTheProcessDoesNotExist() throws BonitaException {
        getProcessAPI().getNumberOfParameterInstances(45);
    }

    @Test
    public void getNoParameters() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterSorting.NAME_DESC);
        Assert.assertEquals(0, parameters.size());

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test
    public void getParameters() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterSorting.NAME_DESC);
        Assert.assertEquals(2, parameters.size());
        final ParameterInstance firstParameter = parameters.get(0);
        Assert.assertEquals("key.2", firstParameter.getName());
        Assert.assertEquals("bos", firstParameter.getValue());
        final ParameterInstance secondParameter = parameters.get(1);
        Assert.assertEquals("key1", secondParameter.getName());
        Assert.assertEquals("engine", secondParameter.getValue());

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test
    public void getParameter() throws BonitaException {
        final String parameterValue = "a very important piece of information";
        final String parameterName = "myParam1";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("getParameter", "1.0");
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
        Assert.assertEquals(parameterName, parameter.getName());
        Assert.assertEquals(parameterValue, parameter.getValue());
        Assert.assertEquals("Parameter description", parameter.getDescription());

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test
    public void deployWithNullParam() throws BonitaException {
        final String parameterName = "myParam1";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("getParameter", "1.0");
        processBuilder.addParameter("myParam1", String.class.getCanonicalName()).addParameter("myParam2", String.class.getCanonicalName())
                .addUserTask("userTask1", null);

        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put(parameterName, null);
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void getParameterOfAnUnknownProcess() throws BonitaException {
        getProcessAPI().getParameterInstance(123456789l, "unknown");
    }

    @Test(expected = ParameterNotFoundException.class)
    public void getUnknownParameter() throws BonitaException {
        final String parameterValue = "a very important piece of information";
        final String parameterName = "myParam1";
        final String wrongParameterName = "wrongParameterName";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("getUnknownParameter", "1.0");
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
            getProcessAPI().deleteProcess(definition.getId());
        }
    }

    @Test
    public void updateParameter() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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

        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterSorting.NAME_DESC);
        Assert.assertEquals(2, parameters.size());
        final ParameterInstance firstParameter = parameters.get(0);
        Assert.assertEquals("key.2", firstParameter.getName());
        Assert.assertEquals("bee", firstParameter.getValue());

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test(expected = ParameterNotFoundException.class)
    public void updateFailsWhenParameterDoesNotExist() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
            getProcessAPI().deleteProcess(definition.getId());
        }
    }

    @Test
    public void updateAParameterWhichHasNotGotAnyDefaultValueWhichResolvesTheProcess() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
        Assert.assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());
        getProcessAPI().updateParameterInstanceValue(definition.getId(), "bear", "sleepy");
        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());
        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test
    public void updateAParameterWhichHasNotGotAnyDefaultValue() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        getProcessAPI().updateParameterInstanceValue(definition.getId(), "bear", "sleepy");
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());
        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void updateFailsWhenProcessDefinitionDoesNotExist() throws BonitaException {
        getProcessAPI().updateParameterInstanceValue(12, "key1", "bee");
    }

    @Test
    public void sortParametersByNameAsc() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterSorting.NAME_ASC);
        Assert.assertEquals(4, parameters.size());
        Assert.assertEquals("squirrel", parameters.get(0).getName());
        Assert.assertEquals("donkey", parameters.get(1).getName());
        Assert.assertEquals("bee", parameters.get(2).getName());
        Assert.assertEquals("bear", parameters.get(3).getName());

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test
    public void sortParametersByNameDesc() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterSorting.NAME_DESC);
        Assert.assertEquals(4, parameters.size());
        Assert.assertEquals("squirrel", parameters.get(3).getName());
        Assert.assertEquals("donkey", parameters.get(2).getName());
        Assert.assertEquals("bee", parameters.get(1).getName());
        Assert.assertEquals("bear", parameters.get(0).getName());

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test
    public void getPageOne() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 2, ParameterSorting.NAME_ASC);
        Assert.assertEquals(2, parameters.size());
        Assert.assertEquals("squirrel", parameters.get(0).getName());
        Assert.assertEquals("donkey", parameters.get(1).getName());

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test
    public void getPageTwo() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 1, 2, ParameterSorting.NAME_ASC);
        Assert.assertEquals(2, parameters.size());
        Assert.assertEquals("bee", parameters.get(0).getName());
        Assert.assertEquals("bear", parameters.get(1).getName());

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test(expected = PageOutOfRangeException.class)
    public void getPageTwoOutOfBound() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
        try {
            getProcessAPI().getParameterInstances(definition.getId(), 1, 8, ParameterSorting.NAME_ASC);
        } finally {
            getProcessAPI().deleteProcess(definition.getId());
        }
    }

    @Test
    public void unresolvedDependencies() throws Exception {
        final long processId = createProcessWithUnresolvedParametersAnhdDeployIt();
        new ExecutionInSession() {

            @Override
            public void run() throws Exception {
                try {
                    final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processId);
                    Assert.assertEquals(TestStates.getProcessDepInfoUnresolvedState(), processDeploymentInfo.getConfigurationState());
                } finally {
                    getProcessAPI().deleteProcess(processId);
                }
            }
        }.executeInSession();

    }

    private long createProcessWithUnresolvedParametersAnhdDeployIt() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
    public void resolvedDependencies() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
        Assert.assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());

        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test(expected = InvalidParameterValueException.class)
    public void cannotUpdateAParameterWithNullValue() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        try {
            getProcessAPI().updateParameterInstanceValue(definition.getId(), "bear", null);
        } finally {
            getProcessAPI().deleteProcess(definition.getId());
        }
    }

    @Test
    public void showResolvedAndUnresolvedParameters() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addDescription("description").addParameter("bear", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterSorting.NAME_ASC);
        Assert.assertEquals(2, parameters.size());
        final ParameterInstance parameter1 = parameters.get(0);
        Assert.assertEquals("bee", parameter1.getName());
        Assert.assertEquals("busy", parameter1.getValue());
        Assert.assertEquals("description", parameter1.getDescription());
        Assert.assertEquals(String.class.getCanonicalName(), parameter1.getType());
        final ParameterInstance parameter2 = parameters.get(1);
        Assert.assertEquals("bear", parameter2.getName());
        Assert.assertNull(parameter2.getValue());
        Assert.assertNull(parameter2.getDescription());
        Assert.assertEquals(String.class.getCanonicalName(), parameter2.getType());
        getProcessAPI().deleteProcess(definition.getId());
    }

    @Test
    public void testImportParameters() throws Exception {
        final InputStream stream = ProcessManagementTest.class.getResourceAsStream("info.properties");
        assertNotNull(stream);
        final byte[] parametersXML = IOUtils.toByteArray(stream);
        final String paraName1 = "name";
        final String paraName2 = "age";

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
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

        getProcessAPI().deleteProcess(processDefinition.getId());
    }

    @Test
    public void testExportZipProcessContentUnderHome() throws Exception {
        final String delivery = "Delivery men";
        final String johnName = "john";
        final User user = createUser(johnName, "bpm");

        final String valueOfInput = "valueOfInput";
        final String inputName = "input1";
        final String connectorId = "org.bonitasoft.connector.testConnector";
        final String connectorVersion = "1.0";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput);
        final String paraName = "Para1";
        final long userId = getIdentityAPI().getUserByUserName(johnName).getId();
        final Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put(paraName, "abc");

        final ProcessDefinitionBuilder designProcessDefinition = buildProcessWithOutputConnectorAndParameter(delivery, inputName, connectorId,
                connectorVersion, input1Expression, paraName);

        final ProcessDefinition processDefinition = deployProcessWithTestConnectorAndParameter(delivery, userId, designProcessDefinition, paraMap);
        final long proDefId = processDefinition.getId();
        startProcessAndWaitForTask(proDefId, "step2");

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(proDefId);
        assertNotNull(processDeploymentInfo);
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        assertEquals("abc", getProcessAPI().getParameterInstance(proDefId, paraName).getValue());
        final ConnectorImplementationDescriptor connector = getProcessAPI().getConnectorImplementation(proDefId, connectorId, connectorVersion);
        assertEquals(TestConnector.class.getName(), connector.getImplementationClassName());
        assertEquals(connectorVersion, connector.getVersion());
        assertEquals(ActivationState.ENABLED, getProcessAPI().getProcessDeploymentInfo(proDefId).getActivationState());

        getProcessAPI().updateParameterInstanceValue(proDefId, paraName, "bcd");
        final String implSourchFile = "/org/bonitasoft/engine/connectors/TestConnector2.impl";
        final Class<TestConnector2> implClass = TestConnector2.class;
        final byte[] connectorImplementationArchive = generateZipByteArrayForConnector(implSourchFile, implClass);
        getProcessAPI().setConnectorImplementation(proDefId, connectorId, connectorVersion, connectorImplementationArchive);

        assertEquals("bcd", getProcessAPI().getParameterInstance(proDefId, paraName).getValue());
        final ConnectorImplementationDescriptor connectorUpdate = getProcessAPI().getConnectorImplementation(proDefId, connectorId, connectorVersion);
        assertEquals(TestConnector2.class.getName(), connectorUpdate.getImplementationClassName());
        assertEquals(connectorVersion, connectorUpdate.getVersion());
        assertEquals(ConfigurationState.RESOLVED, getProcessAPI().getProcessDeploymentInfo(proDefId).getConfigurationState());

        final byte[] resbytes = getProcessAPI().exportBarProcessContentUnderHome(proDefId);
        final ByteArrayInputStream baiStream = new ByteArrayInputStream(resbytes);

        disableAndDelete(processDefinition);

        final BusinessArchive businessArchive = BusinessArchiveFactory.readBusinessArchive(baiStream);
        final ProcessDefinition processDef = getProcessAPI().deploy(businessArchive);
        final long processDefinitionId = processDef.getId();
        getProcessAPI().enableProcess(processDefinitionId);
        startProcessAndWaitForTask(processDefinitionId, "step2");

        assertEquals("bcd", getProcessAPI().getParameterInstance(processDefinitionId, paraName).getValue());
        final ConnectorImplementationDescriptor connectorRedeploy = getProcessAPI().getConnectorImplementation(processDefinitionId, connectorId,
                connectorVersion);
        assertEquals(TestConnector2.class.getName(), connectorRedeploy.getImplementationClassName());
        assertEquals(connectorVersion, connectorRedeploy.getVersion());
        assertEquals(ConfigurationState.RESOLVED, getProcessAPI().getProcessDeploymentInfo(processDefinitionId).getConfigurationState());

        disableAndDelete(processDef);
        deleteUser(user.getId());
    }

    private ProcessDefinitionBuilder buildProcessWithOutputConnectorAndParameter(final String delivery, final String inputName, final String connectorId,
            final String connectorVersion, final Expression input1Expression, final String paraName) {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnector", connectorId, connectorVersion, ConnectorEvent.ON_ENTER)
                .addInput(inputName, input1Expression);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addParameter(paraName, String.class.getCanonicalName());
        return designProcessDefinition;
    }

    private ProcessDefinition deployProcessWithTestConnectorAndParameter(final String delivery, final long userId,
            final ProcessDefinitionBuilder designProcessDefinition, final Map<String, String> parameters) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        if (parameters != null) {
            businessArchive.setParameters(parameters);
        }
        final BusinessArchiveBuilder businessArchiveBuilder = businessArchive.setProcessDefinition(designProcessDefinition.done());
        final List<BarResource> connectorImplementations = generateConnectorImplementations();
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        final List<BarResource> generateConnectorDependencies = generateConnectorDependencies();
        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(delivery, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    @Test
    public void testParametersAreWellTypped() throws Exception {
        final String actor = "acting";
        final User jack = createUserAndLogin("jack", "leaking_caldron");
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("testCantResolveDataInExpressionInDataDefaultValue",
                "1");
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

        final ProcessDefinition processDefinition = deployAndEnableWithActorAndParameters(processBuilder.done(), actor, jack, parameters);

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

        disableAndDelete(processDefinition);
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
            final String baseName = implSourceFile.substring(implSourceFile.lastIndexOf("/") + 1, implSourceFile.lastIndexOf("."));
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

    private List<BarResource> generateConnectorImplementations() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(2);
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnector.impl", "TestConnector.impl");
        // addResource(resources, "/org/bonitasoft/engine/connectors/TestConnector2.impl", "TestConnector2.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorWithOutput.impl", "TestConnectorWithOutput.impl");
        return resources;
    }

    private List<BarResource> generateConnectorDependencies() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(2);
        addResource(resources, TestConnector.class, "TestConnector.jar");
        // addResource(resources, TestConnector2.class, "TestConnector2.jar");
        addResource(resources, TestConnectorWithOutput.class, "TestConnectorWithOutput.jar");
        addResource(resources, VariableStorage.class, "VariableStorage.jar");
        return resources;
    }

    private void addResource(final List<BarResource> resources, final String path, final String name) throws IOException {
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream(path);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        resources.add(new BarResource(name, byteArray));
    }

    private void addResource(final List<BarResource> resources, final Class<?> clazz, final String name) throws IOException {
        final byte[] data = IOUtil.generateJar(clazz);
        resources.add(new BarResource(name, data));
    }
}
