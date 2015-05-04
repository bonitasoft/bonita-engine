/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import com.bonitasoft.engine.bpm.parameter.ParameterCriterion;
import com.bonitasoft.engine.bpm.parameter.ParameterInstance;
import com.bonitasoft.engine.bpm.parameter.ParameterNotFoundException;
import com.bonitasoft.engine.bpm.process.impl.ParameterDefinitionBuilder;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.connectors.TestConnectorWithModifiedOutput;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.process.ProcessManagementIT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcessParameterTest extends CommonAPISPIT {

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

        final ProcessDefinition definition = deployProcess(businessArchive.done());
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
        ParameterDefinitionBuilder bee = processBuilder.addParameter("bee", String.class.getCanonicalName());
        ParameterDefinitionBuilder plop = bee.addDescription("plop");
        ParameterDefinitionBuilder bear = plop.addParameter("bear", String.class.getCanonicalName());
        bear
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = deployProcess(businessArchive.done());
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

        final ProcessDefinition definition = deployProcess(businessArchive.done());
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
        final ProcessDefinition processDefinition = deployProcess(
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
        waitForUserTask(processInstance, "step2");

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
        final ProcessDefinition processDef = deployProcess(businessArchive);
        final long processDefinitionId = processDef.getId();
        getProcessAPI().enableProcess(processDefinitionId);
        processInstance = getProcessAPI().startProcess(processDefinitionId);
        waitForUserTask(processInstance, "step2");

        assertEquals("bcd", getProcessAPI().getParameterInstance(processDefinitionId, paraName).getValue());
        final ConnectorImplementationDescriptor connectorRedeploy = getProcessAPI().getConnectorImplementation(processDefinitionId, connectorId,
                connectorVersion);
        assertEquals(TestConnectorWithModifiedOutput.class.getName(), connectorRedeploy.getImplementationClassName());
        assertEquals(connectorVersion, connectorRedeploy.getVersion());
        assertEquals(ConfigurationState.RESOLVED, getProcessAPI().getProcessDeploymentInfo(processDefinitionId).getConfigurationState());

        disableAndDeleteProcess(processDef);
        deleteUser(user);
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

    // TODO move these methods to an util class
    private byte[] generateZipByteArrayForConnector(final String implSourceFile, final Class<?> implClass) throws IOException {
        // generate byte arrays of .impl and .jar files
        InputStream stream = null;
        ByteArrayOutputStream baos = null;
        ZipOutputStream zos = null;
        try {
            stream = this.getClass().getResourceAsStream(implSourceFile);
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
