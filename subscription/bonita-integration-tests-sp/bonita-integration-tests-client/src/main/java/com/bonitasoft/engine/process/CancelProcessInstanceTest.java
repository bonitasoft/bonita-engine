/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.process;

import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.identity.User;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class CancelProcessInstanceTest extends InterruptProcessInstanceTest {

    @Test
    public void cancelParallelMergeGatewayIntance() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process_with_parallel_gateway", "1.0");
        processDefinitionBuilder.addAutomaticTask("step1");
        processDefinitionBuilder.addAutomaticTask("step2");
        processDefinitionBuilder.addUserTask("step3", "actor");
        processDefinitionBuilder.addAutomaticTask("step4");
        processDefinitionBuilder.addGateway("gateway1", GatewayType.PARALLEL);
        processDefinitionBuilder.addGateway("gateway2", GatewayType.PARALLEL);
        processDefinitionBuilder.addTransition("step1", "gateway1");
        processDefinitionBuilder.addTransition("gateway1", "step2");
        processDefinitionBuilder.addTransition("gateway1", "step3");
        processDefinitionBuilder.addTransition("step2", "gateway2");
        processDefinitionBuilder.addTransition("step3", "gateway2");
        processDefinitionBuilder.addTransition("gateway2", "step4");
        processDefinitionBuilder.addActor("actor");
        final User user = createUser("john", "bpm");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), "actor", user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final FlowNodeInstance waitForFlowNode = waitForFlowNodeInExecutingState(processInstance, "gateway2", false);

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume break point
        getProcessAPI().executeFlowNode(waitForFlowNode.getId());
        waitForProcessToBeInState(processInstance, ProcessInstanceState.CANCELLED);

        // verify that the execution does not pass through the activity after the gateway
        checkWasntExecuted(processInstance, "step4");
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);

    }

}
