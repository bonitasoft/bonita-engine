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
import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisorSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mazourd
 */
public class APITestProcessCleanerImpl implements APITestProcessCleaner{

    private ProcessAPI processAPI;

    public APITestProcessCleanerImpl(ProcessAPI processAPI){
        this.processAPI = processAPI;
    }
    @Override
    public List<String> checkCategories() throws DeletionException {
        final List<String> messages = new ArrayList<String>();
        final long numberOfCategories = getProcessAPI().getNumberOfCategories();
        if (numberOfCategories > 0) {
            final List<Category> categories = getProcessAPI().getCategories(0, 5000, CategoryCriterion.NAME_ASC);
            final StringBuilder categoryBuilder = new StringBuilder("Categories are still present: ");
            for (final Category category : categories) {
                categoryBuilder.append(category.getName()).append(", ");
                getProcessAPI().deleteCategory(category.getId());
            }
            messages.add(categoryBuilder.toString());
        }
        return messages;
    }

    @Override
    public List<String> checkFlowNodesAreDeleted() throws SearchException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<FlowNodeInstance> searchResult = getProcessAPI().searchFlowNodeInstances(build.done());
        final List<FlowNodeInstance> flowNodeInstances = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("FlowNodes are still present: ");
            for (final FlowNodeInstance flowNodeInstance : flowNodeInstances) {
                messageBuilder.append("{" + flowNodeInstance.getName() + " - ").append(flowNodeInstance.getType() + "}").append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    @Override
    public List<String> checkArchivedFlowNodesAreDeleted() throws SearchException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<ArchivedFlowNodeInstance> searchResult = getProcessAPI().searchArchivedFlowNodeInstances(build.done());
        final List<ArchivedFlowNodeInstance> archivedFlowNodeInstances = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Archived flowNodes are still present: ");
            for (final ArchivedFlowNodeInstance archivedFlowNodeInstance : archivedFlowNodeInstances) {
                messageBuilder.append(archivedFlowNodeInstance.getName()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    @Override
    public List<String> checkCommentsAreDeleted() throws SearchException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<Comment> searchResult = getProcessAPI().searchComments(build.done());
        final List<Comment> comments = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Comments are still present: ");
            for (final Comment comment : comments) {
                messageBuilder.append(comment.getContent()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    @Override
    public List<String> checkArchivedCommentsAreDeleted() throws SearchException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<ArchivedComment> searchResult = getProcessAPI().searchArchivedComments(build.done());
        final List<ArchivedComment> archivedComments = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Archived comments are still present: ");
            for (final ArchivedComment archivedComment : archivedComments) {
                messageBuilder.append(archivedComment.getName()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    @Override
    public List<String> checkProcessDefinitionsAreDeleted() throws BonitaException {
        final List<String> messages = new ArrayList<String>();
        final List<ProcessDeploymentInfo> processes = getProcessAPI().getProcessDeploymentInfos(0, 200, ProcessDeploymentInfoCriterion.DEFAULT);
        if (processes.size() > 0) {
            final StringBuilder processBuilder = new StringBuilder("Process Definitions are still active: ");
            for (final ProcessDeploymentInfo processDeploymentInfo : processes) {
                processBuilder.append(processDeploymentInfo.getId()).append(", ");
                if (ActivationState.ENABLED.equals(processDeploymentInfo.getActivationState())) {
                    getProcessAPI().disableProcess(processDeploymentInfo.getProcessId());
                }
                getProcessAPI().deleteProcessDefinition(processDeploymentInfo.getProcessId());
            }
            messages.add(processBuilder.toString());
        }
        return messages;
    }

    @Override
    public List<String> checkSupervisorsAreDeleted() throws SearchException, DeletionException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 200);
        builder.sort(ProcessSupervisorSearchDescriptor.ID, Order.ASC);
        final List<ProcessSupervisor> supervisors = getProcessAPI().searchProcessSupervisors(builder.done()).getResult();

        if (supervisors.size() > 0) {
            final StringBuilder processBuilder = new StringBuilder("Process Supervisors are still active: ");
            for (final ProcessSupervisor supervisor : supervisors) {
                processBuilder.append(supervisor.getSupervisorId()).append(", ");
                getProcessAPI().deleteSupervisor(supervisor.getSupervisorId());
            }
            messages.add(processBuilder.toString());
        }
        return messages;
    }

    @Override
    public List<String> checkArchivedProcessIntancesAreDeleted() throws DeletionException {
        final List<String> messages = new ArrayList<String>();
        final List<ArchivedProcessInstance> archivedProcessInstances = getProcessAPI().getArchivedProcessInstances(0, 1000, ProcessInstanceCriterion.DEFAULT);
        if (!archivedProcessInstances.isEmpty()) {
            final StringBuilder stb = new StringBuilder("Archived process instances are still present: ");
            for (final ArchivedProcessInstance archivedProcessInstance : archivedProcessInstances) {
                stb.append(archivedProcessInstance).append(", ");
                getProcessAPI().deleteArchivedProcessInstancesInAllStates(archivedProcessInstance.getSourceObjectId());
            }
            messages.add(stb.toString());
        }
        return messages;

    }

    @Override
    public List<String> checkProcessIntancesAreDeleted() throws DeletionException {
        final List<String> messages = new ArrayList<String>();
        final List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 1000, ProcessInstanceCriterion.DEFAULT);
        if (!processInstances.isEmpty()) {
            final StringBuilder stb = new StringBuilder("Process instances are still present: ");
            for (final ProcessInstance processInstance : processInstances) {
                stb.append(processInstance).append(", ");
                getProcessAPI().deleteProcessInstance(processInstance.getId());
            }
            messages.add(stb.toString());
        }
        return messages;
    }

    public ProcessAPI getProcessAPI() {
        return processAPI;
    }
}
