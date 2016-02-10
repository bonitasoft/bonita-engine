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
package org.bonitasoft.engine.process.instance;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;

public abstract class AbstractProcessInstanceIT extends TestWithUser {

    protected ProcessDefinition deployProcessWith2UserTasksAnd1AutoTask(final String taskName1, final String taskName2, final String autoTaskName)
            throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask(taskName1, ACTOR_NAME);
        processDefinitionBuilder.addUserTask(taskName2, ACTOR_NAME);
        processDefinitionBuilder.addAutomaticTask(autoTaskName);
        processDefinitionBuilder.addEndEvent("end1");
        processDefinitionBuilder.addEndEvent("end2");
        processDefinitionBuilder.addTransition("start", taskName1);
        processDefinitionBuilder.addTransition(taskName1, autoTaskName);
        processDefinitionBuilder.addTransition(autoTaskName, "end1");
        processDefinitionBuilder.addTransition("start", taskName2);
        processDefinitionBuilder.addTransition(taskName2, "end2");

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    protected ProcessDefinition deployProcessWith2AutomaticTasks(final String taskName1, final String taskName2) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask(taskName1);
        processDefinitionBuilder.addAutomaticTask(taskName2);
        processDefinitionBuilder.addEndEvent("end1");
        processDefinitionBuilder.addEndEvent("end2");
        processDefinitionBuilder.addTransition("start", taskName1);
        processDefinitionBuilder.addTransition(taskName1, "end1");
        processDefinitionBuilder.addTransition("start", taskName2);
        processDefinitionBuilder.addTransition(taskName2, "end2");

        return deployAndEnableProcess(processDefinitionBuilder.done());
    }

    protected ProcessDefinition deployProcessWithCallActivity(final String taskName1, final String callActivityName, final String targetProcess,
            final String targetVersion, final String taskName2) throws BonitaException {
        final Expression targetProcessExpr = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final Expression targetVersionExpr = new ExpressionBuilder().createConstantStringExpression(targetVersion);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("Process with call activity", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask(taskName1);
        processDefinitionBuilder.addCallActivity(callActivityName, targetProcessExpr, targetVersionExpr);
        processDefinitionBuilder.addAutomaticTask(taskName2);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskName1);
        processDefinitionBuilder.addTransition(taskName1, callActivityName);
        processDefinitionBuilder.addTransition(callActivityName, taskName2);
        processDefinitionBuilder.addTransition(taskName2, "end");

        return deployAndEnableProcess(processDefinitionBuilder.done());
    }

    protected ProcessDefinition deployProcessWithIntermediateThrowMessageEvent(final String eventName, final String messageName,
            final String targetProcessName, final String targetFlowNodeName) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask("auto1");
        // create expression for target process/flowNode
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNodeName);
        processDefinitionBuilder.addIntermediateThrowEvent(eventName).addMessageEventTrigger(messageName, targetProcessExpression, targetFlowNodeExpression);
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "auto1");
        processDefinitionBuilder.addTransition("auto1", eventName);
        processDefinitionBuilder.addTransition(eventName, "auto2");
        processDefinitionBuilder.addTransition("auto2", "end");

        return deployAndEnableProcess(processDefinitionBuilder.done());
    }

    protected ProcessDefinition deployProcessWithIntermediateThrowSignalEvent(final String eventName, final String signalName) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask("auto1");
        processDefinitionBuilder.addIntermediateThrowEvent(eventName).addSignalEventTrigger(signalName);
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "auto1");
        processDefinitionBuilder.addTransition("auto1", eventName);
        processDefinitionBuilder.addTransition(eventName, "auto2");
        processDefinitionBuilder.addTransition("auto2", "end");

        return deployAndEnableProcess(processDefinitionBuilder.done());
    }

    protected ProcessDefinition deployProcessWithIntermediateCatchMessageEvent(final String eventName, final String messageName, final String previousStep,
            final String nextStep) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask(previousStep);
        processDefinitionBuilder.addIntermediateCatchEvent(eventName).addMessageEventTrigger(messageName);
        processDefinitionBuilder.addAutomaticTask(nextStep);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", previousStep);
        processDefinitionBuilder.addTransition(previousStep, eventName);
        processDefinitionBuilder.addTransition(eventName, nextStep);
        processDefinitionBuilder.addTransition(nextStep, "end");

        return deployAndEnableProcess(processDefinitionBuilder.done());
    }

    protected ProcessDefinition deployProcessWithIntermediateCatchSignalEvent(final String eventName, final String signalName, final String previousStep,
            final String nextStep) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask("auto1");
        processDefinitionBuilder.addIntermediateCatchEvent(eventName).addSignalEventTrigger(signalName);
        processDefinitionBuilder.addAutomaticTask(nextStep);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", previousStep);
        processDefinitionBuilder.addTransition(previousStep, eventName);
        processDefinitionBuilder.addTransition(eventName, nextStep);
        processDefinitionBuilder.addTransition(nextStep, "end");

        return deployAndEnableProcess(processDefinitionBuilder.done());
    }

    protected ProcessDefinition deployProcessWithParallelGateways() throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process_with_parallel_gateway", "1.0");
        processDefinitionBuilder.addAutomaticTask("step1");
        processDefinitionBuilder.addAutomaticTask("step2");
        processDefinitionBuilder.addAutomaticTask("step3");
        processDefinitionBuilder.addAutomaticTask("step4");
        processDefinitionBuilder.addGateway("gateway1", GatewayType.PARALLEL);
        processDefinitionBuilder.addGateway("gateway2", GatewayType.PARALLEL);
        processDefinitionBuilder.addTransition("step1", "gateway1");
        processDefinitionBuilder.addTransition("gateway1", "step2");
        processDefinitionBuilder.addTransition("gateway1", "step3");
        processDefinitionBuilder.addTransition("step2", "gateway2");
        processDefinitionBuilder.addTransition("step3", "gateway2");
        processDefinitionBuilder.addTransition("gateway2", "step4");

        return deployAndEnableProcess(processDefinitionBuilder.done());
    }

    protected ProcessDefinition deployProcessWithExclusiveSplitGateway() throws BonitaException {
        final Expression trueExpression = new ExpressionBuilder().createConstantBooleanExpression(true);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process_with_parallel_gateway", "1.0");
        processDefinitionBuilder.addAutomaticTask("step1");
        processDefinitionBuilder.addAutomaticTask("step2");
        processDefinitionBuilder.addAutomaticTask("step3");
        processDefinitionBuilder.addAutomaticTask("step4");
        processDefinitionBuilder.addGateway("gateway1", GatewayType.EXCLUSIVE);
        processDefinitionBuilder.addTransition("step1", "gateway1");
        processDefinitionBuilder.addTransition("gateway1", "step2", trueExpression);
        processDefinitionBuilder.addTransition("gateway1", "step3", trueExpression);
        processDefinitionBuilder.addDefaultTransition("gateway1", "step4");

        return deployAndEnableProcess(processDefinitionBuilder.done());
    }

}
