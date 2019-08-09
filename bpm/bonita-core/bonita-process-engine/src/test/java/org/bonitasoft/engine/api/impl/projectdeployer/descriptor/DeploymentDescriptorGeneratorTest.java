/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.impl.projectdeployer.descriptor;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.bonitasoft.engine.io.FileOperations.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.assertj.core.api.JUnitSoftAssertions;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Application;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Layout;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Process;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Profile;
import org.bonitasoft.engine.api.impl.projectdeployer.model.RestAPIExtension;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Theme;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DeploymentDescriptorGeneratorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File applicationDirectory;

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void setup() throws IOException {
        applicationDirectory = temporaryFolder.newFolder("applicationBaseDirectory");
    }

    @Test
    public void should_generate_deploy_descriptor_when_all_resources_are_in_the_same_directory() throws Exception {
        // given:
        DeploymentDescriptorGenerator deploymentDescriptorGenerator = DeploymentDescriptorGenerator.create();

        writeFile("SingleApp.xml", resource("/application.xml"));
        writeFile("bdm.zip", resource("/bdm.zip"));
        writeFile("Organization_Data.xml", resource("/Organization_Data.xml"));
        writeFile("page-example.zip", resource("/page.zip"));
        writeFile("Profile_Data.xml", resource("/Profile_Data.xml"));
        writeFile("CreateAndUpdateData--1.0.bar", resource("/CreateAndUpdateData--1.0.bar"));
        writeFile("RestAPI-1.0.0.zip", resource("/RestAPI-1.0.0.zip"));
        writeFile("layout.zip", resource("/layout.zip"));
        writeFile("custom-theme.zip", resource("/custom-theme.zip"));
        writeFile("bdm.zip", resource("/bdm.zip"));
        writeFile("bdm_access_control.xml", resource("/bdm_access_control.xml"));

        // when:
        DeploymentDescriptor deploymentDescriptor = deploymentDescriptorGenerator.fromDirectory(applicationDirectory);

        softly.assertThat(deploymentDescriptor.getApplications()).extracting(Application::getFile)
                .allMatch(path -> path.endsWith("SingleApp.xml"));
        softly.assertThat(deploymentDescriptor.getOrganization().getFile()).endsWith("Organization_Data.xml");
        softly.assertThat(deploymentDescriptor.getProfiles()).extracting(Profile::getFile)
                .allMatch(path -> path.endsWith("Profile_Data.xml"));
        softly.assertThat(deploymentDescriptor.getProcesses()).extracting(Process::getFile)
                .allMatch(path -> path.endsWith("CreateAndUpdateData--1.0.bar"));
        softly.assertThat(deploymentDescriptor.getRestAPIExtensions()).extracting(RestAPIExtension::getFile)
                .allMatch(path -> path.endsWith("RestAPI-1.0.0.zip"));
        softly.assertThat(deploymentDescriptor.getLayouts()).extracting(Layout::getFile)
                .allMatch(path -> path.endsWith("layout.zip"));
        softly.assertThat(deploymentDescriptor.getThemes()).extracting(Theme::getFile)
                .allMatch(path -> path.endsWith("custom-theme.zip"));
        softly.assertThat(deploymentDescriptor.getBusinessDataModel().getFile()).endsWith("bdm.zip");
        softly.assertThat(deploymentDescriptor.getBdmAccessControl().getFile()).endsWith("bdm_access_control.xml");
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private File writeFile(String fileName, InputStream content) throws IOException {
        File file = new File(applicationDirectory, fileName);
        Files.copy(content, file.toPath(), REPLACE_EXISTING);
        return file;
    }

}
