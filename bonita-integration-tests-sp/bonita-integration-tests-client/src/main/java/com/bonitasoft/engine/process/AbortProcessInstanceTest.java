/**
 * ****************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 * *****************************************************************************
 */
package com.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.TestStates;
import org.junit.Test;

public class AbortProcessInstanceTest extends InterruptProcessInstanceTest {

    @Test
    public void abortProcessWithGateways() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process_with_parallel_gateway", "1.0");
        processDefinitionBuilder.addUserTask("step3", "actor");
        processDefinitionBuilder.addActor("actor");
        final User user = createUser("john", "bpm");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("RemainingInstancesAreAbortedAfterCompletionCondition", "1.0");
        builder.addCallActivity("callActivity", constant("My_Process_with_parallel_gateway"), constant("1.0"))
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(2))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("deployProcessWithMultiInstanceCallActivity",
                                "numberOfCompletedInstances == 1 ", Boolean.class.getName(),
                                new ExpressionBuilder().createEngineConstant(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));

        final ProcessDefinition targetProcess = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), "actor", user);
        final ProcessDefinition parentProcess = deployAndEnableProcess(builder.done());


        final ProcessInstance parentProcessInstance = getProcessAPI().startProcess(parentProcess.getId());
        final HumanTaskInstance step3a = waitForUserTaskAndGetIt("step3");
        final HumanTaskInstance step3b = waitForUserTaskAndGetIt("step3");
        Long targetProcInstToBeAborted = step3b.getParentProcessInstanceId();
        getProcessAPI().assignUserTask(step3a.getId(), user.getId());
        getProcessAPI().executeFlowNode(step3a.getId());
        // the target process instances that exceed the max loop must be in aborted state
        waitForProcessToBeInState(targetProcInstToBeAborted, ProcessInstanceState.ABORTED);


        final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        searchBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.ORIGINAL_FLOW_NODE_ID, step3b.getId());
        searchBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, TestStates.ABORTED.getStateName());
        final SearchResult<ArchivedFlowNodeInstance> searchActivities = getProcessAPI().searchArchivedFlowNodeInstances(searchBuilder.done());
        assertEquals(1, searchActivities.getCount());
        waitForProcessToFinish(parentProcessInstance);
        disableAndDeleteProcess(parentProcess);
        disableAndDeleteProcess(targetProcess);
        deleteUser(user);
    }

    Expression constant(String name) throws InvalidExpressionException {
        return new ExpressionBuilder().createConstantStringExpression(name);
    }

}
