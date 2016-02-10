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
package org.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class StartEventIT extends TestWithUser {

    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Start" }, story = "Execute process with several start event.", jira = "ENGINE-1592, ENGINE-1593")
    @Test
    public void executeSeveralStartEventsInSameProcessDefinition() throws Exception {
        final int timerValue = 10000;
        final Expression timerExpression = new ExpressionBuilder().createConstantLongExpression(timerValue);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("startEvent").addUserTask("step1", ACTOR_NAME).addTransition("startEvent", "step1");
        processDefinitionBuilder.addStartEvent("startEventWithSignal").addSignalEventTrigger("signalName").addUserTask("step1WithSignal", ACTOR_NAME)
                .addTransition("startEventWithSignal", "step1WithSignal");
        processDefinitionBuilder.addStartEvent("startEventWithTimer").addTimerEventTriggerDefinition(TimerType.DURATION, timerExpression)
                .addUserTask("step1WithTimer", ACTOR_NAME).addTransition("startEventWithTimer", "step1WithTimer");
        processDefinitionBuilder.addStartEvent("startEventWithMessage").addMessageEventTrigger("message").addUserTask("step1WithMessage", ACTOR_NAME)
                .addTransition("startEventWithMessage", "step1WithMessage");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.getProcess(), ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance step1 = waitForUserTaskAndGetIt("step1");

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10)
                .sort(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, Order.ASC);
        List<HumanTaskInstance> humanTaskInstances = getProcessAPI().searchPendingTasksForUser(user.getId(), searchOptionsBuilder.done()).getResult();
        assertNotNull("searchPendingTasksForUser give a null result for userId:" + user.getId() + " search options:" + searchOptionsBuilder.done(),
                humanTaskInstances);

        // timerValue is slower than startProcess time
        // then we have 2 tasks
        assertTrue(humanTaskInstances.size() > 0);

        for (final HumanTaskInstance humanTaskInstance : humanTaskInstances) {
            if (humanTaskInstance.getRootContainerId() == step1.getRootContainerId()) {
                // even if step1WithTimer is fired
                // the start event should be the first one
                assertEquals("step1", humanTaskInstance.getName());
            }
        }

        // Verify that the start without trigger, and the start with a timer are started
        // wait for process instance creation
        waitForUserTask("step1WithTimer");
        humanTaskInstances = getProcessAPI().searchPendingTasksForUser(user.getId(), searchOptionsBuilder.done()).getResult();
        assertEquals(2, humanTaskInstances.size());
        assertEquals("step1", humanTaskInstances.get(0).getName());
        assertEquals("step1WithTimer", humanTaskInstances.get(1).getName());
        final ProcessInstance processInstanceWithTimer = getProcessAPI().getProcessInstance(humanTaskInstances.get(1).getRootContainerId());
        assertEquals(0, processInstanceWithTimer.getStartedBy());
        assertNotEquals(startProcess.getId(), processInstanceWithTimer.getId());

        // Verify that the start without trigger, the start with a timer, and the start with signal are started
        getProcessAPI().sendSignal("signalName");
        waitForUserTask("step1WithSignal");

        humanTaskInstances = getProcessAPI().searchPendingTasksForUser(user.getId(), searchOptionsBuilder.done()).getResult();
        assertEquals(3, humanTaskInstances.size());
        assertEquals("step1", humanTaskInstances.get(0).getName());
        assertEquals("step1WithTimer", humanTaskInstances.get(1).getName());
        assertEquals("step1WithSignal", humanTaskInstances.get(2).getName());
        final ProcessInstance processInstanceWithSignal = getProcessAPI().getProcessInstance(humanTaskInstances.get(2).getRootContainerId());
        assertEquals(0, processInstanceWithSignal.getStartedBy());
        assertNotEquals(processInstanceWithTimer.getId(), processInstanceWithSignal.getId());

        // Verify all start are started
        getProcessAPI().sendMessage("message", new ExpressionBuilder().createConstantStringExpression(PROCESS_NAME),
                new ExpressionBuilder().createConstantStringExpression("startEventWithMessage"), null);
        waitForUserTask("step1WithMessage");
        humanTaskInstances = getProcessAPI().searchPendingTasksForUser(user.getId(), searchOptionsBuilder.done()).getResult();
        assertEquals(4, humanTaskInstances.size());
        assertEquals("step1", humanTaskInstances.get(0).getName());
        assertEquals("step1WithTimer", humanTaskInstances.get(1).getName());
        assertEquals("step1WithSignal", humanTaskInstances.get(2).getName());
        assertEquals("step1WithMessage", humanTaskInstances.get(3).getName());
        final ProcessInstance processInstanceWithMessage = getProcessAPI().getProcessInstance(humanTaskInstances.get(3).getRootContainerId());
        assertEquals(0, processInstanceWithMessage.getStartedBy());
        assertNotEquals(processInstanceWithSignal.getId(), processInstanceWithMessage.getId());

        disableAndDeleteProcess(processDefinition);
    }
}
