/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.setup.PlatformSetupAccessor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Frédéric Bouquet
 * @author Céline Souchet
 */
public class ProcessDeploymentIT extends TestWithUser {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void deployProcessInDisabledState() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));

        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        addUserToFirstActorOfProcess(user.getId(), processDefinition);

        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());
        getProcessAPI().enableProcess(processDefinition.getId());
        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void deployProcessFromFile() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        File folder = temporaryFolder.newFolder();
        File tempFile = new File(folder, "tempFile");
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, tempFile);

        // read from the file
        final BusinessArchive readBusinessArchive = BusinessArchiveFactory.readBusinessArchive(tempFile);

        final ProcessDefinition processDefinition = deployProcess(readBusinessArchive);
        addUserToFirstActorOfProcess(user.getId(), processDefinition);

        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());
        getProcessAPI().enableProcess(processDefinition.getId());
        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void deployProcessWithUTF8Characteres() throws Exception {
        // Create process
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1_1"),
                Arrays.asList(false));
        final ProcessDefinition processDefinition1 = deployAndEnableProcess(designProcessDefinition1);

        assertEquals(APITestUtil.PROCESS_NAME, processDefinition1.getName());
        // delete data for test
        disableAndDeleteProcess(processDefinition1);
    }

    @Test
    public void deployProcessWithAutologin_should_respect_lifecycle() throws Exception {
        //given
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final BusinessArchiveBuilder newBusinessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        final byte[] properties = IOUtil
                .getAllContentFrom(this.getClass().getClassLoader().getResourceAsStream("org/bonitasoft/engine/process/security-config.properties"));
        newBusinessArchive.addExternalResource(new BarResource("forms/security-config.properties", properties));

        //when
        final ProcessDefinition processDefinition = deployProcess(
                newBusinessArchive.setProcessDefinition(designProcessDefinition).done());
        addUserToFirstActorOfProcess(user.getId(), processDefinition);
        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());

        //then
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());
        checkAutoLoginConfiguration("should have no auto login for resolved process", "[]");

        //when
        getProcessAPI().enableProcess(processDefinition.getId());
        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());

        //then
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        checkAutoLoginConfiguration("should have auto login for enabled process",
                "[{\"processname\":\"ProcessName\",\"processversion\":\"1.0\",\"username\":\"walter.bates\",\"password\":\"bpm\"}]");

        //when
        getProcessAPI().disableProcess(processDefinition.getId());
        checkAutoLoginConfiguration("should have no auto login for disabled process", "[]");

        // Clean up
        getProcessAPI().deleteProcessDefinition(processDefinition.getId());
    }

    @Test
    public void deployProcessWith_no_security_resource_should_not_allow_auto_login() throws Exception {
        // given
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1_1"),
                Arrays.asList(false));

        //when
        final ProcessDefinition processDefinition1 = deployAndEnableProcess(designProcessDefinition1);

        //then
        checkAutoLoginConfiguration("should have no auto login when process has no resources", "[]");

        // clean up
        disableAndDeleteProcess(processDefinition1);
    }

    private void checkAutoLoginConfiguration(String message, String expectedJson) throws Exception {
        final BonitaConfiguration configuration = getPortalAutoLoginConfiguration();
        assertThat(configuration).isNotNull();
        assertThat(new String(configuration.getResourceContent())).as(message).isEqualTo(expectedJson);
    }

    private BonitaConfiguration getPortalAutoLoginConfiguration() throws Exception {
        ConfigurationService configurationService = PlatformSetupAccessor.getConfigurationService();
        return configurationService.getTenantPortalConfiguration(getApiClient().getSession().getTenantId(), "autologin-v6.json");
    }

    @Test
    public void deployBigProcess() throws Exception {
        // Create process
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1_1"),
                Arrays.asList(false));
        final byte[] bigContent = new byte[1024 * 1024 * 5];
        new Random().nextBytes(bigContent);

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition1)
                .addClasspathResource(new BarResource("bigRessource", bigContent)).done();
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        getProcessAPI().enableProcess(processDefinition.getId());
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = AlreadyExistsException.class)
    public void deployProcess2Times() throws Exception {
        // First process def with 2 instances:
        final DesignProcessDefinition designProcessDef1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("initTask1"),
                Arrays.asList(true));
        final ProcessDefinition processDef1 = deployAndEnableProcessWithActor(designProcessDef1, APITestUtil.ACTOR_NAME, user);
        try {
            deployAndEnableProcessWithActor(designProcessDef1, APITestUtil.ACTOR_NAME, user);
        } finally {
            disableAndDeleteProcess(processDef1);
        }
    }

    @Test
    public void exportBusinessArchiveWithAllArtifacts() throws Exception {
        final User john = createUser("john", "bpm");
        final User jack = createUser("jack", "bpm");
        //create the process
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addConnector("connectorName", "theConnectorId", "theConnectorVersion", ConnectorEvent.ON_ENTER);
        processDefinitionBuilder.addActor("actor");
        processDefinitionBuilder.addUserTask("step1", "actor").addUserFilter("userFilterName", "theUserFilterId", "theUserFilterVersion");
        processDefinitionBuilder.addParameter("param1", String.class.getName());
        processDefinitionBuilder.addDocumentDefinition("myDoc").addContentFileName("myPdfModifiedName.pdf").addDescription("a cool pdf document")
                .addMimeType("application/pdf")
                .addFile("myPdf.pdf").addDescription("my description");
        businessArchiveBuilder.setProcessDefinition(processDefinitionBuilder.done());

        //create the business archive
        businessArchiveBuilder.setParameters(Collections.singletonMap("param1", "theValue"));
        final FormMappingModel formMappingModel = new FormMappingModel();
        formMappingModel.addFormMapping(new FormMappingDefinition("theUrl", FormMappingType.TASK, FormMappingTarget.URL, "step1"));
        businessArchiveBuilder.setFormMappings(formMappingModel);
        final Actor actor = new Actor("actor");
        actor.addUser("john");
        final ActorMapping actorMapping = new ActorMapping();
        actorMapping.addActor(actor);
        businessArchiveBuilder.setActorMapping(actorMapping);
        final byte[] connectorImplementationFile = BuildTestUtil.buildConnectorImplementationFile("theConnectorId", "theConnectorVersion", "impl1", "1.0",
                TestConnectorWithOutput.class.getName());
        businessArchiveBuilder.addConnectorImplementation(new BarResource("theConnctor.impl", connectorImplementationFile));
        final byte[] userFilterImpl = BuildTestUtil.buildConnectorImplementationFile("theConnectorId", "theConnectorVersion", "impl1", "1.0",
                TestConnectorWithOutput.class.getName());
        businessArchiveBuilder.addUserFilters(new BarResource("theUserFilter.impl", userFilterImpl));
        final byte[] pdfContent = new byte[] { 5, 0, 1, 4, 6, 5, 2, 3, 1, 5, 6, 8, 4, 6, 6, 3, 2, 4, 5 };
        businessArchiveBuilder.addDocumentResource(new BarResource("myPdf.pdf", pdfContent));
        businessArchiveBuilder.addClasspathResource(BuildTestUtil.generateJarAndBuildBarResource(ProcessAPI.class, "myJar,jar"));
        businessArchiveBuilder.addExternalResource(new BarResource("index.html", "<html>".getBytes()));
        businessArchiveBuilder.addExternalResource(new BarResource("content/other.html", "<html>1".getBytes()));

        //deploy
        final BusinessArchive businessArchive = businessArchiveBuilder.done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        assertThat(getProcessAPI().getProcessResolutionProblems(processDefinition.getId())).isEmpty();
        getProcessAPI().enableProcess(processDefinition.getId());

        //modify

        //export
        final byte[] bytes = getProcessAPI().exportBarProcessContentUnderHome(processDefinition.getId());
        final BusinessArchive exportedBAR = BusinessArchiveFactory.readBusinessArchive(new ByteArrayInputStream(bytes));

        //check
        assertThat(exportedBAR.getResources().keySet()).containsAll(businessArchive.getResources().keySet());
        assertThat(exportedBAR.getFormMappingModel().getFormMappings()).containsAll(businessArchive.getFormMappingModel().getFormMappings());
        assertThat(exportedBAR.getParameters()).isEqualTo(businessArchive.getParameters());
        assertThat(exportedBAR.getProcessDefinition()).isEqualTo(businessArchive.getProcessDefinition());

        deleteUsers(john, jack);
        disableAndDeleteProcess(processDefinition);
    }

}
