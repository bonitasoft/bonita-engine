/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bpm.process;

import static java.util.Arrays.asList;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.bonitasoft.engine.data.instance.api.DataInstanceContainer.ACTIVITY_INSTANCE;
import static org.bonitasoft.engine.data.instance.api.DataInstanceContainer.PROCESS_INSTANCE;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.core.contract.data.SContractDataNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.CommonAPILocalIT;
import org.junit.Test;

/**
 * Verify that api methods delete process instances and all its elements
 */
public class DeleteProcessInstancesIT extends CommonAPILocalIT {

    @Test
    public void should_delete_complete_archived_process_instances() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        User user = createUser("deleteProcessInstanceIT", "bpm");
        ProcessDefinition mainProcess = createMainProcessDefinition();
        ProcessDefinition sub1 = createSubProcessDefinition1();
        ProcessDefinition sub2 = createSubProcessDefinition2();

        List<Long> processInstances = new ArrayList<>();
        List<Long> userTaskInstances = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            long id = getProcessAPI().startProcessWithInputs(mainProcess.getId(),
                    Collections.singletonMap("simpleInput1", "singleInputValue")).getId();
            long userTask1 = waitForUserTask(id, "userTask1");
            userTaskInstances.add(userTask1);
            getProcessAPI().assignUserTask(userTask1, user.getId());
            getProcessAPI().executeUserTask(userTask1,
                    Collections.singletonMap("simpleInputTask", "simpleInputTaskValue"));
            waitForProcessToFinish(id);
            processInstances.add(id);
        }

        List<SFlowNodeInstance> allFlowNodesBeforeDelete = getAllFlowNodes();
        List<SAFlowNodeInstance> allArchFlowNodesBeforeDelete = getAllArchFlowNodes();
        List<SAProcessInstance> allArchProcessInstancesBeforeDelete = getAllProcessInstances();

        getProcessAPI().deleteArchivedProcessInstancesInAllStates(processInstances);

        getTenantAccessor().getUserTransactionService().executeInTransaction((Callable<Void>) () -> {
            for (Long userTaskInstance : userTaskInstances) {
                try {
                    getTenantAccessor().getContractDataService().getArchivedUserTaskDataValue(userTaskInstance,
                            inputName());
                    fail("should have deleted archived contract data on activity instance");
                } catch (SContractDataNotFoundException e) {
                    //ok
                }
            }
            for (Long processInstance : processInstances) {
                try {
                    getTenantAccessor().getContractDataService().getArchivedProcessDataValue(processInstance,
                            "simpleInput1");
                    fail("should have deleted archived contract data on process instance");
                } catch (SContractDataNotFoundException e) {
                    //ok
                }
            }
            assertSoftly((soft) -> {
                try {
                    soft.assertThat(allFlowNodesBeforeDelete).isEmpty();
                    soft.assertThat(searchAllArchProcessInstances()).isEmpty();
                    soft.assertThat(searchAllArchFlowNodes()).isEmpty();
                    soft.assertThat(
                            getTenantAccessor().getCommentService().searchArchivedComments(new QueryOptions(0, 1000)))
                            .isEmpty();
                    soft.assertThat(getTenantAccessor().getConnectorInstanceService().searchArchivedConnectorInstance(
                            new QueryOptions(0, 100, SAConnectorInstance.class, null, null),
                            getTenantAccessor().getReadPersistenceService())).isEmpty();
                    soft.assertThat(getTenantAccessor().getDocumentService()
                            .getNumberOfArchivedDocuments(new QueryOptions(0, 100))).isEqualTo(0);
                    for (SAFlowNodeInstance flowNodeInstance : allArchFlowNodesBeforeDelete) {
                        soft.assertThat(getTenantAccessor().getDataInstanceService().getLocalSADataInstances(
                                flowNodeInstance.getSourceObjectId(), ACTIVITY_INSTANCE.toString(), 0, 1)).isEmpty();
                    }
                    for (SAProcessInstance processInstance : allArchProcessInstancesBeforeDelete) {
                        soft.assertThat(getTenantAccessor().getDataInstanceService().getLocalSADataInstances(
                                processInstance.getSourceObjectId(), PROCESS_INSTANCE.toString(), 0, 1)).isEmpty();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            return null;
        });
        disableAndDeleteProcess(asList(mainProcess, sub1, sub2));
    }

    @Test
    public void should_delete_process_instance_currently_executing() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        User user = createUser("deleteProcessInstanceIT", "bpm");
        ProcessDefinition mainProcess = createMainProcessDefinition();
        ProcessDefinition sub1 = createSubProcessDefinition1();
        ProcessDefinition sub2 = createSubProcessDefinitionWithUserTask(user);

        long id = getProcessAPI().startProcessWithInputs(mainProcess.getId(),
                Collections.singletonMap("simpleInput1", "singleInputValue")).getId();
        waitForUserTask(id, "userTask1");
        waitForUserTask("taskOfSubProcess");
        waitForUserTask("taskOfSubProcess");

        getProcessAPI().deleteProcessInstance(id);

        assertSoftly((soft) -> {
            try {
                soft.assertThat(getAllFlowNodes()).isEmpty();
                soft.assertThat(getAllArchFlowNodes()).isEmpty();
                soft.assertThat(getAllProcessInstances()).isEmpty();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        disableAndDeleteProcess(asList(mainProcess, sub1, sub2));
    }

    protected String inputName() {
        return "simpleInputTask";
    }

    protected List<SAProcessInstance> getAllProcessInstances() throws Exception {
        return getTenantAccessor()
                .getUserTransactionService().executeInTransaction(this::searchAllArchProcessInstances);
    }

    protected List<SAFlowNodeInstance> getAllArchFlowNodes() throws Exception {
        return getTenantAccessor().getUserTransactionService()
                .executeInTransaction(this::searchAllArchFlowNodes);
    }

    protected List<SFlowNodeInstance> getAllFlowNodes() throws Exception {
        return getTenantAccessor().getUserTransactionService()
                .executeInTransaction(this::searchAllFlowNodes);
    }

    protected ProcessDefinition createMainProcessDefinition() throws Exception {
        ProcessDefinitionBuilder mainProcessBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("mainProcess", "1.0");
        mainProcessBuilder.addContract().addInput("simpleInput1", Type.TEXT, "a simple input");
        mainProcessBuilder.addActor("actor");
        mainProcessBuilder.addUserTask("userTask1", "actor").addContract().addInput("simpleInputTask", Type.TEXT,
                "a simple task input");
        ActorMapping actorMapping = new ActorMapping();
        Actor actor = new Actor("actor");
        actorMapping.addActor(actor);
        actor.addUser("deleteProcessInstanceIT");
        mainProcessBuilder
                .addDocumentDefinition("myDoc").addInitialValue(docValueExpr())
                .addStartEvent("start1")
                .addConnector("connector1", "myConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addAutomaticTask("autoWithConnector")
                .addConnector("connector1", "myConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addAutomaticTask("autoWithData").addShortTextData("activityData", s("activityDataValue"))
                .addCallActivity("call1", s("subProcess"), s("1.0"));
        mainProcessBuilder.addCallActivity("call2", s("subProcess"), s("2.0"))
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(2));
        return deployAndEnableProcess(barWithConnector(mainProcessBuilder
                .getProcess()).setActorMapping(actorMapping).done());
    }

    protected ProcessDefinition createSubProcessDefinition1() throws Exception {
        return getProcessAPI().deployAndEnableProcess(barWithConnector(new ProcessDefinitionBuilder()
                .createNewInstance("subProcess", "1.0")
                .addDocumentDefinition("myDoc").addInitialValue(docValueExpr())
                .addStartEvent("start1")
                .addConnector("connector1", "myConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addAutomaticTask("autoWithConnector")
                .addConnector("connector1", "myConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addAutomaticTask("autoWithData").addShortTextData("activityData", s("activityDataValue"))
                .addCallActivity("sub2", s("subProcess"), s("2.0")).getProcess()).done());
    }

    protected ProcessDefinition createSubProcessDefinition2() throws Exception {
        return getProcessAPI().deployAndEnableProcess(barWithConnector(new ProcessDefinitionBuilder()
                .createNewInstance("subProcess", "2.0")
                .addDocumentDefinition("myDoc").addInitialValue(docValueExpr())
                .addStartEvent("start1")
                .addConnector("connector1", "myConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addAutomaticTask("autoWithConnector")
                .addConnector("connector1", "myConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addAutomaticTask("autoWithData").addShortTextData("activityData", s("activityDataValue")).getProcess())
                        .done());
    }

    protected ProcessDefinition createSubProcessDefinitionWithUserTask(User user) throws Exception {
        return deployAndEnableProcessWithActor(new ProcessDefinitionBuilder()
                .createNewInstance("subProcess", "2.0")
                .addActor("actor")
                .addStartEvent("start1")
                .addUserTask("taskOfSubProcess", "actor")
                .addTransition("start1", "taskOfSubProcess").getProcess(),
                "actor",
                user);
    }

    private List<SAProcessInstance> searchAllArchProcessInstances() throws SBonitaReadException {
        return getTenantAccessor().getProcessInstanceService()
                .searchArchivedProcessInstances(new QueryOptions(0, 1000));
    }

    private List<SAFlowNodeInstance> searchAllArchFlowNodes()
            throws org.bonitasoft.engine.persistence.SBonitaReadException {
        return getTenantAccessor().getActivityInstanceService()
                .searchArchivedFlowNodeInstances(SAFlowNodeInstance.class, new QueryOptions(0, 1000));
    }

    private List<SFlowNodeInstance> searchAllFlowNodes()
            throws org.bonitasoft.engine.persistence.SBonitaReadException {
        return getTenantAccessor().getActivityInstanceService()
                .searchFlowNodeInstances(SFlowNodeInstance.class, new QueryOptions(0, 1000));
    }

    private Expression docValueExpr() throws InvalidExpressionException {
        return new ExpressionBuilder().createGroovyScriptExpression("docValue",
                "new org.bonitasoft.engine.bpm.document.DocumentValue(\"hello3\".getBytes(),\"plain/text\",\"file1.txt\")",
                DocumentValue.class.getName());
    }

    private BusinessArchiveBuilder barWithConnector(DesignProcessDefinition process) throws Exception {
        byte[] connectorImplementationFile = BuildTestUtil.buildConnectorImplementationFile("myConnector", "1.0",
                "impl1", "1.0", AddCommentConnector.class.getName());
        return new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(process)
                .addConnectorImplementation(new BarResource("connector.impl", connectorImplementationFile));
    }

    private Expression s(String s) throws InvalidExpressionException {
        return new ExpressionBuilder().createConstantStringExpression(s);
    }

}
