/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.bpm.flownode.ArchivedProcessInstancesSearchDescriptor;
import com.bonitasoft.engine.bpm.process.Index;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor;
import com.bonitasoft.engine.bpm.process.impl.ProcessInstanceUpdater;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchProcessInstanceTest extends CommonAPISPIT {

    private User user;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(user);
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser("jane", "bpm");
    }

    @Test
    public void searchOpenProcessInstancesFromStringIndex1AndUpdateIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance1, "step2");
        getProcessAPI().startProcess(processDefinition.getId());

        final ProcessInstanceUpdater updateDescriptor = new ProcessInstanceUpdater();
        updateDescriptor.setStringIndex1("metsassa");
        ProcessInstance processInstance = getProcessAPI().updateProcessInstance(processInstance1.getId(), updateDescriptor);
        assertEquals("metsassa", processInstance.getStringIndex1());

        processInstance = getProcessAPI().updateProcessInstanceIndex(processInstance1.getId(), Index.FIRST, "metsassa1");
        assertEquals("metsassa1", processInstance.getStringIndex1());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.STRING_INDEX_1, "metsassa1");

        final SearchResult<ProcessInstance> searchOpenProcessInstances = getProcessAPI().searchOpenProcessInstances(builder.done());
        assertEquals(1, searchOpenProcessInstances.getCount());
        final List<ProcessInstance> instances = searchOpenProcessInstances.getResult();
        assertEquals(processInstance1, instances.get(0));

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchOpenProcessInstancesFromStringIndex2AndUpdateIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        addUserToFirstActorOfProcess(1, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance1, "step2");
        getProcessAPI().startProcess(processDefinition.getId());

        final ProcessInstanceUpdater updateDescriptor = new ProcessInstanceUpdater();
        updateDescriptor.setStringIndex2("metsassa");
        ProcessInstance processInstance = getProcessAPI().updateProcessInstance(processInstance1.getId(), updateDescriptor);
        assertEquals("metsassa", processInstance.getStringIndex2());

        processInstance = getProcessAPI().updateProcessInstanceIndex(processInstance1.getId(), Index.SECOND, "metsassa2");
        assertEquals("metsassa2", processInstance.getStringIndex2());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.STRING_INDEX_2, "metsassa2");

        final SearchResult<ProcessInstance> searchOpenProcessInstances = getProcessAPI().searchOpenProcessInstances(builder.done());
        assertEquals(1, searchOpenProcessInstances.getCount());
        final List<ProcessInstance> instances = searchOpenProcessInstances.getResult();
        assertEquals(processInstance1, instances.get(0));

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchOpenProcessInstancesFromStringIndex3AndUpdateIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance1, "step2");
        getProcessAPI().startProcess(processDefinition.getId());

        final ProcessInstanceUpdater updateDescriptor = new ProcessInstanceUpdater();
        updateDescriptor.setStringIndex3("metsassa");
        ProcessInstance processInstance = getProcessAPI().updateProcessInstance(processInstance1.getId(), updateDescriptor);
        assertEquals("metsassa", processInstance.getStringIndex3());

        processInstance = getProcessAPI().updateProcessInstanceIndex(processInstance1.getId(), Index.THIRD, "metsassa3");
        assertEquals("metsassa3", processInstance.getStringIndex3());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.STRING_INDEX_3, "metsassa3");

        final SearchResult<ProcessInstance> searchOpenProcessInstances = getProcessAPI().searchOpenProcessInstances(builder.done());
        assertEquals(1, searchOpenProcessInstances.getCount());
        final List<ProcessInstance> instances = searchOpenProcessInstances.getResult();
        assertEquals(processInstance1, instances.get(0));

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchOpenProcessInstancesFromStringIndex4AndUpdateIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance1, "step2");
        getProcessAPI().startProcess(processDefinition.getId());

        final ProcessInstanceUpdater updateDescriptor = new ProcessInstanceUpdater();
        updateDescriptor.setStringIndex4("metsassa");
        ProcessInstance processInstance = getProcessAPI().updateProcessInstance(processInstance1.getId(), updateDescriptor);
        assertEquals("metsassa", processInstance.getStringIndex4());

        processInstance = getProcessAPI().updateProcessInstanceIndex(processInstance1.getId(), Index.FOURTH, "metsassa4");
        assertEquals("metsassa4", processInstance.getStringIndex4());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.STRING_INDEX_4, "metsassa4");

        final SearchResult<ProcessInstance> searchOpenProcessInstances = getProcessAPI().searchOpenProcessInstances(builder.done());
        assertEquals(1, searchOpenProcessInstances.getCount());
        final List<ProcessInstance> instances = searchOpenProcessInstances.getResult();
        assertEquals(processInstance1, instances.get(0));

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchOpenProcessInstancesFromStringIndex5AndUpdateIt() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance1, "step2");
        getProcessAPI().startProcess(processDefinition.getId());

        final ProcessInstanceUpdater updateDescriptor = new ProcessInstanceUpdater();
        updateDescriptor.setStringIndex5("metsassa");
        ProcessInstance processInstance = getProcessAPI().updateProcessInstance(processInstance1.getId(), updateDescriptor);
        assertEquals("metsassa", processInstance.getStringIndex5());

        processInstance = getProcessAPI().updateProcessInstanceIndex(processInstance1.getId(), Index.FIFTH, "metsassa5");
        assertEquals("metsassa5", processInstance.getStringIndex5());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProcessInstanceSearchDescriptor.STRING_INDEX_5, "metsassa5");

        final SearchResult<ProcessInstance> searchOpenProcessInstances = getProcessAPI().searchOpenProcessInstances(builder.done());
        assertEquals(1, searchOpenProcessInstances.getCount());
        final List<ProcessInstance> instances = searchOpenProcessInstances.getResult();
        assertEquals(processInstance1, instances.get(0));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class, ArchivedProcessInstancesSearchDescriptor.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Archived",
            "Process Instances" }, jira = "ENGINE-998")
    @Test
    public void searchTermOnIndexOnProcessInstances() throws Exception {
        final User user1 = createUser("john1", "bpm");
        final User user2 = createUser("john2", "bpm");
        final User user3 = createUser("john3", "bpm");
        final User user4 = createUser("john4", "bpm");

        final DesignProcessDefinition designProcessDefinition1 = createProcessDefinition("3", true, "value1", "value2", "value3", "value4", "value5")
                .done();
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition1, ACTOR_NAME, user1);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition1.getId());
        waitForUserTask(processInstance1, "step1");
        logoutOnTenant();

        loginOnDefaultTenantWith("john1", "bpm");
        final DesignProcessDefinition designProcessDefinition2 = createProcessDefinition("2", true, "value2", "value4", "value1", "value5", "value3")
                .done();
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(designProcessDefinition2, ACTOR_NAME, user2);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());
        waitForUserTask(processInstance2, "step1");
        logoutOnTenant();

        loginOnDefaultTenantWith("john3", "bpm");
        final DesignProcessDefinition designProcessDefinition3 = createProcessDefinition("5", true, "value4", "value3", "value5", "value2", "value1")
                .done();
        final ProcessDefinition processDefinition3 = deployAndEnableProcessWithActor(designProcessDefinition3, ACTOR_NAME, user3);
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition3.getId());
        waitForUserTask(processInstance3, "step1");
        logoutOnTenant();

        loginOnDefaultTenantWith("john2", "bpm");
        final DesignProcessDefinition designProcessDefinition4 = createProcessDefinition("4", true, "value5", "value1", "value4", "value3", "value2")
                .done();
        final ProcessDefinition processDefinition4 = deployAndEnableProcessWithActor(designProcessDefinition4, ACTOR_NAME, user4);
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(processDefinition4.getId());
        waitForUserTask(processInstance4, "step1");
        logoutOnTenant();

        loginOnDefaultTenantWith("john4", "bpm");
        final DesignProcessDefinition designProcessDefinition5 = createProcessDefinition("1", true, "value3", "value5", "value2", "value1", "value4")
                .done();
        final ProcessDefinition processDefinition5 = deployAndEnableProcessWithActor(designProcessDefinition5, ACTOR_NAME, user1);
        final ProcessInstance processInstance5 = getProcessAPI().startProcess(processDefinition5.getId());
        waitForUserTask(processInstance5, "step1");

        // Search term for STRING_INDEX
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor.ID, Order.ASC);
        searchOptionsBuilder.searchTerm("value1");
        final List<ProcessInstance> archivedProcessInstances = getProcessAPI().searchOpenProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getId());

        disableAndDeleteProcess(processDefinition1, processDefinition2, processDefinition3, processDefinition4, processDefinition5);
        deleteUsers(user1, user2, user3, user4);
    }

    @Cover(classes = { ProcessAPI.class, ArchivedProcessInstancesSearchDescriptor.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Archived",
            "Process Instances" }, jira = "ENGINE-998")
    @Test
    public void searchArchivedProcessInstances() throws Exception {
        final User user1 = createUser("john1", "bpm");
        final User user2 = createUser("john2", "bpm");
        final User user3 = createUser("john3", "bpm");
        final User user4 = createUser("john4", "bpm");

        final DesignProcessDefinition designProcessDefinition1 = createProcessDefinition("3", false, "value1", "value2", "value3", "value4", "value5")
                .done();
        // final ProcessDefinition processDefinition1 = deployAndEnableWithActor(designProcessDefinition1, delivery, user1);
        final ProcessDefinition processDefinition1 = deployAndEnableProcess(designProcessDefinition1);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition1.getId());
        waitForProcessToFinish(processInstance1);
        logoutOnTenant();

        loginOnDefaultTenantWith("john1", "bpm");
        final DesignProcessDefinition designProcessDefinition2 = createProcessDefinition("2", false, "value2", "value4", "value1", "value5", "value3")
                .done();
        final ProcessDefinition processDefinition2 = deployAndEnableProcess(designProcessDefinition2);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());
        waitForProcessToFinish(processInstance2);
        logoutOnTenant();

        loginOnDefaultTenantWith("john3", "bpm");
        final DesignProcessDefinition designProcessDefinition3 = createProcessDefinition("5", false, "value4", "value3", "value5", "value2", "value1")
                .done();
        final ProcessDefinition processDefinition3 = deployAndEnableProcess(designProcessDefinition3);
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition3.getId());
        waitForProcessToFinish(processInstance3);
        logoutOnTenant();

        loginOnDefaultTenantWith("john2", "bpm");
        final DesignProcessDefinition designProcessDefinition4 = createProcessDefinition("4", false, "value5", "value1", "value4", "value3", "value2")
                .done();
        final ProcessDefinition processDefinition4 = deployAndEnableProcess(designProcessDefinition4);
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(processDefinition4.getId());
        waitForProcessToFinish(processInstance4);
        logoutOnTenant();

        loginOnDefaultTenantWith("john4", "bpm");
        final DesignProcessDefinition designProcessDefinition5 = createProcessDefinition("1", false, "value3", "value5", "value2", "value1", "value4")
                .done();
        final ProcessDefinition processDefinition5 = deployAndEnableProcess(designProcessDefinition5);
        final ProcessInstance processInstance5 = getProcessAPI().startProcess(processDefinition5.getId());
        waitForProcessToFinish(processInstance5);

        // Order by STRING_INDEX_1
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_1, Order.ASC);
        List<ArchivedProcessInstance> archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STRING_INDEX_2
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_2, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STRING_INDEX_3
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_3, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STRING_INDEX_4
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_4, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STRING_INDEX_5
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_5, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Search term for STRING_INDEX
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor.ID, Order.ASC);
        searchOptionsBuilder.searchTerm("value1");
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        disableAndDeleteProcess(processDefinition1, processDefinition2, processDefinition3, processDefinition4, processDefinition5);
        deleteUsers(user1, user2, user3, user4);
    }

    private ProcessDefinitionBuilderExt createProcessDefinition(final String processName, final boolean withUserTask, final String stringIndex1,
            final String stringIndex2, final String stringIndex3, final String stringIndex4, final String stringIndex5)
            throws InvalidExpressionException {
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(processName, "17.3");
        designProcessDefinition.addDescription("Delivery all day and night long");
        if (withUserTask) {
            designProcessDefinition.addActor(ACTOR_NAME);
            designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        } else {
            designProcessDefinition.addAutomaticTask("step1");
        }

        if (stringIndex1 != null && stringIndex1 != "") {
            designProcessDefinition.setStringIndex(1, "label1", new ExpressionBuilder().createConstantStringExpression(stringIndex1));
        }
        if (stringIndex2 != null && stringIndex2 != "") {
            designProcessDefinition.setStringIndex(2, "label2", new ExpressionBuilder().createConstantStringExpression(stringIndex2));
        }
        if (stringIndex3 != null && stringIndex3 != "") {
            designProcessDefinition.setStringIndex(3, "label3", new ExpressionBuilder().createConstantStringExpression(stringIndex3));
        }
        if (stringIndex4 != null && stringIndex4 != "") {
            designProcessDefinition.setStringIndex(4, "label4", new ExpressionBuilder().createConstantStringExpression(stringIndex4));
        }
        if (stringIndex5 != null && stringIndex5 != "") {
            designProcessDefinition.setStringIndex(5, "label5", new ExpressionBuilder().createConstantStringExpression(stringIndex5));
        }
        return designProcessDefinition;
    }

}
