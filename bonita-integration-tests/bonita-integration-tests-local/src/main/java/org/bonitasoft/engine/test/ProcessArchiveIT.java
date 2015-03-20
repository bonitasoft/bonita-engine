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
package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BonitaTestRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class ProcessArchiveIT extends CommonAPILocalIT {

    private User john;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        john = createUser(USERNAME, "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        deleteUser(john);
        logoutOnTenant();
    }

    @Test
    public void deleteProcessDefinitionDeleteArchivedInstances() throws Exception {
        final long initialNumberOfArchivedProcessInstance = getProcessAPI().getNumberOfArchivedProcessInstances();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("ProcessToDelete", "1.0")
                .addAutomaticTask("step1").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p3 = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(p1);
        waitForProcessToFinish(p2);
        waitForProcessToFinish(p3);
        assertEquals(initialNumberOfArchivedProcessInstance + 3, getProcessAPI().getNumberOfArchivedProcessInstances());
        disableAndDeleteProcess(processDefinition);
        assertEquals(initialNumberOfArchivedProcessInstance, getProcessAPI().getNumberOfArchivedProcessInstances());
    }

    @Test()
    public void deleteProcessDefinitionDeleteArchivedInstancesWithData() throws Exception {
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final UserTransactionService userTransactionService = tenantAccessor.getUserTransactionService();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final long initialNumberOfArchivedProcessInstance = getProcessAPI().getNumberOfArchivedProcessInstances();
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessToDelete", "1.0");
        processDefinitionBuilder.addActor("actor");
        processDefinitionBuilder.addShortTextData("procData", new ExpressionBuilder().createConstantStringExpression("procDataBalue"));
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask("step1", "actor");
        userTaskDefinitionBuilder.addOperation(new LeftOperandBuilder().createNewInstance("procData").done(), OperatorType.ASSIGNMENT, "=", null,
                new ExpressionBuilder().createConstantStringExpression("updated proc value"));
        userTaskDefinitionBuilder.addOperation(new LeftOperandBuilder().createNewInstance("activityData").done(), OperatorType.ASSIGNMENT, "=", null,
                new ExpressionBuilder().createConstantStringExpression("updated a value"));
        processDefinitionBuilder.addShortTextData("activityData", new ExpressionBuilder().createConstantStringExpression("activityDataBalue")).getProcess();
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, "actor", john);
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p3 = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(p1, "step1");
        final DataInstance activityDataInstance = getProcessAPI().getActivityDataInstance("activityData", step1Id);
        final DataInstance processDataInstance = getProcessAPI().getProcessDataInstance("procData", p1.getId());
        assertNotNull(activityDataInstance);
        assignAndExecuteStep(step1Id, john.getId());
        waitForUserTaskAndExecuteIt(p2, "step1", john);
        waitForUserTaskAndExecuteIt(p3, "step1", john);
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        userTransactionService.executeInTransaction(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                final SADataInstance saActDataInstances = dataInstanceService.getSADataInstance(activityDataInstance.getId(), System.currentTimeMillis());
                assertNotNull(saActDataInstances);
                final SADataInstance saProcDataInstances = dataInstanceService.getSADataInstance(processDataInstance.getId(), System.currentTimeMillis());
                assertNotNull(saProcDataInstances);

                return null;
            }
        });
        waitForProcessToFinish(p1);
        waitForProcessToFinish(p2);
        waitForProcessToFinish(p3);
        assertEquals(initialNumberOfArchivedProcessInstance + 3, getProcessAPI().getNumberOfArchivedProcessInstances());
        disableAndDeleteProcess(processDefinition);
        assertEquals(initialNumberOfArchivedProcessInstance, getProcessAPI().getNumberOfArchivedProcessInstances());

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        userTransactionService.executeInTransaction(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                final SADataInstance saActDataInstances = dataInstanceService.getSADataInstance(activityDataInstance.getId(), System.currentTimeMillis());
                final SADataInstance saProcDataInstances = dataInstanceService.getSADataInstance(processDataInstance.getId(), System.currentTimeMillis());
                assertNull(saActDataInstances);
                assertNull(saProcDataInstances);
                return null;
            }

        });
        // TODO check data instance visibility mapping when archived
    }

    @Test
    public void deleteProcessDefinitionDeleteArchivedInstancesWithTransition() throws Exception {
        setSessionInfo(getSession());
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final UserTransactionService userTransactionService = tenantAccessor.getUserTransactionService();
        final TransitionService transitionService = tenantAccessor.getTransitionInstanceService();
        final Callable<Long> getNumberOfArchivedTransitionInstances = new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                return transitionService.getNumberOfArchivedTransitionInstances(QueryOptions.countQueryOptions());
            }
        };

        final long initialNumber = userTransactionService.executeInTransaction(getNumberOfArchivedTransitionInstances);
        final long initialNumberOfArchivedProcessInstance = getProcessAPI().getNumberOfArchivedProcessInstances();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("ProcessToDelete", "1.0").addActor("actor")
                .addAutomaticTask("step1").addAutomaticTask("step2").addTransition("step1", "step2").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, "actor", john);
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p3 = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(p1);
        waitForProcessToFinish(p2);
        waitForProcessToFinish(p3);

        assertEquals(initialNumberOfArchivedProcessInstance + 3, getProcessAPI().getNumberOfArchivedProcessInstances());
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final long newNumber = userTransactionService.executeInTransaction(getNumberOfArchivedTransitionInstances);
        assertTrue(newNumber > initialNumber);
        disableAndDeleteProcess(processDefinition);
        assertEquals(initialNumberOfArchivedProcessInstance, getProcessAPI().getNumberOfArchivedProcessInstances());
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final long lastNumber = userTransactionService.executeInTransaction(getNumberOfArchivedTransitionInstances);
        assertEquals(initialNumber, lastNumber);
        cleanSession();
    }

    @Test
    public void deleteProcessDefinitionDeleteArchivedInstancesWithComment() throws Exception {
        setSessionInfo(getSession());
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final UserTransactionService userTransactionService = tenantAccessor.getUserTransactionService();
        final SCommentService commentService = tenantAccessor.getCommentService();
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, "bpm");
        final long initialNumberOfArchivedProcessInstance = getProcessAPI().getNumberOfArchivedProcessInstances();
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessToDelete", "1.0");
        processDefinitionBuilder.addActor("actor");
        processDefinitionBuilder.addUserTask("step1", "actor");
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, "actor", john);
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p3 = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(p1, "step1");
        getProcessAPI().addProcessComment(p1.getId(), "A cool comment on p1");
        getProcessAPI().addProcessComment(p2.getId(), "A cool comment on p2");
        getProcessAPI().addProcessComment(p3.getId(), "A cool comment on p3");
        final List<Comment> comments = getProcessAPI().getComments(p1.getId());
        assertEquals(1, comments.size());
        assertEquals("A cool comment on p1", comments.get(0).getContent());

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        userTransactionService.executeInTransaction(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                assertEquals(3, commentService.getNumberOfComments(QueryOptions.countQueryOptions()));
                assertEquals(0, commentService.getNumberOfArchivedComments(QueryOptions.countQueryOptions()));
                return null;
            }
        });
        assignAndExecuteStep(step1Id, john.getId());
        waitForUserTaskAndExecuteIt(p2, "step1", john);
        waitForUserTaskAndExecuteIt(p3, "step1", john);
        waitForProcessToFinish(p1);
        waitForProcessToFinish(p2);
        waitForProcessToFinish(p3);
        assertEquals(initialNumberOfArchivedProcessInstance + 3, getProcessAPI().getNumberOfArchivedProcessInstances());

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        userTransactionService.executeInTransaction(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                assertEquals(0, commentService.getNumberOfComments(QueryOptions.countQueryOptions()));
                // 3 comments + 3 system comments
                assertEquals(6, commentService.getNumberOfArchivedComments(QueryOptions.countQueryOptions()));
                return null;
            }
        });
        disableAndDeleteProcess(processDefinition);
        assertEquals(initialNumberOfArchivedProcessInstance, getProcessAPI().getNumberOfArchivedProcessInstances());

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        userTransactionService.executeInTransaction(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                assertEquals(0, commentService.getNumberOfComments(QueryOptions.countQueryOptions()));
                assertEquals(0, commentService.getNumberOfArchivedComments(QueryOptions.countQueryOptions()));
                return null;
            }
        });
        cleanSession();
    }

    @Test
    public void archivedFlowNodeInstance() throws Exception {
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, "bpm");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessToDelete", "1.0");
        processDefinitionBuilder.addActor("actor");
        processDefinitionBuilder.addUserTask("step1", "actor").addDescription("My Description")
                .addDisplayName(new ExpressionBuilder().createConstantStringExpression("My Display Name"))
                .addDisplayDescriptionAfterCompletion(new ExpressionBuilder().createConstantStringExpression("My Display Description"));
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, "actor", john);

        final ProcessDefinitionBuilder callingProcess = new ProcessDefinitionBuilder().createNewInstance("Caller", "1.0");
        callingProcess.addCallActivity("call", new ExpressionBuilder().createConstantStringExpression("ProcessToDelete"),
                new ExpressionBuilder().createConstantStringExpression("1.0"));

        final ProcessDefinition callingProcessDef = deployAndEnableProcess(callingProcess.getProcess());

        final ProcessInstance p1 = getProcessAPI().startProcess(callingProcessDef.getId());
        final ActivityInstance userTask = waitForUserTaskAndExecuteAndGetIt(p1, "step1", john);
        waitForProcessToFinish(p1);
        waitForArchivedActivity(userTask.getId(), TestStates.NORMAL_FINAL);
        final ArchivedActivityInstance archivedUserTask = getProcessAPI().getArchivedActivityInstance(userTask.getId());
        assertEquals("My Description", archivedUserTask.getDescription());
        assertEquals("My Display Description", archivedUserTask.getDisplayDescription());
        assertEquals("My Display Name", archivedUserTask.getDisplayName());
        assertEquals("step1", archivedUserTask.getName());
        assertEquals(archivedUserTask.getParentContainerId(), userTask.getParentContainerId());
        assertEquals(archivedUserTask.getRootContainerId(), userTask.getRootContainerId());
        assertEquals(archivedUserTask.getFlownodeDefinitionId(), userTask.getFlownodeDefinitionId());
        assertEquals(archivedUserTask.getType(), userTask.getType());
        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(callingProcessDef);
    }

    @Test
    public void getArchivedFlowNodeInstance() throws Exception {
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, "bpm");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessToDelete", "1.0");
        processDefinitionBuilder.addActor("actor");
        processDefinitionBuilder.addUserTask("step1", "actor").addDescription("My Description")
                .addDisplayName(new ExpressionBuilder().createConstantStringExpression("My Display Name"))
                .addDisplayDescriptionAfterCompletion(new ExpressionBuilder().createConstantStringExpression("My Display Description"));
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, "actor", john);
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final long userTaskId = waitForUserTaskAndExecuteIt(p1, "step1", john);
        waitForProcessToFinish(p1);
        final ArchivedActivityInstance archivedUserTask = getProcessAPI().getArchivedActivityInstance(userTaskId);
        assertEquals(archivedUserTask, getProcessAPI().getArchivedFlowNodeInstance(archivedUserTask.getId()));
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = ArchivedFlowNodeInstanceNotFoundException.class)
    public void getArchivedFlowNodeInstanceNotFound() throws ArchivedFlowNodeInstanceNotFoundException {
        getProcessAPI().getArchivedFlowNodeInstance(123456789L);
    }

}
