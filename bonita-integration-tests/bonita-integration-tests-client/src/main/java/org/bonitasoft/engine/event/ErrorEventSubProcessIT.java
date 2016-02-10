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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
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
public class ErrorEventSubProcessIT extends AbstractWaitingEventIT {

    private List<ProcessDefinition> processDefinitions;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        processDefinitions = new ArrayList<ProcessDefinition>();
    }

    @Override
    @After
    public void after() throws Exception {
        disableAndDeleteProcess(processDefinitions);
        super.after();
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
        waitForUserTask(processInstance.getId(), "step1");
        final long step2Id = waitForUserTask(processInstance, "step2");
        List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(2, activities.size());
        checkNumberOfWaitingEvents(subProcStartEventName, 1);

        // throw error
        assignAndExecuteStep(step2Id, user);
        waitForArchivedActivity(step2Id, TestStates.NORMAL_FINAL);

        waitForFlowNodeInExecutingState(processInstance, BuildTestUtil.EVENT_SUB_PROCESS_NAME, false);
        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, "subStep");
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(2, activities.size());
        // the parent process instance is supposed to be aborted, so no more waiting events are expected
        checkNumberOfWaitingEvents(subProcStartEventName, 0);

        assignAndExecuteStep(subStep, user);

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
        final long step1Id = waitForUserTask(processInstance.getId(), "step1");
        final long step2Id = waitForUserTask(processInstance.getId(), "step2");
        getProcessAPI().getProcessDataInstance("throwException", processInstance.getId());
        getProcessAPI().updateProcessDataInstance("throwException", processInstance.getId(), false);
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(2, activities.size());
        checkNumberOfWaitingEvents(subProcStartEventName, 1);

        assignAndExecuteStep(step1Id, user);
        assignAndExecuteStep(step2Id, user);

        waitForArchivedActivity(step1Id, TestStates.NORMAL_FINAL);
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
        waitForUserTask(processInstance1.getId(), "step1");
        waitForUserTaskAndExecuteIt(processInstance1, "step2", user);

        waitForUserTask(processInstance2.getId(), "step1");
        waitForUserTaskAndExecuteIt(processInstance2, "step2", user);

        waitForUserTask(processInstance1.getId(), "subStep");
        waitForUserTask(processInstance2.getId(), "subStep");
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error", "parent process data" }, jira = "ENGINE-536")
    @Test
    public void subProcessCanAccessParentData() throws Exception {
        processDefinitions.add(deployAndEnableProcessWithErrorEventSubProcessAndData("error1", "error1", "errorStart"));
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        waitForUserTask(processInstance.getId(), "step1");
        waitForUserTaskAndExecuteIt(processInstance, "step2", user);

        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, "subStep");
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkRetrieveDataInstances(processInstance, subStep, subProcInst);
        checkEvaluateExpression(subStep, "count", Integer.class, 1);

        assignAndExecuteStep(subStep, user);
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
        processDefinitions.add(deployAndEnableProcessWithErrorEventSubProcessAndDataOnlyInRoot("error1", "errorStart", rootUserTaskName,
                subProcUserTaskName, dataName, dataValue));
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        waitForUserTaskAndExecuteIt(processInstance, rootUserTaskName, user);

        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, subProcUserTaskName);
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkProcessDataInstance(dataName, subProcInst.getId(), dataValue);
        checkProcessDataInstance(dataName, processInstance.getId(), dataValue);

        checkEvaluateExpression(subStep, dataName, String.class, dataValue);

        assignAndExecuteStep(subStep, user);
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
        processDefinitions.add(deployAndEnableProcessWithErrorEventSubProcessAndDataOnlyInSubProc("error1", "errorStart", rootUserTaskName,
                subProcUserTaskName, dataName, dataValue));
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        waitForUserTaskAndExecuteIt(processInstance, rootUserTaskName, user);

        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, subProcUserTaskName);
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkProcessDataInstance(dataName, subProcInst.getId(), dataValue);

        checkEvaluateExpression(subStep, dataName, String.class, dataValue);

        assignAndExecuteStep(subStep, user);
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
        processDefinitions.add(deployAndEnableProcessWithCallActivity(processDefinitions.get(0).getName(), processDefinitions.get(0).getVersion()));
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(1).getId());
        final ActivityInstance step1 = waitForUserTaskAndGetIt(processInstance, "step1");
        waitForUserTaskAndExecuteIt(processInstance, "step2", user);

        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, "subStep");
        final ProcessInstance calledProcInst = getProcessAPI().getProcessInstance(step1.getParentProcessInstanceId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        waitForArchivedActivity(step1.getId(), TestStates.ABORTED);
        assignAndExecuteStep(subStep, user);
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(calledProcInst, ProcessInstanceState.ABORTED);

        waitForUserTaskAndExecuteIt(processInstance, "step2", user);
        waitForProcessToFinish(processInstance);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "error", "call activity",
            "archived" }, story = "The process A have a call activity (calling the process B) and an event sub process. "
            + "The process B have a automatic task with a failed connector. "
            + "When the process B failed and execute the event sub process of the process A, the process A must be aborted.", jira = "BS-8754")
    @Test
    public void processWithErrorEventSubProcAndCallActivity_must_be_finished_when_subProcess_is_finished() throws Exception {
        // Create the target process
        processDefinitions.add(deployAndEnableProcessWithTestConnectorThatThrowException(BuildTestUtil
                .buildProcessDefinitionWithUserTaskAndFailedConnector(PROCESS_NAME)));

        // Create the caller process
        final Expression targetProcessExpr = new ExpressionBuilder().createConstantStringExpression(BuildTestUtil.PROCESS_NAME);
        final Expression targetVersionExpr = new ExpressionBuilder().createConstantStringExpression(BuildTestUtil.PROCESS_VERSION);

        final ProcessDefinitionBuilder builder = BuildTestUtil.buildProcessDefinitionWithCallActivity("ProcessWithEventSubProcessAndCallActivity",
                targetProcessExpr, targetVersionExpr);
        BuildTestUtil.buildErrorEventSubProcessWithUserTask("SubStep", builder);
        processDefinitions.add(deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user));

        // Start the caller process
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(1).getId());
        final HumanTaskInstance stepBeforeFailedConnector = waitForUserTaskAndExecuteAndGetIt("StepBeforeFailedConnector", user);
        final ActivityInstance subStep = waitForUserTaskAndExecuteAndGetIt("SubStep", user);

        waitForProcessToFinish(subStep.getParentProcessInstanceId());
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);
        waitForProcessToBeInState(stepBeforeFailedConnector.getParentProcessInstanceId(), ProcessInstanceState.ABORTED);
    }

}
