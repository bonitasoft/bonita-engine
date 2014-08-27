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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnectorThatThrowException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
public class ErrorEventSubProcessTest extends EventsAPITest {

    private List<ProcessDefinition> processDefinitions;

    private User john;

    @Before
    public void beforeTest() throws BonitaException {
        processDefinitions = new ArrayList<ProcessDefinition>();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        john = createUser("john", "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        disableAndDeleteProcess(processDefinitions);
        deleteUser(john);
        logoutOnTenant();
    }

    private ProcessDefinition deployAndEnableProcessWithErrorEventSubProcess(final String catchErrorCode, final String throwErrorCode,
            final String subProcStartEventName) throws BonitaException {
        final Expression transitionCondition = new ExpressionBuilder().createDataExpression("throwException", Boolean.class.getName());
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addBooleanData("throwException", new ExpressionBuilder().createConstantBooleanExpression(true));
        builder.addStartEvent("start");
        builder.addUserTask("step1", "mainActor");
        builder.addUserTask("step2", "mainActor");
        builder.addEndEvent("end");
        builder.addEndEvent("endError").addErrorEventTrigger(throwErrorCode);
        builder.addTransition("start", "step1");
        builder.addTransition("start", "step2");
        builder.addTransition("step1", "end");
        builder.addTransition("step2", "endError", transitionCondition);
        builder.addDefaultTransition("step2", "end");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(BuildTestUtil.EVENT_SUB_PROCESS_NAME, true).getSubProcessBuilder();
        if (catchErrorCode == null) {
            subProcessBuilder.addStartEvent(subProcStartEventName).addErrorEventTrigger();
        } else {
            subProcessBuilder.addStartEvent(subProcStartEventName).addErrorEventTrigger(catchErrorCode);
        }
        subProcessBuilder.addUserTask("subStep", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(subProcStartEventName, "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        return deployAndEnableProcessWithActor(builder.done(), "mainActor", john);
    }

    private ProcessDefinition deployAndEnableProcessWithCallActivity(final String processName, final String targetProcessName, final String targetVersion)
            throws BonitaException {
        final Expression targetProcessExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetVersionExpr = new ExpressionBuilder().createConstantStringExpression(targetVersion);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        builder.addActor("mainActor");
        builder.addStartEvent("start");
        builder.addCallActivity("callActivity", targetProcessExpr, targetVersionExpr);
        builder.addUserTask("step2", "mainActor");
        builder.addEndEvent("end");
        builder.addTransition("start", "callActivity");
        builder.addTransition("callActivity", "step2");
        builder.addTransition("step2", "end");
        return deployAndEnableProcessWithActor(builder.done(), "mainActor", john);
    }

    private ProcessDefinition deployAndEnableProcessWithErrorEventSubProcessAndData(final String catchErrorCode, final String throwErroCode,
            final String subProcStartEventName) throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("parentVar"));
        builder.addIntegerData("count", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addActor("mainActor");
        builder.addStartEvent("start");
        builder.addUserTask("step1", "mainActor");
        builder.addUserTask("step2", "mainActor");
        builder.addEndEvent("end");
        builder.addEndEvent("endError").addErrorEventTrigger(throwErroCode);
        builder.addTransition("start", "step1");
        builder.addTransition("start", "step2");
        builder.addTransition("step1", "end");
        builder.addTransition("step2", "endError");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(BuildTestUtil.EVENT_SUB_PROCESS_NAME, true).getSubProcessBuilder();
        subProcessBuilder.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("childVar"));
        subProcessBuilder.addDoubleData("value", new ExpressionBuilder().createConstantDoubleExpression(10.0));
        subProcessBuilder.addStartEvent(subProcStartEventName).addErrorEventTrigger(catchErrorCode);
        subProcessBuilder.addUserTask("subStep", "mainActor").addShortTextData("content",
                new ExpressionBuilder().createConstantStringExpression("childActivityVar"));
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(subProcStartEventName, "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        return deployAndEnableProcessWithActor(builder.done(), "mainActor", john);
    }

    private ProcessDefinition deployAndEnableProcWithErrorEvSubProcAndDataOnlyInRoot(final String errorCode, final String subProcStartEventName,
            final String rootUserTaskName, final String subProcUserTaskName, final String dataName, final String dataValue) throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression(dataValue));
        builder.addActor("mainActor");
        builder.addStartEvent("start");
        builder.addUserTask(rootUserTaskName, "mainActor");
        builder.addEndEvent("end");
        builder.addEndEvent("endError").addErrorEventTrigger(errorCode);
        builder.addTransition("start", rootUserTaskName);
        builder.addTransition(rootUserTaskName, "endError");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(BuildTestUtil.EVENT_SUB_PROCESS_NAME, true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent(subProcStartEventName).addErrorEventTrigger(errorCode);
        subProcessBuilder.addUserTask(subProcUserTaskName, "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(subProcStartEventName, subProcUserTaskName);
        subProcessBuilder.addTransition(subProcUserTaskName, "endSubProcess");
        return deployAndEnableProcessWithActor(builder.done(), "mainActor", john);
    }

    private ProcessDefinition deployAndEnableProcWithErrorEvSubProcAndDataOnlyInSubProc(final String errorCode, final String subProcStartEventName,
            final String rootUserTaskName, final String subProcUserTaskName, final String dataName, final String dataValue) throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addStartEvent("start");
        builder.addUserTask(rootUserTaskName, "mainActor");
        builder.addEndEvent("end");
        builder.addEndEvent("endError").addErrorEventTrigger(errorCode);
        builder.addTransition("start", rootUserTaskName);
        builder.addTransition(rootUserTaskName, "endError");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(BuildTestUtil.EVENT_SUB_PROCESS_NAME, true).getSubProcessBuilder();
        subProcessBuilder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression(dataValue));
        subProcessBuilder.addStartEvent(subProcStartEventName).addErrorEventTrigger(errorCode);
        subProcessBuilder.addUserTask(subProcUserTaskName, "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(subProcStartEventName, subProcUserTaskName);
        subProcessBuilder.addTransition(subProcUserTaskName, "endSubProcess");
        return deployAndEnableProcessWithActor(builder.done(), "mainActor", john);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error" }, jira = "ENGINE-536")
    @Test
    public void errorEventSubProcessTriggeredNamedError() throws Exception {
        executeProcessTriggeringEventSubProcess("e1", "e1");
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error" }, jira = "ENGINE-536")
    @Test
    public void errorEventSubProcessTriggeredCachAllErrors() throws Exception {
        executeProcessTriggeringEventSubProcess(null, "e1");
    }

    private void executeProcessTriggeringEventSubProcess(final String catchErrorCode, final String throwErrorCode) throws Exception {
        final String subProcStartEventName = "errorStart";
        processDefinitions.add(deployAndEnableProcessWithErrorEventSubProcess(catchErrorCode, throwErrorCode, subProcStartEventName));
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());
        final ActivityInstance step2 = waitForUserTask("step2", processInstance.getId());
        List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(2, activities.size());
        checkNumberOfWaitingEvents(subProcStartEventName, 1);

        // throw error
        assignAndExecuteStep(step2, john.getId());
        waitForArchivedActivity(step2.getId(), TestStates.getNormalFinalState());

        final FlowNodeInstance eventSubProcessActivity = waitForFlowNodeInExecutingState(processInstance, BuildTestUtil.EVENT_SUB_PROCESS_NAME, false);
        final ActivityInstance subStep = waitForUserTask("subStep", processInstance.getId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(2, activities.size());
        // the parent process instance is supposed to be aborted, so no more waiting events are expected
        checkNumberOfWaitingEvents(subProcStartEventName, 0);

        assignAndExecuteStep(subStep, john.getId());

        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);

        // check that the transition wasn't taken
        checkWasntExecuted(processInstance, "end");
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error" }, jira = "ENGINE-536")
    @Test
    public void errorEventSubProcessNotTriggered() throws Exception {
        final String subProcStartEventName = "errorStart";
        processDefinitions.add(deployAndEnableProcessWithErrorEventSubProcess("error 1", "error 1", subProcStartEventName));
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());
        final ActivityInstance step2 = waitForUserTask("step2", processInstance.getId());
        getProcessAPI().getProcessDataInstance("throwException", processInstance.getId());
        getProcessAPI().updateProcessDataInstance("throwException", processInstance.getId(), false);
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(2, activities.size());
        checkNumberOfWaitingEvents(subProcStartEventName, 1);

        assignAndExecuteStep(step1, john.getId());
        assignAndExecuteStep(step2, john.getId());

        waitForArchivedActivity(step1.getId(), TestStates.getNormalFinalState());
        waitForProcessToFinish(processInstance);

        // the parent process instance has completed, so no more waiting events are expected
        checkNumberOfWaitingEvents(subProcStartEventName, 0);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error" }, jira = "ENGINE-536")
    @Test
    public void createSeveralInstances() throws Exception {
        processDefinitions.add(deployAndEnableProcessWithErrorEventSubProcess("e2", "e2", "errorStart"));
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinitions.get(0).getId());

        // throw error
        waitForUserTask("step1", processInstance1.getId());
        waitForUserTaskAndExecuteIt("step2", processInstance1.getId(), john.getId());

        waitForUserTask("step1", processInstance2.getId());
        waitForUserTaskAndExecuteIt("step2", processInstance2.getId(), john.getId());

        waitForUserTask("subStep", processInstance1.getId());
        waitForUserTask("subStep", processInstance2.getId());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error", "parent process data" }, jira = "ENGINE-536")
    @Test
    public void subProcessCanAccessParentData() throws Exception {
        processDefinitions.add(deployAndEnableProcessWithErrorEventSubProcessAndData("error1", "error1", "errorStart"));
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        waitForUserTask("step1", processInstance.getId());
        waitForUserTaskAndExecuteIt("step2", processInstance.getId(), john.getId());

        final ActivityInstance subStep = waitForUserTask("subStep", processInstance.getId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkRetrieveDataInstances(processInstance, subStep, subProcInst);

        checkEvaluateExpression(subStep, "count", Integer.class, 1);

        assignAndExecuteStep(subStep, john.getId());
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error", "parent process data" }, jira = "ENGINE-1397")
    @Test
    public void subProcessCanAccessParentDataEvenIfItDoesntHaveLocalData() throws Exception {
        final String rootUserTaskName = "step1";
        final String subProcUserTaskName = "subStep";
        final String dataName = "content";
        final String dataValue = "default";
        processDefinitions.add(deployAndEnableProcWithErrorEvSubProcAndDataOnlyInRoot("error1", "errorStart", rootUserTaskName,
                subProcUserTaskName, dataName, dataValue));
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        waitForUserTaskAndExecuteIt(rootUserTaskName, processInstance, john);

        final ActivityInstance subStep = waitForUserTask(subProcUserTaskName, processInstance);
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkProcessDataInstance(dataName, subProcInst.getId(), dataValue);
        checkProcessDataInstance(dataName, processInstance.getId(), dataValue);

        checkEvaluateExpression(subStep, dataName, String.class, dataValue);

        assignAndExecuteStep(subStep, john.getId());
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error", "parent process data" }, jira = "ENGINE-1397")
    @Test
    public void eventSubProcesWithDataAndRootProcessWithNoData() throws Exception {
        final String rootUserTaskName = "step1";
        final String subProcUserTaskName = "subStep";
        final String dataName = "content";
        final String dataValue = "default";
        processDefinitions.add(deployAndEnableProcWithErrorEvSubProcAndDataOnlyInSubProc("error1", "errorStart", rootUserTaskName,
                subProcUserTaskName, dataName, dataValue));
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        waitForUserTaskAndExecuteIt(rootUserTaskName, processInstance.getId(), john.getId());

        final ActivityInstance subStep = waitForUserTask(subProcUserTaskName, processInstance.getId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkProcessDataInstance(dataName, subProcInst.getId(), dataValue);

        checkEvaluateExpression(subStep, dataName, String.class, dataValue);

        assignAndExecuteStep(subStep, john.getId());
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);
    }

    private void checkEvaluateExpression(final ActivityInstance subStep, final String dataName, final Class<?> expressionType, final Serializable expectedValue)
            throws InvalidExpressionException, ExpressionEvaluationException {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(1);
        final Expression contVarExpr = new ExpressionBuilder().createDataExpression(dataName, expressionType.getName());
        expressions.put(contVarExpr, null);

        final Map<String, Serializable> expressionResults = getProcessAPI().evaluateExpressionsOnActivityInstance(subStep.getId(), expressions);
        assertEquals(expectedValue, expressionResults.get(contVarExpr.getName()));
    }

    private void checkRetrieveDataInstances(final ProcessInstance processInstance, final ActivityInstance subStep, final ProcessInstance subProcInst)
            throws DataNotFoundException {
        checkProcessDataInstance("count", subProcInst.getId(), 1);
        checkProcessDataInstance("content", subProcInst.getId(), "childVar");
        checkProcessDataInstance("value", subProcInst.getId(), 10.0);
        checkProcessDataInstance("content", processInstance.getId(), "parentVar");
        checkActivityDataInstance("content", subStep.getId(), "childActivityVar");
    }

    private void checkProcessDataInstance(final String dataName, final long processInstanceId, final Serializable expectedValue) throws DataNotFoundException {
        final DataInstance processDataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstanceId);
        assertEquals(expectedValue, processDataInstance.getValue());
    }

    private void checkActivityDataInstance(final String dataName, final long activityInstanceId, final Serializable expectedValue) throws DataNotFoundException {
        final DataInstance activityDataInstance = getProcessAPI().getActivityDataInstance(dataName, activityInstanceId);
        assertEquals(expectedValue, activityDataInstance.getValue());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error", "call activity" }, jira = "ENGINE-536")
    @Test
    public void errorEventSubProcInsideTargetCallActivity() throws Exception {
        processDefinitions.add(deployAndEnableProcessWithErrorEventSubProcess("e1", "e1", "errorStart"));
        processDefinitions.add(deployAndEnableProcessWithCallActivity("ProcessWithCallActivity", processDefinitions.get(0).getName(),
                processDefinitions.get(0).getVersion()));
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(1).getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());
        waitForUserTaskAndExecuteIt("step2", processInstance.getId(), john.getId());

        final ActivityInstance subStep = waitForUserTask("subStep", processInstance.getId());
        final ProcessInstance calledProcInst = getProcessAPI().getProcessInstance(step1.getParentProcessInstanceId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());
        assignAndExecuteStep(subStep, john.getId());
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(calledProcInst, ProcessInstanceState.ABORTED);

        waitForUserTaskAndExecuteIt("step2", processInstance.getId(), john.getId());
        waitForProcessToFinish(processInstance);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error", "call activity",
            "archived" }, story = "The process A have a call activity (calling the process B) and an event sub process. "
            + "The process B have a automatic task with a failed connector. "
            + "When the process B failed and execute the event sub process of the process A, the process A must be aborted.", jira = "BS-8754")
    @Test
    public void processWithErrorEventSubProcAndCallActivity_must_be_finished_when_subProcess_is_finished() throws Exception {
        // Create the target process
        processDefinitions.add(deployProcessWithTestConnectorThatThrowException(BuildTestUtil
                .buildProcessDefinitionWithUserTaskAndFailedConnector(PROCESS_NAME)));

        // Create the caller process
        final Expression targetProcessExpr = new ExpressionBuilder().createConstantStringExpression(BuildTestUtil.PROCESS_NAME);
        final Expression targetVersionExpr = new ExpressionBuilder().createConstantStringExpression(BuildTestUtil.PROCESS_VERSION);

        final ProcessDefinitionBuilder builder = BuildTestUtil.buildProcessDefinitionWithCallActivity("ProcessWithEventSubProcessAndCallActivity",
                targetProcessExpr, targetVersionExpr);
        BuildTestUtil.buildErrorEventSubProcessWithUserTask("SubStep", builder);
        processDefinitions.add(deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john));

        // Start the caller process
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(1).getId());
        final HumanTaskInstance stepBeforeFailedConnector = waitForUserTaskAndExecuteIt("StepBeforeFailedConnector", john);
        final ActivityInstance subStep = waitForUserTask("SubStep");

        assignAndExecuteStep(subStep, john.getId());
        waitForProcessToFinish(subStep.getParentProcessInstanceId());
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);
        waitForProcessToBeInState(stepBeforeFailedConnector.getParentProcessInstanceId(), ProcessInstanceState.ABORTED);
    }

    private ProcessDefinition deployProcessWithTestConnectorThatThrowException(final ProcessDefinitionBuilder processDefinitionBuilder)
            throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, ACTOR_NAME, john, "TestConnectorThatThrowException.impl",
                TestConnectorThatThrowException.class, "TestConnectorThatThrowException.jar");
    }

}
