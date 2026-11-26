/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.bpm.failure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.*;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.commons.exceptions.ScopedException;
import org.bonitasoft.engine.connectors.TestConnectorThatThrowException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcessInstanceFailureIT extends TestWithUser {

    private ServiceAccessor serviceAccessor;
    private ProcessDefinition failingProcessDefinition;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        serviceAccessor = ServiceAccessorFactory.getInstance().createServiceAccessor();
        failingProcessDefinition = getFailingProcessDefinition("My_Process_with_failing_connector");
    }

    @After
    public void after() throws Exception {
        disableAndDeleteProcess(failingProcessDefinition);
        super.after();
    }

    private ProcessDefinition getFailingProcessDefinition(String processName) throws InvalidProcessDefinitionException,
            IOException, BonitaException, InvalidBusinessArchiveFormatException {
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance(processName, PROCESS_VERSION)
                .addActor(ACTOR_NAME)
                .addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0",
                        ConnectorEvent.ON_FINISH)
                .addAutomaticTask("step1")
                .getProcess();
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(
                        designProcessDefinition);
        businessArchiveBuilder.addConnectorImplementation(
                getResource("/org/bonitasoft/engine/connectors/TestConnectorThatThrowException.impl",
                        "TestConnectorThatThrowException.impl"));
        businessArchiveBuilder
                .addClasspathResource(BuildTestUtil
                        .generateJarAndBuildBarResource(TestConnectorThatThrowException.class,
                                "TestConnectorThatThrowException.jar"));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchiveBuilder.done(),
                ACTOR_NAME,
                user);
        return processDefinition;
    }

    @Test
    public void create_a_failure_on_process_connector_exception() throws Exception {
        // Given a process failing on a flownode operation
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI()
                .getProcessDeploymentInfo(failingProcessDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ERROR);

        // Then a failure is created
        var failureService = ServiceAccessorFactory.getInstance().createServiceAccessor().getBpmFailureService();
        var failures = serviceAccessor.getTransactionService()
                .executeInTransaction(() -> failureService.getProcessInstanceFailures(processInstance.getId(), 5));
        assertThat(failures).hasSize(1);
        var failure = failures.get(0);
        assertThat(failure.getScope())
                .isEqualTo(ScopedException.CONNECTOR);
        assertThat(failure.getContext())
                .isEqualTo(
                        "testConnectorThatThrowException::testConnectorThatThrowException::on_finish//input-validation");
        assertThat(failure.getErrorMessage())
                .isEqualTo("ConnectorValidationException: bad kind of exception");

        //There are no subprocess so no subprocess failure should be retrieved
        var subProcessInstanceFailures = serviceAccessor.getTransactionService()
                .executeInTransaction(
                        () -> failureService.getChildProcessInstancesFailures(processInstance.getId(), 5));
        assertThat(subProcessInstanceFailures).hasSize(0);
    }

    @Test
    public void create_a_failure_on_sub_process_connector_exception() throws Exception {
        // Given a process failing on a flownode operation
        final DesignProcessDefinition mainProcessDesign = new ProcessDefinitionBuilder()
                .createNewInstance("My_Process_with_call_activity", PROCESS_VERSION)
                .addActor(ACTOR_NAME)
                .addCallActivity("call activity",
                        new ExpressionBuilder().createConstantStringExpression(failingProcessDefinition.getName()),
                        new ExpressionBuilder().createConstantStringExpression(PROCESS_VERSION))
                .getProcess();
        final ProcessDefinition mainProcessDefinition = deployAndEnableProcessWithActor(mainProcessDesign, ACTOR_NAME,
                user);
        try {
            final ProcessDeploymentInfo mainProcessDeploymentInfo = getProcessAPI()
                    .getProcessDeploymentInfo(mainProcessDefinition.getId());
            assertEquals(ActivationState.ENABLED, mainProcessDeploymentInfo.getActivationState());

            final ProcessInstance processInstance = getProcessAPI()
                    .startProcess(mainProcessDeploymentInfo.getProcessId());
            waitForProcessToBeInState(processInstance, ProcessInstanceState.STARTED);

            SearchOptions SearchOption = new SearchOptionsBuilder(0, 1)
                    .filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, failingProcessDefinition.getId())
                    .done();
            await().atMost(3, TimeUnit.SECONDS)
                    .until(() -> !getProcessAPI().searchProcessInstances(SearchOption).getResult().isEmpty());
            ProcessInstance subProcessInstance = getProcessAPI().searchProcessInstances(SearchOption).getResult()
                    .get(0);
            waitForProcessToBeInState(subProcessInstance, ProcessInstanceState.ERROR);

            // Then a failure is created in the subprocess
            var failureService = ServiceAccessorFactory.getInstance().createServiceAccessor().getBpmFailureService();
            var subProcessInstanceFailures = serviceAccessor.getTransactionService()
                    .executeInTransaction(
                            () -> failureService.getChildProcessInstancesFailures(processInstance.getId(), 5));
            assertThat(subProcessInstanceFailures).hasSize(1);
            var failure = subProcessInstanceFailures.get(0);
            assertThat(failure.getScope())
                    .isEqualTo(ScopedException.CONNECTOR);
            assertThat(failure.getContext())
                    .isEqualTo(
                            "testConnectorThatThrowException::testConnectorThatThrowException::on_finish//input-validation");
            assertThat(failure.getErrorMessage())
                    .isEqualTo("ConnectorValidationException: bad kind of exception");

            //No failure as root process instance level
            var mainProcessInstanceFailures = serviceAccessor.getTransactionService()
                    .executeInTransaction(() -> failureService.getProcessInstanceFailures(processInstance.getId(), 5));
            assertThat(mainProcessInstanceFailures).hasSize(0);

            //cancel process instance to archive it
            getProcessAPI().cancelProcessInstance(processInstance.getId());
            waitForProcessToBeInState(processInstance, ProcessInstanceState.CANCELLED);

            // Now let's check the archived version, for sub-process...
            var archSubProcessInstanceFailures = serviceAccessor.getTransactionService()
                    .executeInTransaction(
                            () -> failureService.getArchivedChildProcessInstancesFailures(processInstance.getId(), 5));
            assertThat(archSubProcessInstanceFailures).hasSize(1);

            // ... and for the main process:
            var archMainProcessInstanceFailures = serviceAccessor.getTransactionService()
                    .executeInTransaction(
                            () -> failureService.getArchivedProcessInstanceFailures(processInstance.getId(), 5));
            assertThat(archMainProcessInstanceFailures).hasSize(0);

        } finally {
            disableAndDeleteProcess(mainProcessDefinition);
        }
    }

}
