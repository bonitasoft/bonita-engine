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

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.Test;

/**
 * Test common use case of flow pattern in bpmn
 *
 * @author Baptiste Mesta
 */
public class FlowPatternsIT extends TestWithUser {

    @Test
    public void process_with_inclusive() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithInclusiveGateway();

        //all conditions are true
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        long step0 = waitForUserTask("Step0");
        getProcessAPI().updateProcessDataInstance("cond1", processInstance.getId(), true);
        getProcessAPI().updateProcessDataInstance("cond2", processInstance.getId(), true);
        getProcessAPI().updateProcessDataInstance("cond3", processInstance.getId(), true);
        getProcessAPI().updateProcessDataInstance("cond4", processInstance.getId(), true);
        assignAndExecuteStep(step0, user);
        waitForUserTaskAndExecuteIt(processInstance, "Step1", user);
        waitForUserTaskAndExecuteIt(processInstance, "Step2", user);
        waitForUserTaskAndExecuteIt(processInstance, "Step3", user);
        waitForUserTaskAndExecuteIt(processInstance, "Step4", user);
        waitForUserTaskAndExecuteIt(processInstance, "Step6", user);
        waitForProcessToFinish(processInstance.getId());
        //all conditions are false
        processInstance = getProcessAPI().startProcess(processDefinition.getId());
        step0 = waitForUserTask("Step0");
        getProcessAPI().updateProcessDataInstance("cond1", processInstance.getId(), false);
        getProcessAPI().updateProcessDataInstance("cond2", processInstance.getId(), false);
        getProcessAPI().updateProcessDataInstance("cond3", processInstance.getId(), false);
        getProcessAPI().updateProcessDataInstance("cond4", processInstance.getId(), false);
        assignAndExecuteStep(step0, user);
        waitForUserTaskAndExecuteIt(processInstance, "Step5", user);
        waitForUserTaskAndExecuteIt(processInstance, "Step6", user);
        waitForProcessToFinish(processInstance.getId());
        //some conditions are false
        processInstance = getProcessAPI().startProcess(processDefinition.getId());
        step0 = waitForUserTask("Step0");
        getProcessAPI().updateProcessDataInstance("cond1", processInstance.getId(), false);
        getProcessAPI().updateProcessDataInstance("cond2", processInstance.getId(), true);
        getProcessAPI().updateProcessDataInstance("cond3", processInstance.getId(), false);
        getProcessAPI().updateProcessDataInstance("cond4", processInstance.getId(), true);
        assignAndExecuteStep(step0, user);
        waitForUserTaskAndExecuteIt(processInstance, "Step2", user);
        waitForUserTaskAndExecuteIt(processInstance, "Step4", user);
        waitForUserTaskAndExecuteIt(processInstance, "Step6", user);
        waitForProcessToFinish(processInstance.getId());

        disableAndDeleteProcess(processDefinition);
    }

    ProcessDefinition deployProcessWithInclusiveGateway() throws BonitaException {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("InclusiveProcess", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addBooleanData("cond1", new ExpressionBuilder().createConstantBooleanExpression(true));
        builder.addBooleanData("cond2", new ExpressionBuilder().createConstantBooleanExpression(true));
        builder.addBooleanData("cond3", new ExpressionBuilder().createConstantBooleanExpression(true));
        builder.addBooleanData("cond4", new ExpressionBuilder().createConstantBooleanExpression(true));
        builder.addStartEvent("Start");
        builder.addUserTask("Step0", ACTOR_NAME);
        builder.addUserTask("Step1", ACTOR_NAME);
        builder.addUserTask("Step2", ACTOR_NAME);
        builder.addUserTask("Step3", ACTOR_NAME);
        builder.addUserTask("Step4", ACTOR_NAME);
        builder.addUserTask("Step5", ACTOR_NAME);
        builder.addGateway("Gateway1", GatewayType.INCLUSIVE);
        builder.addGateway("Gateway2", GatewayType.INCLUSIVE);
        builder.addUserTask("Step6", ACTOR_NAME);
        builder.addEndEvent("End");
        builder.addTransition("Start", "Step0");
        builder.addTransition("Step0", "Gateway1");
        builder.addTransition("Gateway1", "Step1", new ExpressionBuilder().createDataExpression("cond1", Boolean.class.getName()));
        builder.addTransition("Gateway1", "Step2", new ExpressionBuilder().createDataExpression("cond2", Boolean.class.getName()));
        builder.addTransition("Gateway1", "Step3", new ExpressionBuilder().createDataExpression("cond3", Boolean.class.getName()));
        builder.addTransition("Gateway1", "Step4", new ExpressionBuilder().createDataExpression("cond4", Boolean.class.getName()));
        builder.addDefaultTransition("Gateway1", "Step5");
        builder.addTransition("Step1", "Gateway2");
        builder.addTransition("Step2", "Gateway2");
        builder.addTransition("Step3", "Gateway2");
        builder.addTransition("Step4", "Gateway2");
        builder.addTransition("Step5", "Gateway2");
        builder.addTransition("Gateway2", "Step6");
        builder.addTransition("Step6", "End");
        final DesignProcessDefinition designProcessDefinition = builder.getProcess();
        return deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
    }

    @Test
    public void process_that_merge_different_branches() throws Exception {
        /*
         * step 1 and 2 are merged into step 4 by gateway 2 and step 3 and step4 are merged by gateway3
         */
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MultipleMergeProcess", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("Start");
        builder.addUserTask("Step1", ACTOR_NAME);
        builder.addUserTask("Step2", ACTOR_NAME);
        builder.addUserTask("Step3", ACTOR_NAME);
        builder.addUserTask("Step4", ACTOR_NAME);
        builder.addUserTask("Step5", ACTOR_NAME);
        builder.addGateway("Gateway1", GatewayType.PARALLEL);
        builder.addGateway("Gateway2", GatewayType.PARALLEL);
        builder.addGateway("Gateway3", GatewayType.INCLUSIVE);
        builder.addEndEvent("End");
        builder.addTransition("Start", "Gateway1");
        builder.addTransition("Gateway1", "Step1");
        builder.addTransition("Gateway1", "Step2");
        builder.addTransition("Gateway1", "Step3");
        builder.addTransition("Step1", "Gateway2");
        builder.addTransition("Step2", "Gateway2");
        builder.addTransition("Gateway2", "Step4");
        builder.addTransition("Step3", "Gateway3");
        builder.addTransition("Step4", "Gateway3");
        builder.addTransition("Gateway3", "Step5");
        builder.addTransition("Step5", "End");
        final DesignProcessDefinition designProcessDefinition = builder.getProcess();
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTaskAndExecuteIt("Step1",user);
        waitForUserTaskAndExecuteIt("Step2",user);
        waitForUserTaskAndExecuteIt("Step4",user);
        waitForUserTaskAndExecuteIt("Step3",user);
        waitForUserTaskAndExecuteIt("Step5",user);
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);


    }


    @Test
    public void process_with_branch_out() throws Exception {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MultipleMergeProcess", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("Start");
        builder.addUserTask("Step1", ACTOR_NAME);
        builder.addUserTask("Step2", ACTOR_NAME);
        builder.addUserTask("Step3", ACTOR_NAME);
        builder.addUserTask("Step4", ACTOR_NAME);
        builder.addGateway("Gateway1", GatewayType.PARALLEL);
        builder.addGateway("Gateway2", GatewayType.PARALLEL);
        builder.addEndEvent("End1");
        builder.addEndEvent("End2");
        builder.addTransition("Start", "Gateway1");
        builder.addTransition("Gateway1", "Step1");
        builder.addTransition("Gateway1", "Step2");
        builder.addTransition("Step1", "Gateway2");
        builder.addTransition("Step2", "Gateway2");
        builder.addTransition("Step2", "Step3");
        builder.addTransition("Gateway2", "Step4");
        builder.addTransition("Step4", "End1");
        builder.addTransition("Step3", "End2");
        final DesignProcessDefinition designProcessDefinition = builder.getProcess();
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTaskAndExecuteIt("Step2", user);
        waitForUserTaskAndExecuteIt("Step1",user);
        waitForUserTaskAndExecuteIt("Step4",user);
        waitForUserTaskAndExecuteIt("Step3",user);
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);

    }


    @Test
    public void inclusive_merge_with_no_start() throws Exception {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MultipleMergeProcess", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("Step1", ACTOR_NAME);
        builder.addUserTask("Step2", ACTOR_NAME);
        builder.addUserTask("Step3", ACTOR_NAME);
        builder.addGateway("Gateway1", GatewayType.INCLUSIVE);
        builder.addTransition("Step1", "Gateway1");
        builder.addTransition("Step2", "Gateway1");
        builder.addTransition("Gateway1", "Step3");
        final DesignProcessDefinition designProcessDefinition = builder.getProcess();
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTaskAndExecuteIt("Step1",user);
        waitForUserTaskAndExecuteIt("Step2",user);
        waitForUserTaskAndExecuteIt("Step3",user);
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);

    }

}
