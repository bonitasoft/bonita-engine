/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.flownode.HumanTaskDefinition;
import org.bonitasoft.web.rest.model.bpm.flownode.HumanTaskItem;
import org.bonitasoft.web.rest.server.datastore.converter.ActivityAttributeConverter;
import org.bonitasoft.web.rest.server.datastore.utils.Sort;
import org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.ui.utils.DateFormat;

/**
 * @author Séverin Moussel
 */
public class AbstractHumanTaskDatastore<CONSOLE_ITEM extends HumanTaskItem, ENGINE_ITEM extends HumanTaskInstance>
        extends AbstractTaskDatastore<CONSOLE_ITEM, ENGINE_ITEM> {

    public AbstractHumanTaskDatastore(final APISession engineSession) {
        super(engineSession);
    }

    /**
     * Fill a console item using the engine item passed.
     *
     * @param result
     *        The console item to fill
     * @param item
     *        The engine item to use for filling
     * @return This method returns the result parameter passed.
     */
    protected static HumanTaskItem fillConsoleItem(final HumanTaskItem result, final HumanTaskInstance item) {
        TaskDatastore.fillConsoleItem(result, item);

        result.setActorId(APIID.makeAPIID(item.getActorId()));
        result.setAssignedId(APIID.makeAPIID(item.getAssigneeId()));
        result.setAssignedDate(item.getClaimedDate());
        result.setPriority(item.getPriority() != null ? item.getPriority().toString().toLowerCase() : null);
        result.setDueDate(item.getExpectedEndDate());
        return result;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // C.R.U.D.S
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // SEARCH

    @Override
    protected SearchOptionsBuilder makeSearchOptionBuilder(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        String convertedSort = null;
        if (orders != null) {
            convertedSort = new Sort(orders, new ActivityAttributeConverter()).toString();
        }
        final SearchOptionsBuilder builder = SearchOptionsBuilderUtil.buildSearchOptions(page, resultsByPage,
                convertedSort, search);

        addStringFilterToSearchBuilder(filters, builder, HumanTaskItem.ATTRIBUTE_CASE_ID,
                HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID);
        addStringFilterToSearchBuilder(filters, builder, HumanTaskItem.ATTRIBUTE_ROOT_CASE_ID,
                FlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID);
        addStringFilterToSearchBuilder(filters, builder, HumanTaskItem.ATTRIBUTE_PARENT_CASE_ID,
                FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID);
        if (!filters.containsKey(HumanTaskItem.FILTER_USER_ID)
                && !filters.containsKey(HumanTaskItem.FILTER_TEAM_MANAGER_ID)) {
            addStringFilterToSearchBuilder(filters, builder, HumanTaskItem.ATTRIBUTE_PROCESS_ID,
                    HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID);
        }
        addStringFilterToSearchBuilder(filters, builder, HumanTaskItem.ATTRIBUTE_STATE,
                HumanTaskInstanceSearchDescriptor.STATE_NAME);
        addStringFilterToSearchBuilder(filters, builder, HumanTaskItem.ATTRIBUTE_TYPE,
                ActivityInstanceSearchDescriptor.ACTIVITY_TYPE);
        addStringFilterToSearchBuilder(filters, builder, HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID,
                HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID);
        addStringFilterToSearchBuilder(filters, builder, HumanTaskItem.ATTRIBUTE_PRIORITY,
                HumanTaskInstanceSearchDescriptor.PRIORITY);
        addStringFilterToSearchBuilder(filters, builder, HumanTaskItem.ATTRIBUTE_NAME,
                HumanTaskInstanceSearchDescriptor.NAME);
        addStringFilterToSearchBuilder(filters, builder, HumanTaskItem.ATTRIBUTE_DISPLAY_NAME,
                HumanTaskInstanceSearchDescriptor.DISPLAY_NAME);

        return builder;
    }

    @Override
    protected SearchResult<ENGINE_ITEM> runSearch(final SearchOptionsBuilder builder,
            final Map<String, String> filters) {
        try {

            // Using the same id for each test to avoid useless memory usage.
            APIID id = null;

            // Tasks of all users using a specific supervisor's processes.
            id = APIID.makeAPIID(filters.get(HumanTaskItem.FILTER_SUPERVISOR_ID));
            if (id != null && id.isValidLongID()) {
                filters.remove(HumanTaskItem.FILTER_SUPERVISOR_ID);
                return runSupervisorSearch(filters, builder, id.toLong());
            }

            // Tasks of all members of a specific team manager's team.
            id = APIID.makeAPIID(filters.get(HumanTaskItem.FILTER_TEAM_MANAGER_ID));
            if (id != null && id.isValidLongID()) {
                return runTeamManagerSearch(filters, builder, id.toLong());
            }

            return runGenericSearch(builder, filters);

        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    protected SearchResult<ENGINE_ITEM> runGenericSearch(final SearchOptionsBuilder builder,
            final Map<String, String> filters) {
        try {
            // Using the same id for each test to avoid useless memory usage.
            APIID id = null;

            // Tasks claimed by a specific user
            id = APIID.makeAPIID(filters.get(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID));
            if (id != null && id.isValidLongID()) {
                @SuppressWarnings("unchecked")
                final SearchResult<ENGINE_ITEM> searchHumanTaskInstances = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                        .searchPendingTasksAssignedToUser(id.toLong(), builder.done());
                return searchHumanTaskInstances;
            }

            // Available tasks for a specific user (pending + assigned)
            id = APIID.makeAPIID(filters.get(HumanTaskItem.FILTER_USER_ID));
            if (id != null && id.isValidLongID()) {
                // Show also assigned to other users
                if (filters.containsKey(HumanTaskItem.FILTER_SHOW_ASSIGNED_TO_OTHERS)
                        && Boolean.parseBoolean(filters.get(HumanTaskItem.FILTER_SHOW_ASSIGNED_TO_OTHERS))) {
                    @SuppressWarnings("unchecked")
                    final SearchResult<ENGINE_ITEM> searchPendingTasks = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                            .searchPendingOrAssignedToUserOrAssignedToOthersTasks(
                                    id.toLong(), builder.done());
                    return searchPendingTasks;
                } // ATTRIBUTE_ASSIGNED_USER_ID explicitly passed as NULL
                else if (filters.containsKey(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID)
                        && APIID.makeAPIID(filters.get(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID)) == null) {
                    @SuppressWarnings("unchecked")
                    final SearchResult<ENGINE_ITEM> searchPendingTasksForUser = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                            .searchPendingTasksForUser(
                                    id.toLong(), builder.done());
                    return searchPendingTasksForUser;
                } else if (filters.containsKey(HumanTaskItem.ATTRIBUTE_PROCESS_ID)) {
                    @SuppressWarnings("unchecked")
                    final SearchResult<ENGINE_ITEM> searchMyAvailableHumanTasks = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                            .searchAssignedAndPendingHumanTasksFor(
                                    APIID.makeAPIID(filters.get(HumanTaskItem.ATTRIBUTE_PROCESS_ID)).toLong(),
                                    id.toLong(),
                                    builder.done());
                    return searchMyAvailableHumanTasks;
                } else {
                    @SuppressWarnings("unchecked")
                    final SearchResult<ENGINE_ITEM> searchMyAvailableHumanTasks = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                            .searchMyAvailableHumanTasks(
                                    id.toLong(), builder.done());
                    return searchMyAvailableHumanTasks;
                }
            }

            if (HumanTaskItem.VALUE_STATE_READY.equals(filters.get(HumanTaskItem.ATTRIBUTE_STATE))) {
                @SuppressWarnings("unchecked")
                SearchResult<ENGINE_ITEM> searchHumanTaskInstances = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                        .searchAssignedAndPendingHumanTasks(builder.done());
                return searchHumanTaskInstances;
            }

            // Custom search
            @SuppressWarnings("unchecked")
            final SearchResult<ENGINE_ITEM> searchHumanTaskInstances = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                    .searchHumanTaskInstances(builder.done());
            return searchHumanTaskInstances;

        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    private SearchResult<ENGINE_ITEM> runTeamManagerSearch(final Map<String, String> filters,
            final SearchOptionsBuilder builder, final Long teamManagerId) {
        try {
            if (filters.containsKey(HumanTaskItem.FILTER_IS_ASSIGNED)) {
                if (Boolean.TRUE.equals(MapUtil.getValueAsBoolean(filters, HumanTaskItem.FILTER_IS_ASSIGNED))) {
                    @SuppressWarnings("unchecked")
                    final SearchResult<ENGINE_ITEM> searchResult = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                            .searchAssignedTasksManagedBy(teamManagerId,
                                    builder.done());
                    return searchResult;
                } else {
                    if (filters.containsKey(HumanTaskItem.ATTRIBUTE_PROCESS_ID)) {
                        @SuppressWarnings("unchecked")
                        final SearchResult<ENGINE_ITEM> searchMyAvailableHumanTasks = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                                .searchAssignedAndPendingHumanTasks(
                                        APIID.makeAPIID(filters.get(HumanTaskItem.ATTRIBUTE_PROCESS_ID)).toLong(),
                                        builder.done());
                        return searchMyAvailableHumanTasks;
                    } else {
                        @SuppressWarnings("unchecked")
                        final SearchResult<ENGINE_ITEM> searchResult = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                                .searchPendingTasksManagedBy(teamManagerId,
                                        builder.done());
                        return searchResult;
                    }
                }
            } else {
                // Custom search
                @SuppressWarnings("unchecked")
                final SearchResult<ENGINE_ITEM> searchHumanTaskInstances = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                        .searchHumanTaskInstances(builder.done());
                return searchHumanTaskInstances;
            }

        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    private SearchResult<ENGINE_ITEM> runSupervisorSearch(final Map<String, String> filters,
            final SearchOptionsBuilder builder, final Long supervisorId) {
        try {

            final String taskType = filters.get(HumanTaskItem.ATTRIBUTE_STATE);
            if (taskType == null) {
                throw new APIException("Can't retrieve mixed state tasks for a defined supervisor");
            }

            if (StringUtils.equalsIgnoreCase(taskType, HumanTaskItem.VALUE_STATE_READY)) {
                @SuppressWarnings("unchecked")
                final SearchResult<ENGINE_ITEM> searchResult = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                        .searchPendingTasksSupervisedBy(supervisorId,
                                builder.done());
                return searchResult;
            } else {
                throw new APIException("Can't retrieve non pending human task for a Process Manager");
            }
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    // GET

    @Override
    public CONSOLE_ITEM get(final APIID id) {
        try {
            @SuppressWarnings("unchecked")
            final ENGINE_ITEM humanTaskInstance = (ENGINE_ITEM) getProcessAPI().getHumanTaskInstance(id.toLong());
            return convertEngineToConsoleItem(humanTaskInstance);
        } catch (final ActivityInstanceNotFoundException e) {
            throw new APIItemNotFoundException(HumanTaskDefinition.TOKEN, id);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    // UPDATE
    @Override
    public CONSOLE_ITEM update(final APIID id, final Map<String, String> attributes) {
        try {
            // Priority
            if (attributes.containsKey(HumanTaskItem.ATTRIBUTE_PRIORITY)) {
                getProcessAPI().setTaskPriority(
                        id.toLong(),
                        TaskPriority.valueOf(attributes.get(HumanTaskItem.ATTRIBUTE_PRIORITY).toUpperCase()));
            }

            // Due date
            if (attributes.containsKey(HumanTaskItem.ATTRIBUTE_DUE_DATE)) {
                getProcessAPI().updateDueDateOfTask(
                        id.toLong(),
                        DateFormat.sqlToDate(attributes.get(HumanTaskItem.ATTRIBUTE_DUE_DATE)));
            }

            // Assigned to
            if (attributes.containsKey(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID)) {

                final APIID userId = APIID.makeAPIID(attributes.get(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID));
                if (userId == null || !userId.isValidLongID()) {
                    getProcessAPI().releaseUserTask(id.toLong());
                } else {
                    getProcessAPI().assignUserTask(id.toLong(), userId.toLong());
                }
            }

            return super.update(id, attributes);

        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COUNTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Long getNumberOfOpenTasks(final APIID userId) {
        try {
            return getProcessAPI().getNumberOfOpenTasks(Collections.singletonList(userId.toLong()))
                    .get(userId.toLong());
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    public Long getNumberOfOverdueOpenTasks(final APIID userId) {
        try {
            return getProcessAPI().getNumberOfOverdueOpenTasks(Collections.singletonList(userId.toLong()))
                    .get(userId.toLong());
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

}
