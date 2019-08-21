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
package org.bonitasoft.engine.api.impl.application.deployer.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.bonitasoft.engine.api.impl.application.deployer.model.Application;
import org.bonitasoft.engine.api.impl.application.deployer.model.ConnectorImplementation;
import org.bonitasoft.engine.api.impl.application.deployer.model.Process;
import org.bonitasoft.engine.api.impl.application.deployer.model.RestAPIExtension;
import org.bonitasoft.engine.api.impl.application.deployer.policies.ApplicationImportPolicy;
import org.bonitasoft.engine.api.impl.application.deployer.policies.ProcessImportPolicy;
import org.junit.Test;

/**
 * @author Baptiste Mesta.
 */
public class DeploymentDescriptorReaderTest {

    private DeploymentDescriptorReader deploymentDescriptorReader = new DeploymentDescriptorReader();

    @Test
    public void should_complete_deploy_json_file_to_object() throws Exception {
        String json = read("/org/bonitasoft/engine/api/impl/application/deployer/model/descriptor.json");

        DeploymentDescriptor deploymentDescriptor = deploymentDescriptorReader.fromJson(json);

        assertThat(deploymentDescriptor.getName()).isEqualTo("LeaveRequest");
        assertThat(deploymentDescriptor.getVersion()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(deploymentDescriptor.getTargetVersion()).isEqualTo("7.2.0");
        assertThat(deploymentDescriptor.getModelVersion()).isEqualTo("0.1");

        List<Process> processes = deploymentDescriptor.getProcesses();
        assertThat(processes).hasSize(2);
        Process firstProcess = processes.get(0);
        assertThat(firstProcess.getFile())
                .isEqualTo("processes/New-Vacation-Request--1.0.bar");
        assertThat(firstProcess.getPolicy()).isEqualTo(ProcessImportPolicy.REPLACE_DUPLICATES);
        assertThat(firstProcess.getConfiguration().getActorMapping())
                .isEqualTo("processes/New-Vacation-Request_Mapping.xml");
        assertThat(firstProcess.getConfiguration().getParameters())
                .isEqualTo("processes/New-Vacation-Request.properties");

        List<ConnectorImplementation> connectorImplementations = firstProcess
                .getConfiguration().getConnectorImplementations();
        assertThat(connectorImplementations).hasSize(1);
        assertThat(connectorImplementations.get(0).getConnectorName()).isEqualTo("email");
        assertThat(connectorImplementations.get(0).getConnectorVersion()).isEqualTo("1.4");
        assertThat(connectorImplementations.get(0).getFile()).isEqualTo("processes/email-impl-2.0.zip");

        Process secondProcess = processes.get(1);
        assertThat(secondProcess.getFile()).isEqualTo("processes/Initiate-Vacation-Available--1.0.bar");
        assertThat(secondProcess.getPolicy()).isEqualTo(ProcessImportPolicy.FAIL_ON_DUPLICATES);

        RestAPIExtension restAPIExtensions = deploymentDescriptor.getRestAPIExtensions().get(0);
        assertThat(restAPIExtensions.getFile())
                .as("should contain REST API extension")
                .isEqualTo("restAPIExtensions/VacationRequestRestAPI-1.0.0-SNAPSHOT.zip");

        RestAPIExtension restAPIExtension = deploymentDescriptor.getRestAPIExtensions().get(1);
        assertThat(restAPIExtension.getFile())
                .as("should contain REST API extension")
                .isEqualTo("restAPIExtensions/CancelRequestRestAPI-1.0.0-SNAPSHOT.zip");

        assertThat(deploymentDescriptor.getPages().get(0).getFile())
                .isEqualTo("pages/page-ExampleVacationManagement.zip");

        Application firstApplication = deploymentDescriptor.getApplications().get(0);
        assertThat(firstApplication.getFile()).isEqualTo("tahiti/Application_Data.xml");
        assertThat(firstApplication.getPolicy()).isEqualTo(ApplicationImportPolicy.FAIL_ON_DUPLICATES);

    }

    public static String read(String resourceName) throws IOException {
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(DeploymentDescriptorReaderTest.class.getResourceAsStream(resourceName)))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}
