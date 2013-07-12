/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessAPIImplTest {

    @InjectMocks
    private ProcessAPIImpl processAPIImpl;

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchHumanTaskInstances(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchHumanTaskInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deleteProcess(long)}.
     */
    @Test
    public final void deleteProcess() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deleteProcessInstancesFromProcessDefinition(long, org.bonitasoft.engine.service.TenantServiceAccessor)}
     * .
     */
    @Test
    public final void deleteProcessInstancesFromProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deleteProcesses(java.util.List)}.
     */
    @Test
    public final void deleteProcesses() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deployAndEnableProcess(org.bonitasoft.engine.bpm.process.DesignProcessDefinition)}.
     */
    @Test
    public final void deployAndEnableProcessDesignProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deployAndEnableProcess(org.bonitasoft.engine.bpm.bar.BusinessArchive)}.
     */
    @Test
    public final void deployAndEnableProcessBusinessArchive() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deploy(org.bonitasoft.engine.bpm.process.DesignProcessDefinition)}.
     */
    @Test
    public final void deployDesignProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deploy(org.bonitasoft.engine.bpm.bar.BusinessArchive)}.
     */
    @Test
    public final void deployBusinessArchive() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#importActorMapping(long, byte[])}.
     */
    @Test
    public final void importActorMappingLongByteArray() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#exportBarProcessContentUnderHome(long)}.
     */
    @Test
    public final void exportBarProcessContentUnderHome() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#unzipBar(org.bonitasoft.engine.bpm.bar.BusinessArchive, org.bonitasoft.engine.core.process.definition.model.SProcessDefinition, long)}
     * .
     */
    @Test
    public final void unzipBar() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#disableAndDelete(long)}.
     */
    @Test
    public final void disableAndDelete() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#disableProcess(long)}.
     */
    @Test
    public final void disableProcess() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#enableProcess(long)}.
     */
    @Test
    public final void enableProcess() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#executeFlowNode(long)}.
     */
    @Test
    public final void executeFlowNode() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActivities(long, int, int)}.
     */
    @Test
    public final void getActivities() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfProcessDeploymentInfos()}.
     */
    @Test
    public final void getNumberOfProcessDeploymentInfos() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDefinition(long)}.
     */
    @Test
    public final void getProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfo(long)}.
     */
    @Test
    public final void getProcessDeploymentInfo() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessInstance(long)}.
     */
    @Test
    public final void getProcessInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getArchivedProcessInstances(long, int, int)}.
     */
    @Test
    public final void getArchivedProcessInstancesLongIntInt() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getArchivedProcessInstance(long)}.
     */
    @Test
    public final void getArchivedProcessInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getFinalArchivedProcessInstance(long)}.
     */
    @Test
    public final void getFinalArchivedProcessInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#startProcess(long)}.
     */
    @Test
    public final void startProcessLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#startProcess(long, long)}.
     */
    @Test
    public final void startProcessLongLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfActors(long)}.
     */
    @Test
    public final void getNumberOfActors() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActors(long, int, int, org.bonitasoft.engine.bpm.actor.ActorCriterion)}.
     */
    @Test
    public final void getActors() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActorMembers(long, int, int)}.
     */
    @Test
    public final void getActorMembers() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfActorMembers(long)}.
     */
    @Test
    public final void getNumberOfActorMembers() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfUsersOfActor(long)}.
     */
    @Test
    public final void getNumberOfUsersOfActor() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfRolesOfActor(long)}.
     */
    @Test
    public final void getNumberOfRolesOfActor() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfGroupsOfActor(long)}.
     */
    @Test
    public final void getNumberOfGroupsOfActor() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfMembershipsOfActor(long)}.
     */
    @Test
    public final void getNumberOfMembershipsOfActor() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#updateActor(long, org.bonitasoft.engine.bpm.actor.ActorUpdater)}.
     */
    @Test
    public final void updateActor() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addUserToActor(long, long)}.
     */
    @Test
    public final void addUserToActorLongLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addUserToActor(java.lang.String, org.bonitasoft.engine.bpm.process.ProcessDefinition, long)}.
     */
    @Test
    public final void addUserToActorStringProcessDefinitionLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addGroupToActor(long, long)}.
     */
    @Test
    public final void addGroupToActorLongLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addGroupToActor(java.lang.String, long, org.bonitasoft.engine.bpm.process.ProcessDefinition)}.
     */
    @Test
    public final void addGroupToActorStringLongProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addRoleToActor(long, long)}.
     */
    @Test
    public final void addRoleToActorLongLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addRoleToActor(java.lang.String, org.bonitasoft.engine.bpm.process.ProcessDefinition, long)}.
     */
    @Test
    public final void addRoleToActorStringProcessDefinitionLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addRoleAndGroupToActor(java.lang.String, org.bonitasoft.engine.bpm.process.ProcessDefinition, long, long)}
     * .
     */
    @Test
    public final void addRoleAndGroupToActorStringProcessDefinitionLongLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addRoleAndGroupToActor(long, long, long)}.
     */
    @Test
    public final void addRoleAndGroupToActorLongLongLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#removeActorMember(long)}.
     */
    @Test
    public final void removeActorMember() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActor(long)}.
     */
    @Test
    public final void getActor() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActivityInstance(long)}.
     */
    @Test
    public final void getActivityInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getFlowNodeInstance(long)}.
     */
    @Test
    public final void getFlowNodeInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getAssignedHumanTaskInstances(long, int, int, org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion)}
     * .
     */
    @Test
    public final void getAssignedHumanTaskInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getPendingHumanTaskInstances(long, int, int, org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion)}
     * .
     */
    @Test
    public final void getPendingHumanTaskInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getArchivedActivityInstance(long)}.
     */
    @Test
    public final void getArchivedActivityInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getArchivedFlowNodeInstance(long)}.
     */
    @Test
    public final void getArchivedFlowNodeInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessInstances(int, int, org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion)}.
     */
    @Test
    public final void getProcessInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfProcessInstances()}.
     */
    @Test
    public final void getNumberOfProcessInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchProcessInstances(org.bonitasoft.engine.service.TenantServiceAccessor, org.bonitasoft.engine.search.SearchOptions)}
     * .
     */
    @Test
    public final void searchProcessInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getArchivedProcessInstances(int, int, org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion)}.
     */
    @Test
    public final void getArchivedProcessInstancesIntIntProcessInstanceCriterion() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfArchivedProcessInstances()}.
     */
    @Test
    public final void getNumberOfArchivedProcessInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedProcessInstances(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedProcessInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getOpenActivityInstances(long, int, int, org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion)}
     * .
     */
    @Test
    public final void getOpenActivityInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getArchivedActivityInstances(long, int, int, org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion)}
     * .
     */
    @Test
    public final void getArchivedActivityInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfOpenedActivityInstances(long)}.
     */
    @Test
    public final void getNumberOfOpenedActivityInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#createCategory(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void createCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getCategory(long)}.
     */
    @Test
    public final void getCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfCategories()}.
     */
    @Test
    public final void getNumberOfCategories() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getCategories(int, int, org.bonitasoft.engine.bpm.category.CategoryCriterion)}.
     */
    @Test
    public final void getCategories() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addCategoriesToProcess(long, java.util.List)}.
     */
    @Test
    public final void addCategoriesToProcess() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#removeCategoriesFromProcess(long, java.util.List)}.
     */
    @Test
    public final void removeCategoriesFromProcess() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addProcessDefinitionToCategory(long, long)}.
     */
    @Test
    public final void addProcessDefinitionToCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addProcessDefinitionsToCategory(long, java.util.List)}.
     */
    @Test
    public final void addProcessDefinitionsToCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfCategories(long)}.
     */
    @Test
    public final void getNumberOfCategoriesLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfProcessDefinitionsOfCategory(long)}.
     */
    @Test
    public final void getNumberOfProcessDefinitionsOfCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosOfCategory(long, int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getProcessDeploymentInfosOfCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getCategoriesOfProcessDefinition(long, int, int, org.bonitasoft.engine.bpm.category.CategoryCriterion)}
     * .
     */
    @Test
    public final void getCategoriesOfProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getCategoriesUnrelatedToProcessDefinition(long, int, int, org.bonitasoft.engine.bpm.category.CategoryCriterion)}
     * .
     */
    @Test
    public final void getCategoriesUnrelatedToProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#updateCategory(long, org.bonitasoft.engine.bpm.category.CategoryUpdater)}.
     */
    @Test
    public final void updateCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deleteCategory(long)}.
     */
    @Test
    public final void deleteCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#removeAllProcessDefinitionsFromCategory(long)}.
     */
    @Test
    public final void removeAllProcessDefinitionsFromCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfUncategorizedProcessDefinitions()}.
     */
    @Test
    public final void getNumberOfUncategorizedProcessDefinitions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getUncategorizedProcessDeploymentInfos(int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getUncategorizedProcessDeploymentInfos() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfProcessDeploymentInfosUnrelatedToCategory(long)}.
     */
    @Test
    public final void getNumberOfProcessDeploymentInfosUnrelatedToCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosUnrelatedToCategory(long, int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getProcessDeploymentInfosUnrelatedToCategory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#removeAllCategoriesFromProcessDefinition(long)}.
     */
    @Test
    public final void removeAllCategoriesFromProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getEventInstances(long, int, int, org.bonitasoft.engine.bpm.flownode.EventCriterion)}.
     */
    @Test
    public final void getEventInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#assignUserTask(long, long)}.
     */
    @Test
    public final void assignUserTask() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActivityDataDefinitions(long, java.lang.String, int, int)}.
     */
    @Test
    public final void getActivityDataDefinitions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDataDefinitions(long, int, int)}.
     */
    @Test
    public final void getProcessDataDefinitions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getHumanTaskInstance(long)}.
     */
    @Test
    public final void getHumanTaskInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfAssignedHumanTaskInstances(long)}.
     */
    @Test
    public final void getNumberOfAssignedHumanTaskInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfOpenTasks(java.util.List)}.
     */
    @Test
    public final void getNumberOfOpenTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfPendingHumanTaskInstances(long)}.
     */
    @Test
    public final void getNumberOfPendingHumanTaskInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessResources(long, java.lang.String)}.
     */
    @Test
    public final void getProcessResources() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getLatestProcessDefinitionId(java.lang.String)}.
     */
    @Test
    public final void getLatestProcessDefinitionId() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDataInstances(long, int, int)}.
     */
    @Test
    public final void getProcessDataInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDataInstance(java.lang.String, long)}.
     */
    @Test
    public final void getProcessDataInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#updateProcessDataInstance(java.lang.String, long, java.io.Serializable)}.
     */
    @Test
    public final void updateProcessDataInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActivityDataInstances(long, int, int)}.
     */
    @Test
    public final void getActivityDataInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActivityDataInstance(java.lang.String, long)}.
     */
    @Test
    public final void getActivityDataInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#updateActivityDataInstance(java.lang.String, long, java.io.Serializable)}.
     */
    @Test
    public final void updateActivityDataInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#importActorMapping(long, java.lang.String)}.
     */
    @Test
    public final void importActorMappingLongString() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#exportActorMapping(long)}.
     */
    @Test
    public final void exportActorMapping() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#isInvolvedInProcessInstance(long, long)}.
     */
    @Test
    public final void isInvolvedInProcessInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessInstanceIdFromActivityInstanceId(long)}.
     */
    @Test
    public final void getProcessInstanceIdFromActivityInstanceId() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDefinitionIdFromActivityInstanceId(long)}.
     */
    @Test
    public final void getProcessDefinitionIdFromActivityInstanceId() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDefinitionIdFromProcessInstanceId(long)}.
     */
    @Test
    public final void getProcessDefinitionIdFromProcessInstanceId() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActivityReachedStateDate(long, java.lang.String)}.
     */
    @Test
    public final void getActivityReachedStateDate() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getSupportedStates(org.bonitasoft.engine.bpm.flownode.FlowNodeType)}.
     */
    @Test
    public final void getSupportedStates() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#updateActivityInstanceVariables(long, java.util.Map)}.
     */
    @Test
    public final void updateActivityInstanceVariablesLongMapOfStringSerializable() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#updateActivityInstanceVariables(java.util.List, long, java.util.Map)}.
     */
    @Test
    public final void updateActivityInstanceVariablesListOfOperationLongMapOfStringSerializable() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getOneAssignedUserTaskInstanceOfProcessInstance(long, long)}.
     */
    @Test
    public final void getOneAssignedUserTaskInstanceOfProcessInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getOneAssignedUserTaskInstanceOfProcessDefinition(long, long)}.
     */
    @Test
    public final void getOneAssignedUserTaskInstanceOfProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActivityInstanceState(long)}.
     */
    @Test
    public final void getActivityInstanceState() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#canExecuteTask(long, long)}.
     */
    @Test
    public final void canExecuteTask() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDefinitionId(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void getProcessDefinitionId() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#releaseUserTask(long)}.
     */
    @Test
    public final void releaseUserTask() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#updateProcessDeploymentInfo(long, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoUpdater)}.
     */
    @Test
    public final void updateProcessDeploymentInfo() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getStartableProcessDeploymentInfosForActors(java.util.Set, int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getStartableProcessDeploymentInfosForActors() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#isAllowedToStartProcess(long, java.util.Set)}.
     */
    @Test
    public final void isAllowedToStartProcess() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActorInitiator(long)}.
     */
    @Test
    public final void getActorInitiator() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfActivityDataDefinitions(long, java.lang.String)}.
     */
    @Test
    public final void getNumberOfActivityDataDefinitions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfProcessDataDefinitions(long)}.
     */
    @Test
    public final void getNumberOfProcessDataDefinitions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#startProcess(long, java.util.List, java.util.Map)}.
     */
    @Test
    public final void startProcessLongListOfOperationMapOfStringSerializable() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#startProcess(long, long, java.util.List, java.util.Map)}.
     */
    @Test
    public final void startProcessLongLongListOfOperationMapOfStringSerializable() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfActivityDataInstances(long)}.
     */
    @Test
    public final void getNumberOfActivityDataInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfProcessDataInstances(long)}.
     */
    @Test
    public final void getNumberOfProcessDataInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#executeOperations(org.bonitasoft.engine.core.connector.ConnectorResult, java.util.List, java.util.Map, org.bonitasoft.engine.core.expression.control.model.SExpressionContext, java.lang.ClassLoader, org.bonitasoft.engine.service.TenantServiceAccessor)}
     * .
     */
    @Test
    public final void executeOperations() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#executeConnectorOnProcessDefinition(java.lang.String, java.lang.String, java.util.Map, java.util.Map, long)}
     * .
     */
    @Test
    public final void executeConnectorOnProcessDefinitionStringStringMapOfStringExpressionMapOfStringMapOfStringSerializableLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#executeConnectorOnProcessDefinition(java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.List, java.util.Map, long)}
     * .
     */
    @Test
    public final void executeConnectorOnProcessDefinitionStringStringMapOfStringExpressionMapOfStringMapOfStringSerializableListOfOperationMapOfStringSerializableLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getSerializableResultOfConnector(java.lang.String, org.bonitasoft.engine.core.connector.ConnectorResult, org.bonitasoft.engine.core.connector.ConnectorService)}
     * .
     */
    @Test
    public final void getSerializableResultOfConnector() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#checkConnectorParameters(java.util.Map, java.util.Map)}.
     */
    @Test
    public final void checkConnectorParameters() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#setActivityStateByName(long, java.lang.String)}.
     */
    @Test
    public final void setActivityStateByName() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#setActivityStateById(long, int)}.
     */
    @Test
    public final void setActivityStateById() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#setTaskPriority(long, org.bonitasoft.engine.bpm.flownode.TaskPriority)}.
     */
    @Test
    public final void setTaskPriority() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deleteProcessInstances(long)}.
     */
    @Test
    public final void deleteProcessInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deleteProcessInstance(long)}.
     */
    @Test
    public final void deleteProcessInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchOpenProcessInstances(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchOpenProcessInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchOpenProcessInstancesSupervisedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchOpenProcessInstancesSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchProcessDeploymentInfosStartedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchProcessDeploymentInfosStartedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchProcessDeploymentInfos(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchProcessDeploymentInfosSearchOptions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchProcessDeploymentInfos(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchProcessDeploymentInfosLongSearchOptions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchProcessDeploymentInfosUsersManagedByCanStart(long, org.bonitasoft.engine.search.SearchOptions)}
     * .
     */
    @Test
    public final void searchProcessDeploymentInfosUsersManagedByCanStart() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchProcessDeploymentInfosSupervisedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchProcessDeploymentInfosSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchAssignedTasksSupervisedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchAssignedTasksSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedHumanTasksSupervisedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedHumanTasksSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchProcessSupervisors(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchProcessSupervisors() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#isUserProcessSupervisor(long, long)}.
     */
    @Test
    public final void isUserProcessSupervisor() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deleteSupervisor(long)}.
     */
    @Test
    public final void deleteSupervisorLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#deleteSupervisor(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.Long)}.
     */
    @Test
    public final void deleteSupervisorLongLongLongLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#createProcessSupervisorForUser(long, long)}.
     */
    @Test
    public final void createProcessSupervisorForUser() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#createProcessSupervisorForRole(long, long)}.
     */
    @Test
    public final void createProcessSupervisorForRole() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#createProcessSupervisorForGroup(long, long)}.
     */
    @Test
    public final void createProcessSupervisorForGroup() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#createProcessSupervisorForMembership(long, long, long)}.
     */
    @Test
    public final void createProcessSupervisorForMembership() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchUncategorizedProcessDeploymentInfosUserCanStart(long, org.bonitasoft.engine.search.SearchOptions)}
     * .
     */
    @Test
    public final void searchUncategorizedProcessDeploymentInfosUserCanStart() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedHumanTasksManagedBy(long, org.bonitasoft.engine.search.SearchOptions)}
     * .
     */
    @Test
    public final void searchArchivedHumanTasksManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchOpenProcessInstancesInvolvingUser(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchOpenProcessInstancesInvolvingUser() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchOpenProcessInstancesInvolvingUsersManagedBy(long, org.bonitasoft.engine.search.SearchOptions)}
     * .
     */
    @Test
    public final void searchOpenProcessInstancesInvolvingUsersManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedHumanTasks(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedHumanTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchAssignedTasksManagedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchAssignedTasksManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedProcessInstancesSupervisedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedProcessInstancesSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedProcessInstancesInvolvingUser(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedProcessInstancesInvolvingUser() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchPendingTasksForUser(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchPendingTasksForUser() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchMyAvailableHumanTasks(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchMyAvailableHumanTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchPendingTasksSupervisedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchPendingTasksSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchComments(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchComments() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#addComment(long, java.lang.String)}.
     */
    @Test
    public final void addComment() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getComments(long)}.
     */
    @Test
    public final void getComments() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#attachDocument(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public final void attachDocumentLongStringStringStringString() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#attachDocument(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.bonitasoft.engine.core.process.document.api.ProcessDocumentService, org.bonitasoft.engine.commons.transaction.TransactionExecutor, org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder, long)}
     * .
     */
    @Test
    public final void attachDocumentLongStringStringStringStringProcessDocumentServiceTransactionExecutorSProcessDocumentBuilderLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#attachDocument(long, java.lang.String, java.lang.String, java.lang.String, byte[])}.
     */
    @Test
    public final void attachDocumentLongStringStringStringByteArray() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#attachDocument(long, java.lang.String, java.lang.String, java.lang.String, byte[], org.bonitasoft.engine.core.process.document.api.ProcessDocumentService, org.bonitasoft.engine.commons.transaction.TransactionExecutor, org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder, long)}
     * .
     */
    @Test
    public final void attachDocumentLongStringStringStringByteArrayProcessDocumentServiceTransactionExecutorSProcessDocumentBuilderLong() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#attachNewDocumentVersion(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public final void attachNewDocumentVersionLongStringStringStringString() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#attachNewDocumentVersion(long, java.lang.String, java.lang.String, java.lang.String, byte[])}.
     */
    @Test
    public final void attachNewDocumentVersionLongStringStringStringByteArray() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getDocument(long)}.
     */
    @Test
    public final void getDocument() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getLastVersionOfDocuments(long, int, int, org.bonitasoft.engine.bpm.document.DocumentCriterion)}.
     */
    @Test
    public final void getLastVersionOfDocuments() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getDocumentContent(java.lang.String)}.
     */
    @Test
    public final void getDocumentContent() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getLastDocument(long, java.lang.String)}.
     */
    @Test
    public final void getLastDocument() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfDocuments(long)}.
     */
    @Test
    public final void getNumberOfDocuments() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getDocumentAtProcessInstantiation(long, java.lang.String)}.
     */
    @Test
    public final void getDocumentAtProcessInstantiation() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getDocumentAtActivityInstanceCompletion(long, java.lang.String)}.
     */
    @Test
    public final void getDocumentAtActivityInstanceCompletion() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchPendingTasksManagedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchPendingTasksManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfOverdueOpenTasks(java.util.List)}.
     */
    @Test
    public final void getNumberOfOverdueOpenTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchUncategorizedProcessDeploymentInfos(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchUncategorizedProcessDeploymentInfos() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchCommentsManagedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchCommentsManagedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchCommentsInvolvingUser(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchCommentsInvolvingUser() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getChildrenInstanceIdsOfProcessInstance(long, int, int, org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion)}
     * .
     */
    @Test
    public final void getChildrenInstanceIdsOfProcessInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchUncategorizedProcessDeploymentInfosSupervisedBy(long, org.bonitasoft.engine.search.SearchOptions)}
     * .
     */
    @Test
    public final void searchUncategorizedProcessDeploymentInfosSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosFromIds(java.util.List)}.
     */
    @Test
    public final void getProcessDeploymentInfosFromIds() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getConnectorImplementations(long, int, int, org.bonitasoft.engine.bpm.connector.ConnectorCriterion)}
     * .
     */
    @Test
    public final void getConnectorImplementations() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getNumberOfConnectorImplementations(long)}.
     */
    @Test
    public final void getNumberOfConnectorImplementations() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchActivities(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchActivities() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedFlowNodeInstances(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedFlowNodeInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchFlowNodeInstances(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchFlowNodeInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedActivities(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedActivities() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getConnectorImplementation(long, java.lang.String, java.lang.String)}.
     */
    @Test
    public final void getConnectorImplementation() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#cancelProcessInstance(long)}.
     */
    @Test
    public final void cancelProcessInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#setProcessInstanceState(org.bonitasoft.engine.bpm.process.ProcessInstance, java.lang.String)}.
     */
    @Test
    public final void setProcessInstanceState() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosFromProcessInstanceIds(java.util.List)}.
     */
    @Test
    public final void getProcessDeploymentInfosFromProcessInstanceIds() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchDocuments(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchDocuments() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchDocumentsSupervisedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchDocumentsSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedDocuments(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedDocuments() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedDocumentsSupervisedBy(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedDocumentsSupervisedBy() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#retryTask(long)}.
     */
    @Test
    public final void retryTask() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getArchivedVersionOfProcessDocument(long)}.
     */
    @Test
    public final void getArchivedVersionOfProcessDocument() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getArchivedProcessDocument(long)}.
     */
    @Test
    public final void getArchivedProcessDocument() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedComments(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedComments() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getArchivedComment(long)}.
     */
    @Test
    public final void getArchivedComment() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getActorsFromActorIds(java.util.List)}.
     */
    @Test
    public final void getActorsFromActorIds() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosFromArchivedProcessInstanceIds(java.util.List)}.
     */
    @Test
    public final void getProcessDeploymentInfosFromArchivedProcessInstanceIds() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchPendingHiddenTasks(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchPendingHiddenTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#hideTasks(long, java.lang.Long[])}.
     */
    @Test
    public final void hideTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#unhideTasks(long, java.lang.Long[])}.
     */
    @Test
    public final void unhideTasks() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#evaluateExpressionOnProcessDefinition(org.bonitasoft.engine.expression.Expression, java.util.Map, long)}
     * .
     */
    @Test
    public final void evaluateExpressionOnProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#updateDueDateOfTask(long, java.util.Date)}.
     */
    @Test
    public final void updateDueDateOfTask() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#isTaskHidden(long, long)}.
     */
    @Test
    public final void isTaskHidden() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#countComments(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void countComments() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#countAttachments(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void countAttachments() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#sendSignal(java.lang.String)}.
     */
    @Test
    public final void sendSignal() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#sendMessage(java.lang.String, org.bonitasoft.engine.expression.Expression, org.bonitasoft.engine.expression.Expression, java.util.Map)}
     * .
     */
    @Test
    public final void sendMessageStringExpressionExpressionMapOfExpressionExpression() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#sendMessage(java.lang.String, org.bonitasoft.engine.expression.Expression, org.bonitasoft.engine.expression.Expression, java.util.Map, java.util.Map)}
     * .
     */
    @Test
    public final void sendMessageStringExpressionExpressionMapOfExpressionExpressionMapOfExpressionExpression() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessResolutionProblems(long)}.
     */
    @Test
    public final void getProcessResolutionProblems() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfos(int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getProcessDeploymentInfos() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosWithActorOnlyForGroup(long, int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getProcessDeploymentInfosWithActorOnlyForGroup() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosWithActorOnlyForGroups(java.util.List, int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getProcessDeploymentInfosWithActorOnlyForGroups() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosWithActorOnlyForRole(long, int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getProcessDeploymentInfosWithActorOnlyForRole() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosWithActorOnlyForRoles(java.util.List, int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getProcessDeploymentInfosWithActorOnlyForRoles() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosWithActorOnlyForUser(long, int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getProcessDeploymentInfosWithActorOnlyForUser() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getProcessDeploymentInfosWithActorOnlyForUsers(java.util.List, int, int, org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion)}
     * .
     */
    @Test
    public final void getProcessDeploymentInfosWithActorOnlyForUsers() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchConnectorInstances(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchConnectorInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchArchivedConnectorInstances(org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchArchivedConnectorInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getHumanTaskInstances(long, java.lang.String, int, int)}.
     */
    @Test
    public final void getHumanTaskInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#getLastStateHumanTaskInstance(long, java.lang.String)}.
     */
    @Test
    public final void getLastStateHumanTaskInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#searchUsersWhoCanStartProcessDefinition(long, org.bonitasoft.engine.search.SearchOptions)}.
     */
    @Test
    public final void searchUsersWhoCanStartProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#evaluateExpressionsAtProcessInstanciation(long, java.util.Map)}.
     */
    @Test
    public final void evaluateExpressionsAtProcessInstanciation() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#evaluateExpressionOnCompletedProcessInstance(long, java.util.Map)}.
     */
    @Test
    public final void evaluateExpressionOnCompletedProcessInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#evaluateExpressionsOnProcessInstance(long, java.util.Map)}.
     */
    @Test
    public final void evaluateExpressionsOnProcessInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#evaluateExpressionsOnProcessDefinition(long, java.util.Map)}.
     */
    @Test
    public final void evaluateExpressionsOnProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#evaluateExpressionsOnActivityInstance(long, java.util.Map)}.
     */
    @Test
    public final void evaluateExpressionsOnActivityInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.ProcessAPIImpl#evaluateExpressionsOnCompletedActivityInstance(long, java.util.Map)}.
     */
    @Test
    public final void evaluateExpressionsOnCompletedActivityInstance() {
        // TODO : Not yet implemented
    }

}
