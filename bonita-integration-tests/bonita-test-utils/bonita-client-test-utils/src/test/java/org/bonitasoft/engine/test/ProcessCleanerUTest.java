/*
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
 */

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.bpm.category.impl.CategoryImpl;
import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.comment.impl.ArchivedCommentImpl;
import org.bonitasoft.engine.bpm.comment.impl.CommentImpl;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedAutomaticTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.AutomaticTaskInstanceImpl;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.impl.internal.ArchivedProcessInstanceImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDeploymentInfoImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessInstanceImpl;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisorSearchDescriptor;
import org.bonitasoft.engine.bpm.supervisor.impl.ProcessSupervisorImpl;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * @author mazourd
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientEventUtil.class)
public class ProcessCleanerUTest {

    @Mock
    private ProcessAPI processAPI;

    @InjectMocks
    private APITestProcessCleanerImpl ProcessCleaner;
    private APITestProcessCleanerImpl spyProcessCleaner;

    @Before
    public void setUp() throws Exception {
        spyProcessCleaner = spy(ProcessCleaner);
        given(spyProcessCleaner.getProcessAPI()).willReturn(processAPI);
    }

    @Test
    public void checkCategories_should_return_messages() throws DeletionException {
        List<Category> listCategory = new ArrayList<>();
        listCategory.add(new CategoryImpl(25, "dummy1"));
        listCategory.add(new CategoryImpl(27, "dummy2"));
        doReturn(listCategory).when(processAPI).getCategories(0, 5000, CategoryCriterion.NAME_ASC);
        doReturn(2L).when(processAPI).getNumberOfCategories();
        List<String> list = spyProcessCleaner.checkCategories();
        assertThat(list != null);
        assertThat(!list.isEmpty());
    }

    @Test
    public void checkFlowNodesAreDeleted_should_return_messages() throws SearchException {
        List<FlowNodeInstance> flowNodeInstances = new ArrayList<>();
        FlowNodeInstance flowNode1 = new AutomaticTaskInstanceImpl("dummy1", 32L);
        FlowNodeInstance flowNode2 = new AutomaticTaskInstanceImpl("dummy2", 33L);
        SearchResult<FlowNodeInstance> searchResult = new SearchResultImpl<>(2, flowNodeInstances);
        flowNodeInstances.add(flowNode1);
        flowNodeInstances.add(flowNode2);
        doReturn(searchResult).when(processAPI).searchFlowNodeInstances(new SearchOptionsBuilder(0, 1000).done());
        List<String> list = spyProcessCleaner.checkFlowNodesAreDeleted();
        assertThat(list != null);
        assertThat(!list.isEmpty());
    }

    @Test
    public void checkArchivedFlowNodesAreDelete_should_return_messages() throws SearchException {
        List<ArchivedFlowNodeInstance> flowNodeInstances = new ArrayList<>();
        ArchivedFlowNodeInstance flowNode1 = new ArchivedAutomaticTaskInstanceImpl("dummy1");
        ArchivedFlowNodeInstance flowNode2 = new ArchivedAutomaticTaskInstanceImpl("dummy2");
        SearchResult<ArchivedFlowNodeInstance> searchResult = new SearchResultImpl<>(2, flowNodeInstances);
        flowNodeInstances.add(flowNode1);
        flowNodeInstances.add(flowNode2);
        doReturn(searchResult).when(processAPI).searchArchivedFlowNodeInstances(new SearchOptionsBuilder(0, 1000).done());
        List<String> list = spyProcessCleaner.checkArchivedFlowNodesAreDeleted();
        assertThat(list != null);
        assertThat(!list.isEmpty());
    }

    @Test
    public void checkCommentsAreDeleted_should_return_messages() throws SearchException {
        List<Comment> Comments = new ArrayList<>();
        CommentImpl comment1 = new CommentImpl();
        CommentImpl comment2 = new CommentImpl();
        Comments.add(comment1);
        Comments.add(comment2);
        SearchResult<Comment> searchResult = new SearchResultImpl<>(2, Comments);
        doReturn(searchResult).when(processAPI).searchComments(new SearchOptionsBuilder(0, 1000).done());
        List<String> list = spyProcessCleaner.checkCommentsAreDeleted();
        assertThat(list != null);
        assertThat(!list.isEmpty());
    }

    @Test
    public void checkArchivedCommentsAreDeleted_should_return_messages() throws SearchException {
        List<ArchivedComment> Comments = new ArrayList<>();
        ArchivedComment comment1 = new ArchivedCommentImpl("lalaal");
        ArchivedComment comment2 = new ArchivedCommentImpl("lalalala");
        Comments.add(comment1);
        Comments.add(comment2);
        SearchResult<ArchivedComment> searchResult = new SearchResultImpl<>(2, Comments);
        doReturn(searchResult).when(processAPI).searchArchivedComments(new SearchOptionsBuilder(0, 1000).done());
        List<String> list = spyProcessCleaner.checkArchivedCommentsAreDeleted();
        assertThat(list != null);
        assertThat(!list.isEmpty());
    }

    @Test
    public void checkProcessDefinitionsAreDeleted_should_return_messages() throws BonitaException {
        List<ProcessDeploymentInfo> Comments = new ArrayList<>();
        ProcessDeploymentInfo deploymentInfo1 = new ProcessDeploymentInfoImpl(25L, 25L, "dummy1", "1.0", "random description", new Date(45L), 45L,
                ActivationState.ENABLED, ConfigurationState.RESOLVED, "random1", new Date(45L), "random2", "random3");
        ProcessDeploymentInfo deploymentInfo2 = new ProcessDeploymentInfoImpl(26L, 26L, "dummy2", "1.0", "random description", new Date(45L), 45L,
                ActivationState.DISABLED, ConfigurationState.UNRESOLVED, "random1", new Date(45L), "random2", "random3");
        Comments.add(deploymentInfo1);
        Comments.add(deploymentInfo2);
        doReturn(Comments).when(processAPI).getProcessDeploymentInfos(0, 200, ProcessDeploymentInfoCriterion.DEFAULT);
        List<String> list = spyProcessCleaner.checkProcessDefinitionsAreDeleted();
        assertThat(list != null);
        assertThat(!list.isEmpty());
    }

    @Test
    public void checkSupervisorsAreDeleted_should_return_messages() throws SearchException, DeletionException {
        List<ProcessSupervisor> supervisors = new ArrayList<>();
        ProcessSupervisor supervisor1 = new ProcessSupervisorImpl();
        ProcessSupervisor supervisor2 = new ProcessSupervisorImpl();
        supervisors.add(supervisor1);
        supervisors.add(supervisor2);
        SearchResult<ProcessSupervisor> searchResult = new SearchResultImpl<>(2, supervisors);
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 200);
        searchOptionsBuilder.sort(ProcessSupervisorSearchDescriptor.ID, Order.ASC);
        doReturn(searchResult).when(processAPI).searchProcessSupervisors(searchOptionsBuilder.done());
        List<String> list = spyProcessCleaner.checkSupervisorsAreDeleted();
        assertThat(list != null);
        assertThat(!list.isEmpty());
    }

    @Test
    public void checkArchivedProcessIntancesAreDeleted_should_return_messages() throws DeletionException {
        List<ArchivedProcessInstance> processInstances = new ArrayList<>();
        ArchivedProcessInstance archivedProcessInstance1 = new ArchivedProcessInstanceImpl("dummy1");
        ArchivedProcessInstance archivedProcessInstance2 = new ArchivedProcessInstanceImpl("dummy2");
        processInstances.add(archivedProcessInstance1);
        processInstances.add(archivedProcessInstance2);
        doReturn(processInstances).when(processAPI).getArchivedProcessInstances(0, 1000, ProcessInstanceCriterion.DEFAULT);
        List<String> list = spyProcessCleaner.checkArchivedProcessIntancesAreDeleted();
        assertThat(list != null);
        assertThat(!list.isEmpty());
    }

    @Test
    public void checkProcessIntancesAreDeleted_should_return_messages() throws DeletionException {
        List<ProcessInstance> processInstances = new ArrayList<>();
        ProcessInstance processInstance1 = new ProcessInstanceImpl("dummy1");
        ProcessInstance processInstance2 = new ProcessInstanceImpl("dummy2");
        processInstances.add(processInstance1);
        processInstances.add(processInstance2);
        doReturn(processInstances).when(processAPI).getProcessInstances(0, 1000, ProcessInstanceCriterion.DEFAULT);
        List<String> list = spyProcessCleaner.checkProcessIntancesAreDeleted();
        assertThat(list != null);
        assertThat(!list.isEmpty());
    }

}
