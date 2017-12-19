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
 */
package org.bonitasoft.engine.api.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractHumanTaskInstanceSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Emmanuel Duchastenier
 */
public class TaskInvolvementDelegate {

    private static final int BATCH_SIZE = 100;

    protected TenantServiceAccessor getTenantServiceAccessor() {
        return APIUtils.getTenantAccessor();
    }

    public boolean isExecutorOfArchivedTaskOfProcess(long userId, Long rootProcessInstanceId)
            throws SBonitaReadException {
        final ActivityInstanceService activityInstanceService = getTenantServiceAccessor().getActivityInstanceService();

        QueryOptions archivedQueryOptions = buildArchivedTasksQueryOptions(rootProcessInstanceId);
        List<SAHumanTaskInstance> sArchivedHumanTasks = activityInstanceService.searchArchivedTasks(archivedQueryOptions);
        while (!sArchivedHumanTasks.isEmpty()) {
            for (final SAHumanTaskInstance sArchivedHumanTask : sArchivedHumanTasks) {
                if (userId == sArchivedHumanTask.getExecutedBy()) {
                    return true;
                }
            }
            archivedQueryOptions = QueryOptions.getNextPage(archivedQueryOptions);
            sArchivedHumanTasks = activityInstanceService.searchArchivedTasks(archivedQueryOptions);
        }
        return false;
    }

    private static QueryOptions buildArchivedTasksQueryOptions(final long rootProcessInstanceId) {
        final SAUserTaskInstanceBuilderFactory archUserTaskKeyFactory = BuilderFactory.get(SAUserTaskInstanceBuilderFactory.class);
        final String humanTaskIdKey = archUserTaskKeyFactory.getIdKey();
        final String parentProcessInstanceKey = archUserTaskKeyFactory.getRootProcessInstanceKey();
        final List<OrderByOption> archivedOrderByOptions = Collections
                .singletonList(new OrderByOption(SAHumanTaskInstance.class, humanTaskIdKey, OrderByType.ASC));
        final List<FilterOption> archivedFilterOptions = Collections
                .singletonList(new FilterOption(SAHumanTaskInstance.class, parentProcessInstanceKey, rootProcessInstanceId));
        return new QueryOptions(0, BATCH_SIZE, archivedOrderByOptions, archivedFilterOptions, null);
    }

    public boolean isInvolvedInHumanTaskInstance(long userId, long humanTaskInstanceId) throws ActivityInstanceNotFoundException {
        final ActivityInstanceService activityInstanceService = getTenantServiceAccessor().getActivityInstanceService();
        try {
            long assigneeId;
            final SHumanTaskInstance humanTaskInstance = activityInstanceService.getHumanTaskInstance(humanTaskInstanceId);
            assigneeId = humanTaskInstance.getAssigneeId();
            if (assigneeId > 0) {
                //check if the user is the assigned user
                return userId == assigneeId;
            } else {
                //if the task is not assigned check if the user is mapped to the actor of the task
                return activityInstanceService.isTaskPendingForUser(humanTaskInstanceId, userId);
            }
        } catch (SActivityInstanceNotFoundException e) {
            throw new ActivityInstanceNotFoundException(humanTaskInstanceId);
        } catch (SBonitaReadException | SActivityReadException e) {
            throw new RetrieveException(e);
        }
    }

    public boolean hasUserPendingOrAssignedTasks(long userId, Long processInstanceId) throws SExecutionException {
        final ActivityInstanceService activityInstanceService = getTenantServiceAccessor().getActivityInstanceService();
        // is user assigned or has pending tasks on this process instance:
        final QueryOptions queryOptions = new QueryOptions(0, 1, Collections.EMPTY_LIST, Arrays.asList(new FilterOption(SHumanTaskInstance.class,
                "logicalGroup2", processInstanceId)), null);
        try {
            return activityInstanceService.getNumberOfPendingOrAssignedTasks(userId, queryOptions) > 0;
        } catch (SBonitaReadException e) {
            throw new SExecutionException(e);
        }
    }

    public SearchResult<HumanTaskInstance> searchPendingTasksManagedBy(final long managerUserId, final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor();
        final ActivityInstanceService activityInstanceService = tenantServiceAccessor.getActivityInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantServiceAccessor.getSearchEntitiesDescriptor();
        final FlowNodeStateManager flowNodeStateManager = tenantServiceAccessor.getFlowNodeStateManager();
        return AbstractHumanTaskInstanceSearchEntity.searchHumanTaskInstance(searchEntitiesDescriptor.getSearchHumanTaskInstanceDescriptor(),
                searchOptions,
                flowNodeStateManager,
                (queryOptions) -> activityInstanceService.searchNumberOfPendingTasksManagedBy(managerUserId, queryOptions),
                (queryOptions) -> activityInstanceService.searchPendingTasksManagedBy(managerUserId, queryOptions)).search();
    }

}
