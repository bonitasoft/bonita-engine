/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package com.bonitasoft.engine.breakpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.core.operation.LeftOperandBuilder;
import org.bonitasoft.engine.core.operation.OperatorType;
import org.bonitasoft.engine.exception.ActivityInterruptedException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.wait.WaitProcessToFinishAndBeArchived;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.model.breakpoint.Breakpoint;
import com.bonitasoft.engine.bpm.model.breakpoint.BreakpointCriterion;
import com.bonitasoft.engine.exception.BreakpointNotFoundException;

/**
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class BreakpointsTest extends CommonAPISPTest {

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @Test
    public void testAddBreakpoint() throws Exception {
        final long breakpointId = getProcessAPI().addBreakpoint(123, "pouf", 2, 12);
        assertTrue(breakpointId > 0);
        getProcessAPI().removeBreakpoint(breakpointId);
    }

    @Test
    public void testGetBreakpoint() throws Exception {
        final long breakpointId = getProcessAPI().addBreakpoint(123, "pouf", 2, 12);
        final long breakpointId2 = getProcessAPI().addBreakpoint(124, "pouf2", 23, 12);
        assertTrue(breakpointId > 0);
        List<Breakpoint> breakpoints = getProcessAPI().getBreakpoints(0, 10, BreakpointCriterion.DEFINITION_ID_ASC);
        assertEquals(2, breakpoints.size());
        getProcessAPI().removeBreakpoint(breakpointId);
        getProcessAPI().removeBreakpoint(breakpointId2);
        breakpoints = getProcessAPI().getBreakpoints(0, 10, BreakpointCriterion.DEFINITION_ID_ASC);
        assertEquals(0, breakpoints.size());
    }

    @Test(expected = BreakpointNotFoundException.class)
    public void testRemoveBreakpointTwice() throws Exception {
        final long breakpointId = getProcessAPI().addBreakpoint(123, "pouf", 2, 12);
        assertTrue(breakpointId > 0);
        getProcessAPI().removeBreakpoint(breakpointId);
        getProcessAPI().removeBreakpoint(breakpointId);
    }

    @Test
    public void testAddBreakpointOnInstance() throws Exception {
        final long breakpointId = getProcessAPI().addBreakpoint(123, 456, "pouf", 2, 12);
        assertTrue(breakpointId > 0);
        getProcessAPI().removeBreakpoint(breakpointId);
    }

    @Test(expected = BreakpointNotFoundException.class)
    public void testRemoveUnexistingBreakpoint() throws Exception {
        getProcessAPI().removeBreakpoint(123);
    }

    @Test
    public void addBreakpointOnAProcessDefinition() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("BrokenProcess", "1.01",
                Arrays.asList("step1", "step2"), Arrays.asList(false, false));
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final long id = processDefinition.getId();
        ProcessInstance processInstance = getProcessAPI().startProcess(id);
        waitForProcessToFinish(processInstance);
        final long breakpointId = getProcessAPI().addBreakpoint(id, "step2", 2, 45);
        processInstance = getProcessAPI().startProcess(id);
        final WaitProcessToFinishAndBeArchived waitProcessToFinishAndBeArchived = new WaitProcessToFinishAndBeArchived(50, 1000, false, processInstance,
                getProcessAPI());
        assertFalse(waitProcessToFinishAndBeArchived.waitUntil());
        final Set<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(1, activities.size());
        final ActivityInstance activityInstance = activities.iterator().next();
        assertEquals("step2", activityInstance.getName());
        assertEquals("interrupted", activityInstance.getState());
        getProcessAPI().removeBreakpoint(breakpointId);
        disableAndDelete(processDefinition);
    }

    @Test
    public void addBreakpointOnAProcessDefinitionAndResumeIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("BrokenProcess", "1.02",
                Arrays.asList("step1", "step2"), Arrays.asList(false, false));
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final long id = processDefinition.getId();
        ProcessInstance processInstance = getProcessAPI().startProcess(id);
        waitForProcessToFinish(processInstance);
        final long breakpointId = getProcessAPI().addBreakpoint(id, "step2", 2, 45);
        processInstance = getProcessAPI().startProcess(id);
        final WaitProcessToFinishAndBeArchived waitProcessToFinishAndBeArchived = new WaitProcessToFinishAndBeArchived(50, 1000, false, processInstance,
                getProcessAPI());
        assertFalse(waitProcessToFinishAndBeArchived.waitUntil());
        final Set<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(1, activities.size());
        final ActivityInstance activityInstance = activities.iterator().next();
        assertEquals("step2", activityInstance.getName());
        assertEquals("interrupted", activityInstance.getState());
        getProcessAPI().executeActivity(activityInstance.getId());
        assertTrue(waitProcessToFinishAndBeArchived.waitUntil());
        getProcessAPI().removeBreakpoint(breakpointId);
        disableAndDelete(processDefinition);
    }

    @Test
    public void addBreakpointOnAProcessInstance() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("BrokenProcess", "1.03",
                Arrays.asList("step1", "step2"), Arrays.asList(true, false), "actor", true);
        final String username = "baptiste.mesta";
        final String password = "secretPassword";
        final User user = createUser(username, password);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, "actor", user);
        final long id = processDefinition.getId();
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(id);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(id);
        final long breakpointId = getProcessAPI().addBreakpoint(id, processInstance2.getId(), "step2", 2, 45);
        waitForUserTaskAndExecuteIt("step1", processInstance1, user.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance2, user.getId());
        waitForProcessToFinish(processInstance1);
        final WaitProcessToFinishAndBeArchived waitProcessToFinishAndBeArchived = new WaitProcessToFinishAndBeArchived(50, 5000, false, processInstance2,
                getProcessAPI());
        assertFalse(waitProcessToFinishAndBeArchived.waitUntil());
        final Set<ActivityInstance> activities = getProcessAPI().getActivities(processInstance2.getId(), 0, 10);
        assertEquals(1, activities.size());
        final ActivityInstance activityInstance = activities.iterator().next();
        assertEquals("step2", activityInstance.getName());
        assertEquals("interrupted", activityInstance.getState());
        getProcessAPI().removeBreakpoint(breakpointId);
        disableAndDelete(processDefinition);
        deleteUser(user.getId());
    }

    @Test
    public void addBreakpointOnHumanStepAndResumeIt() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("myProcessWithBreakPoints", "1.0");
        builder.addActor("actor");
        builder.addShortTextData("myData", new ExpressionBuilder().createConstantStringExpression("beforeUpdate"));
        final UserTaskDefinitionBuilder addUserTask = builder.addUserTask("step1", "actor");
        addUserTask.addOperation(new LeftOperandBuilder().createNewInstance("myData").done(), OperatorType.ASSIGNMENT, "=", null,
                new ExpressionBuilder().createConstantStringExpression("afterUpdate"));
        addUserTask.addBoundaryEvent("boundary").addSignalEventTrigger("unknown");
        builder.addUserTask("step2", "actor");
        builder.addStartEvent("start").addTransition("start", "step1").addTransition("step1", "step2").addTransition("boundary", "step2");
        final String username = "baptiste.mesta";
        final String password = "secretPassword";
        final User user = createUser(username, password);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), "actor", user);
        final long id = processDefinition.getId();
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(id);
        // add breakpoint on completing with boundary state (must have a boundary on the user task)
        final long addBreakpoint = getProcessAPI().addBreakpoint(id, "step1", 34, 45);
        try {
            waitForUserTaskAndExecuteIt("step1", processInstance1, user.getId());
        } catch (final ActivityInterruptedException e) {
            // ok it's interrupted
        }

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstance1.getId());
        searchOptionsBuilder.sort(ActivityInstanceSearchDescriptor.NAME, Order.ASC);
        final List<ActivityInstance> activities = getProcessAPI().searchActivities(searchOptionsBuilder.done()).getResult();
        assertEquals(1, activities.size());
        final ActivityInstance activityInstance = activities.iterator().next();
        assertEquals("step1", activityInstance.getName());
        assertEquals("interrupted", activityInstance.getState());
        assertEquals("afterUpdate", getProcessAPI().getProcessDataInstance("myData", processInstance1.getId()).getValue());
        getProcessAPI().executeActivity(activityInstance.getId());
        waitForUserTask("step2", processInstance1);
        getProcessAPI().removeBreakpoint(addBreakpoint);
        disableAndDelete(processDefinition);
        deleteUser(user.getId());
    }

}
