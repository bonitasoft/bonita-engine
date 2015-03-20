/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.process;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connectors.TestConnectorWithModifiedOutput;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class ProcessResolutionTest extends CommonAPISPIT {

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ParameterDefinition.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-531", keywords = { "process resolution" })
    @Test
    public void parameterUnset() throws BonitaException {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt();
        builder.createNewInstance("resolve", "1.0").addParameter("param1", String.class.getName()).addAutomaticTask("step1");
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive);
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.UNRESOLVED, deploymentInfo.getConfigurationState());

        final List<Problem> problems = getProcessAPI().getProcessResolutionProblems(definition.getId());
        Assert.assertEquals(1, problems.size());
        final Problem problem = problems.get(0);
        Assert.assertEquals(Problem.Level.ERROR, problem.getLevel());
        Assert.assertEquals("parameter", problem.getResource());
        Assert.assertNotNull(problem.getDescription());

        deleteProcess(definition.getId());
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ParameterDefinition.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-1881", keywords = { "process resolution" })
    @Test
    public void importParametersComputesProcessResolution() throws BonitaException {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt();
        builder.createNewInstance("resolve", "1.0").addParameter("param1", String.class.getName()).addActor("Leader", true).addUserTask("step1", "Leader");
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive);
        final ActorInstance initiator = getProcessAPI().getActorInitiator(definition.getId());
        getProcessAPI().addUserToActor(initiator.getId(), getSession().getUserId());

        ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.UNRESOLVED, deploymentInfo.getConfigurationState());

        getProcessAPI().importParameters(definition.getId(), "param1=toto".getBytes());

        // try to fix windows issue
        deploymentInfo = getProcessAPI().getProcessDeploymentInfo(getProcessAPI().getProcessDefinition(definition.getId()).getId());
        // deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());

        Assert.assertEquals(ConfigurationState.RESOLVED, deploymentInfo.getConfigurationState());

        deleteProcess(definition.getId());
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ParameterDefinition.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-531", keywords = { "process resolution" })
    @Test
    public void resolveParameter() throws BonitaException {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt();
        builder.createNewInstance("resolve", "1.0").addParameter("param1", String.class.getName()).addActor("Leader", true).addUserTask("step1", "Leader");
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive);
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.UNRESOLVED, deploymentInfo.getConfigurationState());

        getProcessAPI().updateParameterInstanceValue(definition.getId(), "param1", "value");
        final ActorInstance initiator = getProcessAPI().getActorInitiator(definition.getId());
        getProcessAPI().addUserToActor(initiator.getId(), getSession().getUserId());
        final List<Problem> problems = getProcessAPI().getProcessResolutionProblems(definition.getId());
        Assert.assertEquals(0, problems.size());

        deleteProcess(definition.getId());
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, Connector.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-556", keywords = { "process resolution" })
    @Test
    public void resolveConnectorImplementation() throws BonitaException, IOException {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt();
        builder.createNewInstance("resolve", "1.0").addAutomaticTask("auto")
                .addConnector("exec", "org.bonitasoft.connector.testConnectorWithOutput", "1.0", ConnectorEvent.ON_ENTER);
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive);
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.UNRESOLVED, deploymentInfo.getConfigurationState());

        final String implSourchFile = "/org/bonitasoft/engine/connectors/TestConnectorWithModifiedOutput.impl";
        final Class<TestConnectorWithModifiedOutput> implClass = TestConnectorWithModifiedOutput.class;
        final byte[] connectorImplementationArchive = generateZipByteArrayForConnector(implSourchFile, implClass);
        getProcessAPI().setConnectorImplementation(definition.getId(), "org.bonitasoft.connector.testConnectorWithOutput", "1.0",
                connectorImplementationArchive);
        final List<Problem> problems = getProcessAPI().getProcessResolutionProblems(definition.getId());
        Assert.assertEquals(0, problems.size());

        deleteProcess(definition.getId());
    }

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
