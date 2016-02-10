/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.TestStates;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

@SuppressWarnings("javadoc")
public class SPTimerBoundaryEventTest extends CommonAPISPIT {

    private User donaBenta;

    @Before
    public void beforeTest() throws BonitaException {
        BPMTestSPUtil.refreshDefaultTenantId();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        donaBenta = createUser("donabenta", "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith("donabenta", "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        deleteUser(donaBenta);
        logoutOnTenant();
    }

    private ProcessDefinition deployProcessWithBoundaryEvent(final long timerValue, final boolean withData) throws BonitaException,
            InvalidProcessDefinitionException {
        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("pTimerBoundary", "2.0");
        final String actorName = "delivery";
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask("step1", actorName);

        final Expression timerExpr;
        if (withData) {
            processDefinitionBuilder.addData("timer", Long.class.getName(), new ExpressionBuilder().createConstantLongExpression(timerValue));
            timerExpr = new ExpressionBuilder().createDataExpression("timer", Long.class.getName());
        } else {
            timerExpr = new ExpressionBuilder().createConstantLongExpression(timerValue);
        }
        userTaskDefinitionBuilder.addBoundaryEvent("Boundary timer").addTimerEventTriggerDefinition(TimerType.DURATION, timerExpr);
        userTaskDefinitionBuilder.addUserTask("exceptionStep", actorName);
        userTaskDefinitionBuilder.addUserTask("step2", actorName);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("Boundary timer", "exceptionStep");
        processDefinitionBuilder.addTransition("exceptionStep", "end");

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), actorName, donaBenta);
    }

    @Test
    public void timerEventTriggered() throws Exception {
        executeProcessWithTimerBoundary(false);
    }

    @Test
    public void timerEventTriggeredSubtasks() throws Exception {
        executeProcessWithTimerBoundary(true);
    }

    private void executeProcessWithTimerBoundary(final boolean addChild) throws Exception {
        final long timerDuration = 1000;

        final ProcessDefinition processDefinition = deployProcessWithBoundaryEvent(timerDuration, false);
        try {
            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
            final long step1Id = waitForUserTask(processInstance, "step1");

            ManualTaskInstance manualUserTask = null;
            if (addChild) {
                getProcessAPI().assignUserTask(step1Id, donaBenta.getId());
                final ManualTaskCreator taskCreator = buildManualTaskCreator(step1Id, "childOfStep1", donaBenta.getId(), "child task",
                        new Date(), TaskPriority.NORMAL);
                manualUserTask = getProcessAPI().addManualUserTask(taskCreator);
            }

            final long exceptionStepId = waitForUserTask(processInstance, "exceptionStep");

            ArchivedActivityInstance archActivityInst = waitForArchivedActivity(step1Id, TestStates.ABORTED);
            if (manualUserTask != null) {
                archActivityInst = getProcessAPI().getArchivedActivityInstance(manualUserTask.getId());
                assertEquals(TestStates.ABORTED.getStateName(), archActivityInst.getState());
            }

            assignAndExecuteStep(exceptionStepId, donaBenta);
            waitForProcessToFinish(processInstance);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

}
