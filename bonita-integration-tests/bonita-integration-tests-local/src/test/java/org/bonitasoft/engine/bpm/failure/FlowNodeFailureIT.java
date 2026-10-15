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
import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.process.*;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.commons.exceptions.ScopedException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FlowNodeFailureIT extends TestWithUser {

    private ServiceAccessor serviceAccessor;
    private ProcessDefinition processDefinition;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        serviceAccessor = ServiceAccessorFactory.getInstance().createServiceAccessor();
        DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("My_Process_with_failed_flownode", PROCESS_VERSION)
                .addActor(ACTOR_NAME)
                .addAutomaticTask("step1")
                .addOperation(new LeftOperandBuilder().createDataLeftOperand("myData"),
                        OperatorType.ASSIGNMENT,
                        "",
                        new ExpressionBuilder()
                                .createGroovyScriptExpression("my-failing-script",
                                        "throw new RuntimeException('Failed !')", String.class.getName()))
                .getProcess();
        processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
    }

    @After
    public void after() throws Exception {
        disableAndDeleteProcess(processDefinition);
        super.after();
    }

    @Test
    public void create_a_failure_on_flownode_operation_exception() throws Exception {
        // Given a process failing on a flownode operation
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI()
                .getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // When the flownode is executed
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final FlowNodeInstance failFlowNodeInstance = waitForFlowNodeInFailedState(processInstance);

        // Then a failure is created
        assertEquals("step1", failFlowNodeInstance.getName());
        var failureService = ServiceAccessorFactory.getInstance().createServiceAccessor().getBpmFailureService();
        var failures = serviceAccessor.getTransactionService()
                .executeInTransaction(() -> failureService.getFlowNodeFailures(failFlowNodeInstance.getId(), 5));
        assertThat(failures).hasSize(1);
        var failure = failures.get(0);
        assertThat(failure.getScope())
                .isEqualTo(ScopedException.OPERATION);
        assertThat(failure.getContext())
                .isEqualTo("expression::my-failing-script");
        assertThat(failure.getErrorMessage())
                .isEqualTo("RuntimeException: Failed !");

        var processInstanceFailures = serviceAccessor.getTransactionService()
                .executeInTransaction(() -> failureService.getProcessInstanceFailures(processInstance.getId(), 5));
        assertThat(processInstanceFailures).hasSize(1);
        assertThat(processInstanceFailures.get(0))
                .isEqualTo(failures.get(0));
    }

}
