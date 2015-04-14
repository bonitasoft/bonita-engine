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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Frédéric Bouquet
 * @author Céline Souchet
 */
public class ProcessDeploymentIT extends TestWithUser {

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
        final File tempFile = IOUtil.createTempFile("testbar", ".bar", new File(IOUtil.TMP_DIRECTORY));
        tempFile.delete();
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
        tempFile.delete();
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

    @Cover(classes = { ProcessAPI.class, ProcessDefinition.class }, concept = BPMNConcept.PROCESS, keywords = { "ProcessDefinition", "deploy" }, jira = "ENGINE-655", exceptions = { AlreadyExistsException.class })
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

}
