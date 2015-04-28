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
package org.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * author Emmanuel Duchastenier
 */
public class IsTaskPerformerRule extends AuthorizationRuleWithParameters implements AuthorizationRule {

    private ActivityInstanceService activityInstanceService;
    private TechnicalLoggerService technicalLoggerService;

    public IsTaskPerformerRule(ActivityInstanceService activityInstanceService, TechnicalLoggerService technicalLoggerService) {
        this.activityInstanceService = activityInstanceService;
        this.technicalLoggerService = technicalLoggerService;
    }

    @Override
    public boolean isAllowed(String key, Map<String, Serializable> context) throws SExecutionException {
        try {
            Long userId = getLongParameter(context, URLAdapterConstants.USER_QUERY_PARAM);
            Long processInstanceId = getLongParameter(context, URLAdapterConstants.ID_QUERY_PARAM);
            if (userId == null || processInstanceId == null) {
                throw new IllegalArgumentException(
                        "Parameters 'userId' and 'processInstanceId' are mandatory to execute Page Authorization rule 'IsProcessInitiatorRule'");
            }

            QueryOptions archivedQueryOptions = buildArchivedTasksQueryOptions(processInstanceId);
            List<SAHumanTaskInstance> sArchivedHumanTasks = activityInstanceService.searchArchivedTasks(archivedQueryOptions);
            while (!sArchivedHumanTasks.isEmpty()) {
                for (final SAHumanTaskInstance sArchivedHumanTask : sArchivedHumanTasks) {
                    if (userId == sArchivedHumanTask.getAssigneeId()) {
                        return true;
                    }
                }
                archivedQueryOptions = QueryOptions.getNextPage(archivedQueryOptions);
                sArchivedHumanTasks = activityInstanceService.searchArchivedTasks(archivedQueryOptions);
            }
            return false;

        } catch (final SBonitaException e) {
            throw new SExecutionException(e);
        }
    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_TASK_PERFORMER;
    }

    private QueryOptions buildArchivedTasksQueryOptions(final long processInstanceId) {
        final SAUserTaskInstanceBuilderFactory archUserTaskKeyFactory = BuilderFactory.get(SAUserTaskInstanceBuilderFactory.class);
        final String humanTaskIdKey = archUserTaskKeyFactory.getIdKey();
        final String parentProcessInstanceKey = archUserTaskKeyFactory.getParentProcessInstanceKey();
        final List<OrderByOption> archivedOrderByOptions = Arrays.asList(new OrderByOption(SAHumanTaskInstance.class, humanTaskIdKey, OrderByType.ASC));
        final List<FilterOption> archivedFilterOptions = Arrays
                .asList(new FilterOption(SAHumanTaskInstance.class, parentProcessInstanceKey, processInstanceId));
        return new QueryOptions(0, 10, archivedOrderByOptions, archivedFilterOptions, null);
    }
}
