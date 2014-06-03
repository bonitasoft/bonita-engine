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
package org.bonitasoft.engine.event;

import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ErrorEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class ErrorBoundaryEventTest extends CommonAPITest {

    private User donaBenta;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        donaBenta = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
    }

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        deleteUser(donaBenta.getId());
        logoutOnTenant();
    }

    private ProcessDefinition deployAndEnableProcessWithEndThrowErrorEvent(final String processName, final String errorCode, final String actorName)
            throws BonitaException {
        final ProcessDefinitionBuilder calledProcess = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        calledProcess.addActor(actorName);
        calledProcess.addStartEvent("start");
        calledProcess.addUserTask("calledStep1", actorName);
        calledProcess.addUserTask("calledStep2", actorName);
        calledProcess.addEndEvent("endError").addErrorEventTrigger(errorCode);
        calledProcess.addEndEvent("end").addTerminateEventTrigger();
        calledProcess.addTransition("start", "calledStep1");
        calledProcess.addTransition("start", "calledStep2");
        calledProcess.addTransition("calledStep1", "endError");
        calledProcess.addTransition("calledStep2", "end");
        return deployAndEnableProcessWithActor(calledProcess.done(), actorName, donaBenta);
    }

    private ProcessDefinition deployAndEnableProcessWithBoundaryErrorEventOnCallActivity(final String processName, final String targetProcessName,
            final String callActivityName, final String errorCode, final String actorName) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addStartEvent("start");
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity(callActivityName,
                new ExpressionBuilder().createConstantStringExpression(targetProcessName), new ExpressionBuilder().createConstantStringExpression("1.0"));
        callActivityBuilder.addBoundaryEvent("error", true).addErrorEventTrigger(errorCode);
        processDefinitionBuilder.addUserTask("exceptionStep", actorName);
        processDefinitionBuilder.addUserTask("step2", actorName);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", callActivityName);
        processDefinitionBuilder.addTransition(callActivityName, "step2");
        processDefinitionBuilder.addTransition("error", "exceptionStep");
        processDefinitionBuilder.addTransition("exceptionStep", "end");
        processDefinitionBuilder.addTransition("step2", "end");
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), actorName, donaBenta);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event", "call activity" }, jira = "ENGINE-501")
    public void errorBoundaryEventTriggeredNamedError() throws Exception {
        executionWitherrorEventTriggered("error1");
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event", "call activity" }, jira = "ENGINE-501")
    public void errorBoundaryEventTriggeredCatchAllError() throws Exception {
        executionWitherrorEventTriggered(null);
    }

    protected void executionWitherrorEventTriggered(final String catchErrorCode) throws Exception {
        final ProcessDefinition calledProcDef = deployAndEnableProcessWithEndThrowErrorEvent("calledProcess", "error1", "delivery");
        final ProcessDefinition callerProcDef = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("pErrorBoundary", "calledProcess", "callStep",
                catchErrorCode, "delivery");

        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcDef.getId());
        final FlowNodeInstance callActivity = waitForFlowNodeInExecutingState(processInstance, "callStep", false);
        final ActivityInstance calledStep1 = waitForUserTask("calledStep1", processInstance);
        final ActivityInstance calledStep2 = waitForUserTask("calledStep2", processInstance);
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep1.getParentProcessInstanceId());
        assignAndExecuteStep(calledStep1, donaBenta.getId());

        waitForProcessToFinish(calledProcessInstance);
        try {
            waitForArchivedActivity(calledStep2.getId(), TestStates.getAbortedState());
        } catch (final Exception e) {
            final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(processInstance.getId(), 0, 100,
                    ActivityInstanceCriterion.DEFAULT);
            System.out.println("After completion of the called process");
            for (final ArchivedActivityInstance archivedActivityInstance : archivedActivityInstances) {
                System.out.println("name=" + archivedActivityInstance.getName() + ", state=" + archivedActivityInstance.getState() + ", archivedDate="
                        + archivedActivityInstance.getArchiveDate().getTime());
            }
            throw new Exception(archivedActivityInstances.toString(), e);
        }
        final ActivityInstance executionStep = waitForUserTask("exceptionStep", processInstance);

        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(callActivity.getId(), TestStates.getAbortedState());
        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(calledProcDef);
        disableAndDeleteProcess(callerProcDef);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event", "call activity" }, jira = "ENGINE-501")
    public void errorBoundaryEventNotTriggered() throws Exception {
        final ProcessDefinition calledProcDef = deployAndEnableProcessWithEndThrowErrorEvent("calledProcess", "error1", "delivery");
        final ProcessDefinition callerProcDef = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("pErrorBoundary", "calledProcess", "callStep",
                "error1", "delivery");

        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcDef.getId());
        final ActivityInstance calledStep1 = waitForUserTask("calledStep1", processInstance.getId());
        final ActivityInstance calledStep2 = waitForUserTask("calledStep2", processInstance.getId());
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep1.getParentProcessInstanceId());
        assignAndExecuteStep(calledStep2, donaBenta.getId());

        waitForProcessToFinish(calledProcessInstance);
        waitForArchivedActivity(calledStep1.getId(), TestStates.getAbortedState());

        final ActivityInstance executionStep = waitForUserTask("step2", processInstance.getId());
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(calledProcDef);
        disableAndDeleteProcess(callerProcDef);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event" }, jira = "ENGINE-501")
    public void testUncaughtThrowErrorEvent() throws Exception {
        final ProcessDefinition calledProcDef = deployAndEnableProcessWithEndThrowErrorEvent("calledProcess", "error1", "delivery");
        // catch a different error
        final ProcessDefinition callerProcDef = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("pErrorBoundary", "calledProcess", "callStep",
                "error2", "delivery");

        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcDef.getId());
        waitForFlowNodeInExecutingState(processInstance, "callStep", false);
        final ActivityInstance calledStep1 = waitForUserTask("calledStep1", processInstance.getId());
        final ActivityInstance calledStep2 = waitForUserTask("calledStep2", processInstance.getId());
        assignAndExecuteStep(calledStep1, donaBenta.getId());

        waitForArchivedActivity(calledStep2.getId(), TestStates.getAbortedState());
        // if there are no catch error able to handle the thrown error, the throw error event has the same behavior as a terminate event.
        waitForUserTaskAndExecuteIt("step2", processInstance.getId(), donaBenta.getId());

        waitForProcessToFinish(processInstance);

        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(calledProcDef);
        disableAndDeleteProcess(callerProcDef);

    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event" }, jira = "ENGINE-501")
    public void errorEventCaughtAtParentLevel2() throws Exception {
        final ProcessDefinition procDefLevel0 = deployAndEnableProcessWithEndThrowErrorEvent("procDefLevel0", "error1", "delivery");
        final ProcessDefinition procDefLevel1 = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("procDefLevel1", "procDefLevel0", "callStepL1",
                "error2", "delivery");
        final ProcessDefinition procDefLevel2 = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("procDefLevel2", "procDefLevel1", "callStepL2",
                "error1", "delivery");

        final ProcessInstance processInstance = getProcessAPI().startProcess(procDefLevel2.getId());
        final FlowNodeInstance callActivityL2 = waitForFlowNodeInExecutingState(processInstance, "callStepL2", false);
        final FlowNodeInstance callActivityL1 = waitForFlowNodeInExecutingState(processInstance, "callStepL1", true);
        final ActivityInstance calledStep1 = waitForUserTask("calledStep1", processInstance.getId());
        final ActivityInstance calledStep2 = waitForUserTask("calledStep2", processInstance.getId());
        final ProcessInstance calledProcessInstanceL0 = getProcessAPI().getProcessInstance(calledStep1.getParentProcessInstanceId());
        final ProcessInstance calledProcessInstanceL1 = getProcessAPI().getProcessInstance(callActivityL1.getParentProcessInstanceId());
        assignAndExecuteStep(calledStep1, donaBenta.getId());

        waitForArchivedActivity(calledStep2.getId(), TestStates.getAbortedState());
        final FlowNodeInstance executionStep = waitForFlowNodeInReadyState(processInstance, "exceptionStep", false);
        waitForProcessToFinish(calledProcessInstanceL0);
        waitForProcessToFinish(calledProcessInstanceL1, TestStates.getAbortedState());

        assignAndExecuteStep(executionStep.getId(), donaBenta.getId());
        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(callActivityL1.getId(), TestStates.getAbortedState());
        waitForArchivedActivity(callActivityL2.getId(), TestStates.getAbortedState());
        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(procDefLevel0);
        disableAndDeleteProcess(procDefLevel1);
        disableAndDeleteProcess(procDefLevel2);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event" }, jira = "ENGINE-501")
    public void errorEventTwoCatchErrorMatching() throws Exception {
        final ProcessDefinition procDefLevel0 = deployAndEnableProcessWithEndThrowErrorEvent("procDefLevel0", "error1", "delivery");
        final ProcessDefinition procDefLevel1 = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("procDefLevel1", "procDefLevel0", "callStepL1",
                "error1", "delivery");
        final ProcessDefinition procDefLevel2 = deployAndEnableProcessWithBoundaryErrorEventOnCallActivity("procDefLevel2", "procDefLevel1", "callStepL2",
                "error1", "delivery");

        final ProcessInstance processInstance = getProcessAPI().startProcess(procDefLevel2.getId());
        final FlowNodeInstance callActivityL2 = waitForFlowNodeInExecutingState(processInstance, "callStepL2", false);
        final FlowNodeInstance callActivityL1 = waitForFlowNodeInExecutingState(processInstance, "callStepL1", true);
        final ActivityInstance calledStep1 = waitForUserTask("calledStep1", processInstance.getId());
        final ActivityInstance calledStep2 = waitForUserTask("calledStep2", processInstance.getId());
        final ProcessInstance calledProcessInstanceL0 = getProcessAPI().getProcessInstance(calledStep1.getParentProcessInstanceId());
        final ProcessInstance calledProcessInstanceL1 = getProcessAPI().getProcessInstance(callActivityL1.getParentProcessInstanceId());
        assignAndExecuteStep(calledStep1, donaBenta.getId());

        waitForArchivedActivity(calledStep2.getId(), TestStates.getAbortedState());
        final FlowNodeInstance executionStep = waitForFlowNodeInReadyState(calledProcessInstanceL1, "exceptionStep", false);
        waitForProcessToFinish(calledProcessInstanceL0);

        assignAndExecuteStep(executionStep.getId(), donaBenta.getId());
        waitForProcessToFinish(calledProcessInstanceL1);

        waitForUserTaskAndExecuteIt("step2", processInstance.getId(), donaBenta.getId());
        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(callActivityL1.getId(), TestStates.getAbortedState());
        waitForArchivedActivity(callActivityL2.getId(), TestStates.getNormalFinalState());
        checkWasntExecuted(calledProcessInstanceL1, "step2");

        disableAndDeleteProcess(procDefLevel0);
        disableAndDeleteProcess(procDefLevel1);
        disableAndDeleteProcess(procDefLevel2);
    }
}
