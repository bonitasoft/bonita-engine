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

import org.bonitasoft.engine.api.impl.transaction.process.GetLastArchivedProcessInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Emmanuel Duchastenier
 */
public class ProcessInvolvementDelegate {

    private static final int BATCH_SIZE = 100;

    protected TenantServiceAccessor getTenantServiceAccessor() {
        return APIUtils.getTenantAccessor();
    }

    private static QueryOptions buildArchivedTasksQueryOptions(final long processInstanceId) {
        final SAUserTaskInstanceBuilderFactory archUserTaskKeyFactory = BuilderFactory.get(SAUserTaskInstanceBuilderFactory.class);
        final String humanTaskIdKey = archUserTaskKeyFactory.getIdKey();
        final String parentProcessInstanceKey = archUserTaskKeyFactory.getParentProcessInstanceKey();
        final List<OrderByOption> archivedOrderByOptions = Collections
                .singletonList(new OrderByOption(SAHumanTaskInstance.class, humanTaskIdKey, OrderByType.ASC));
        final List<FilterOption> archivedFilterOptions = Collections
                .singletonList(new FilterOption(SAHumanTaskInstance.class, parentProcessInstanceKey, processInstanceId));
        return new QueryOptions(0, BATCH_SIZE, archivedOrderByOptions, archivedFilterOptions, null);
    }

    public boolean isInvolvedInProcessInstance(final long userId, final long processInstanceId) throws ProcessInstanceNotFoundException {
        final TaskInvolvementDelegate taskInvolvementDelegate = new TaskInvolvementDelegate();
        // IS_PROCESS_INITIATOR rule
        if (isProcessOrArchivedProcessInitiator(userId, processInstanceId)) {
            return true;
        }
        try {
            // IS_TASK_PERFORMER rule
            if (taskInvolvementDelegate.isExecutorOfArchivedTaskOfProcess(userId, processInstanceId)) {
                return true;
            }
        } catch (SBonitaReadException e) {
            throw new RetrieveException(e);
        }

        try {
            // IS_INVOLVED_IN_PROCESS_INSTANCE rule
            if (taskInvolvementDelegate.hasUserPendingOrAssignedTasks(userId, processInstanceId)) {
                return true;
            }
        } catch (SExecutionException e) {
            throw new RetrieveException(e);
        }

        return false;

    }

    public boolean isProcessOrArchivedProcessInitiator(long userId, long processInstanceId) throws ProcessInstanceNotFoundException {
        try {
            return isProcessInitiator(userId, processInstanceId);
        } catch (SProcessInstanceNotFoundException e) {
            return isArchivedProcessInitiator(userId, processInstanceId);
        } catch (SProcessInstanceReadException e) {
            throw new RetrieveException(e);
        }
    }

    private boolean isProcessInitiator(long userId, Long processInstanceId) throws SProcessInstanceNotFoundException, SProcessInstanceReadException {
        final ProcessInstanceService processInstanceService = getTenantServiceAccessor().getProcessInstanceService();
        final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        return userId == processInstance.getStartedBy();
    }

    boolean isArchivedProcessInitiator(long userId, long processInstanceId) throws ProcessInstanceNotFoundException {
        final ProcessInstanceService processInstanceService = getTenantServiceAccessor().getProcessInstanceService();
        final List<OrderByOption> orderByOptions = Arrays.asList(
                new OrderByOption(SAProcessInstance.class, ArchivedProcessInstancesSearchDescriptor.ARCHIVE_DATE, OrderByType.DESC),
                new OrderByOption(SAProcessInstance.class, ArchivedProcessInstancesSearchDescriptor.END_DATE, OrderByType.DESC));
        final List<FilterOption> filterOptions = Collections.singletonList(new FilterOption(SAProcessInstance.class,
                ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, processInstanceId));
        final QueryOptions queryOptions = new QueryOptions(0, 1, orderByOptions, filterOptions, null);

        final List<SAProcessInstance> saProcessInstances;
        try {
            saProcessInstances = processInstanceService.searchArchivedProcessInstances(queryOptions);
        } catch (SBonitaReadException e) {
            throw new RetrieveException(e);
        }
        if (saProcessInstances.isEmpty()) {
            throw new ProcessInstanceNotFoundException(processInstanceId);
        }
        return userId == (saProcessInstances.get(0).getStartedBy());
    }

    public boolean isManagerOfUserInvolvedInProcessInstance(final long managerUserId, final long processInstanceId) throws BonitaException {
        final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor();
        final ProcessInstanceService processInstanceService = tenantServiceAccessor.getProcessInstanceService();
        final IdentityService identityService = tenantServiceAccessor.getIdentityService();
        final TaskInvolvementDelegate taskInvolvementDelegate = new TaskInvolvementDelegate();
        final ActivityInstanceService activityInstanceService = tenantServiceAccessor.getActivityInstanceService();

        final List<SUser> subordinates = getSubordinates(managerUserId, identityService);

        try {
            try {

                // Part specific to active process instances:
                final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
                if (isUserManagerOfProcessInstanceInitiator(managerUserId, processInstance.getStartedBy())) {
                    return true;
                }

                // Has the manager at least one subordinates with at least one pending task in this process instance:
                if (taskInvolvementDelegate.searchPendingTasksManagedBy(managerUserId,
                        new SearchOptionsBuilder(0, 1).filter(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstanceId).done())
                        .getCount() > 0) {
                    return true;
                }

                QueryOptions queryOptions = buildActiveTasksQueryOptions(processInstanceId);
                List<SHumanTaskInstance> sHumanTaskInstances = activityInstanceService.searchHumanTasks(queryOptions);
                while (!sHumanTaskInstances.isEmpty()) {
                    for (final SHumanTaskInstance sHumanTaskInstance : sHumanTaskInstances) {
                        if (isTaskAssignedToAUserInTheList(sHumanTaskInstance, subordinates)) {
                            return true;
                        }
                    }
                    queryOptions = QueryOptions.getNextPage(queryOptions);
                    sHumanTaskInstances = activityInstanceService.searchHumanTasks(queryOptions);
                }
            } catch (final SProcessInstanceNotFoundException exc) {
                // process instance may be completed already:

                // Part specific to archived process instances:
                try {
                    final ArchivedProcessInstance archProcessInstance = getLastArchivedProcessInstance(processInstanceId);
                    if (isUserManagerOfProcessInstanceInitiator(managerUserId, archProcessInstance.getStartedBy())) {
                        return true;
                    }
                } catch (final SBonitaException e) {
                    throw new ProcessInstanceNotFoundException(processInstanceId);
                }
            }

            // Part common to active and archived process instances:
            return isArchivedTaskDoneByOneOfTheSubordinates(processInstanceId, activityInstanceService, subordinates);

        } catch (final SBonitaException e) {
            throw new BonitaException("Problem while searching for users involved in process instance through their manager", e);
        }
    }

    private QueryOptions buildActiveTasksQueryOptions(final long processInstanceId) {
        final SUserTaskInstanceBuilderFactory userTaskKeyFactory = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        final String humanTaskIdKey = userTaskKeyFactory.getIdKey();
        final String parentProcessInstanceKey = userTaskKeyFactory.getParentProcessInstanceKey();
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SHumanTaskInstance.class, humanTaskIdKey, OrderByType.ASC));
        final List<FilterOption> filterOptions = Collections
                .singletonList(new FilterOption(SHumanTaskInstance.class, parentProcessInstanceKey, processInstanceId));
        return new QueryOptions(0, BATCH_SIZE, orderByOptions, filterOptions, null);
    }

    private List<SUser> getSubordinates(final long managerUserId, final IdentityService identityService) {
        final List<OrderByOption> userOrderBys = Collections
                .singletonList(new OrderByOption(SUser.class, BuilderFactory.get(SUserBuilderFactory.class).getIdKey(),
                        OrderByType.ASC));
        final List<FilterOption> userFilters = Collections.singletonList(new FilterOption(SUser.class, BuilderFactory.get(SUserBuilderFactory.class)
                .getManagerUserIdKey(), managerUserId));
        try {
            return identityService.searchUsers(new QueryOptions(0, Integer.MAX_VALUE, userOrderBys, userFilters, null));
        } catch (final SBonitaReadException e) {
            return Collections.emptyList();
        }
    }

    private boolean isArchivedTaskDoneByOneOfTheSubordinates(final long processInstanceId, final ActivityInstanceService activityInstanceService,
            final List<SUser> subordinates) throws SBonitaReadException {
        QueryOptions archivedQueryOptions = buildArchivedTasksQueryOptions(processInstanceId);

        List<SAHumanTaskInstance> sArchivedHumanTasks = activityInstanceService.searchArchivedTasks(archivedQueryOptions);
        while (!sArchivedHumanTasks.isEmpty()) {
            for (final SAHumanTaskInstance sArchivedHumanTask : sArchivedHumanTasks) {
                if (isTaskDoneByAUserInTheList(sArchivedHumanTask, subordinates)) {
                    return true;
                }
            }
            archivedQueryOptions = QueryOptions.getNextPage(archivedQueryOptions);
            sArchivedHumanTasks = activityInstanceService.searchArchivedTasks(archivedQueryOptions);
        }
        return false;
    }

    private boolean isTaskDoneByAUserInTheList(final SAHumanTaskInstance sArchivedHumanTask, final List<SUser> users) {
        for (final SUser user : users) {
            if (user.getId() == sArchivedHumanTask.getExecutedBy()) {
                return true;
            }
        }
        return false;
    }

    private boolean isTaskAssignedToAUserInTheList(final SHumanTaskInstance humanTask, final List<SUser> users) {
        for (final SUser user : users) {
            if (user.getId() == humanTask.getAssigneeId()) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserManagerOfProcessInstanceInitiator(final long userId, final long startedByUserId) {
        final IdentityService identityService = getTenantServiceAccessor().getIdentityService();
        SUser sUser;
        try {
            sUser = identityService.getUser(startedByUserId);
        } catch (final SUserNotFoundException e) {
            return false;
        }
        return userId == sUser.getManagerUserId();
    }

    public ArchivedProcessInstance getLastArchivedProcessInstance(final long processInstanceId) throws SBonitaException {
        final ProcessInstanceService processInstanceService = getTenantServiceAccessor().getProcessInstanceService();
        final ProcessDefinitionService processDefinitionService = getTenantServiceAccessor().getProcessDefinitionService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = getTenantServiceAccessor().getSearchEntitiesDescriptor();

        final GetLastArchivedProcessInstance searchArchivedProcessInstances = new GetLastArchivedProcessInstance(processInstanceService,
                processDefinitionService, processInstanceId, searchEntitiesDescriptor);

        searchArchivedProcessInstances.execute();
        return searchArchivedProcessInstances.getResult();
    }
}
