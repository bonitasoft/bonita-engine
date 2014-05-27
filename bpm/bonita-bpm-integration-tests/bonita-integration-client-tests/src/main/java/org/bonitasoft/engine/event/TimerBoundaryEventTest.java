/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.event;

import org.bonitasoft.engine.bpm.flownode.BoundaryEventInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class TimerBoundaryEventTest extends AbstractTimerBoundaryEventTest {
    
    
    protected ProcessDefinition deployProcessWithInterruptingAndNonInterruptingTimer(final long interruptTimer, final long nonInterruptingTimer,
            final String taskWithBoundaryName, final String interruptExceptionTaskName, final String nonInterruptExceptionTaskName, String interruptTimerName, String nonInterruptTimerName) throws BonitaException {
        String normalFlowTaskName = "normalFlow";
        Expression interruptTimerExpr = new ExpressionBuilder().createConstantLongExpression(interruptTimer);
        Expression nonInterruptTimerExpr = new ExpressionBuilder().createConstantLongExpression(nonInterruptingTimer);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pTimerBoundary", "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask(taskWithBoundaryName, ACTOR_NAME);
        userTaskDefinitionBuilder.addBoundaryEvent(interruptTimerName, true).addTimerEventTriggerDefinition(TimerType.DURATION, interruptTimerExpr);
        userTaskDefinitionBuilder.addBoundaryEvent(nonInterruptTimerName, false).addTimerEventTriggerDefinition(TimerType.DURATION, nonInterruptTimerExpr);
        processDefinitionBuilder.addUserTask(interruptExceptionTaskName, ACTOR_NAME);
        processDefinitionBuilder.addUserTask(nonInterruptExceptionTaskName, ACTOR_NAME);
        processDefinitionBuilder.addUserTask(normalFlowTaskName, ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundaryName);
        processDefinitionBuilder.addTransition(taskWithBoundaryName, normalFlowTaskName);
        processDefinitionBuilder.addTransition(interruptTimerName, interruptExceptionTaskName);
        processDefinitionBuilder.addTransition(nonInterruptTimerName, nonInterruptExceptionTaskName);

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, donaBenta);
    }
    
    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Timer",
    "interrupting", "Non-interrupting" }, story = "A non-interrupting timer event is not triggered if an interrupting timer event is triggered before in the same task.", 
    jira = "ENGINE-1731")
    @Test
    public void nonInterruptingNotTrigerAfterInterruptingTriggering() throws Exception {
        //deploy process
        String taskWithBoundaryName = "taskWithBoundary";
        int nonInterruptingTimer = 30 * 1000; // the timer will be aborted, so no problem if it's a hight value
        String interruptExceptionTaskName = "afterInterrupt";
        String nonInterruptExceptionTaskName = "afterNonInterrupt";
        String interruptTimerName = "interruptTimer";
        String nonInterruptTimerName = "nonInterruptTimer";
        ProcessDefinition processDefinition = deployProcessWithInterruptingAndNonInterruptingTimer(100, nonInterruptingTimer, taskWithBoundaryName, interruptExceptionTaskName, nonInterruptExceptionTaskName, interruptTimerName, nonInterruptTimerName);
        
        //start process and wait for task in aborted state
        ProcessInstance processInstance = getProcessAPI().startProcess(donaBenta.getId(), processDefinition.getId());
        waitForFlowNodeInState(processInstance, taskWithBoundaryName, TestStates.getAbortedState(), false);
        
        //verify that non-interrupting timer was aborted
        waitForFlowNodeInState(processInstance, nonInterruptTimerName, TestStates.getAbortedState(), false);
        
        //verify that exception flow was taken
        waitForUserTask(interruptExceptionTaskName);
        
        getProcessAPI().deleteProcessInstance(processInstance.getId());
        disableAndDeleteProcess(processDefinition);
    }
    
    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Timer",
            "interrupting", "Non-interrupting" }, story = "An interrupting timer event is triggered after that a non-interrupting timer event is triggered in the same task.", 
            jira = "ENGINE-1731")
    @Test
    public void interruptingTrigerAfterNonInterruptingTriggering() throws Exception {
        //deploy process
        String taskWithBoundaryName = "taskWithBoundary";
        String interruptExceptionTaskName = "afterInterrupt";
        String nonInterruptExceptionTaskName = "afterNonInterrupt";
        String interruptTimerName = "interruptTimer";
        String nonInterruptTimerName = "nonInterruptTimer";
        ProcessDefinition processDefinition = deployProcessWithInterruptingAndNonInterruptingTimer(1000, 100, taskWithBoundaryName, interruptExceptionTaskName, nonInterruptExceptionTaskName, interruptTimerName, nonInterruptTimerName);
        
        //start process and wait for task in ready state
        ProcessInstance processInstance = getProcessAPI().startProcess(donaBenta.getId(), processDefinition.getId());
        waitForUserTask(taskWithBoundaryName);
        
        //verify that exception flow was taken for non-interrupting event
        waitForUserTask(nonInterruptExceptionTaskName);

        //wait for aborted state
        waitForFlowNodeInState(processInstance, taskWithBoundaryName, TestStates.getAbortedState(), false);
        //verify that interrupting  event was triggered
        waitForUserTask(interruptExceptionTaskName);
        
        getProcessAPI().deleteProcessInstance(processInstance.getId());
        disableAndDeleteProcess(processDefinition);
    }

    
}
