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
package org.bonitasoft.engine.execution;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.work.SWorkRegisterException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class ProcessInstanceInterruptor {

    private ProcessInstanceService processInstanceService;
    private FlowNodeInstanceService flowNodeInstanceService;
    private ContainerRegistry containerRegistry;
    private final TechnicalLoggerService logger;

    public ProcessInstanceInterruptor(ProcessInstanceService processInstanceService, FlowNodeInstanceService flowNodeInstanceService,
            ContainerRegistry containerRegistry, final TechnicalLoggerService technicalLoggerService) {
        super();
        this.processInstanceService = processInstanceService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.containerRegistry = containerRegistry;
        logger = technicalLoggerService;
    }

    public void interruptProcessInstance(final long processInstanceId, final SStateCategory stateCategory)
            throws SBonitaException {
        processInstanceService.setStateCategory(processInstanceService.getProcessInstance(processInstanceId), stateCategory);
        final List<SFlowNodeInstance> stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory);
        if (stableChildrenIds != null) {
            for (final SFlowNodeInstance child : stableChildrenIds) {
                executeFlowNode(child);
            }
        }
    }

    private void executeFlowNode(SFlowNodeInstance child) throws SWorkRegisterException {
        containerRegistry.executeFlowNode(child.getProcessDefinitionId(), child.getParentProcessInstanceId(), child.getId());
    }

    public void interruptProcessInstance(final long processInstanceId, final SStateCategory stateCategory, final long exceptionChildId)
            throws SBonitaException {
        processInstanceService.setStateCategory(processInstanceService.getProcessInstance(processInstanceId), stateCategory);
        final List<SFlowNodeInstance> stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory, exceptionChildId);
        if (stableChildrenIds != null) {
            for (final SFlowNodeInstance child : stableChildrenIds) {
                executeFlowNode(child);
            }
        }
    }

    public void interruptChildrenOnly(final long processInstanceId, final SStateCategory stateCategory, final long interruptorChildId)
            throws SBonitaException {
        final List<SFlowNodeInstance> stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory, interruptorChildId);
        if (stableChildrenIds != null) {
            for (final SFlowNodeInstance child : stableChildrenIds) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Resume child in stateCategory " + stateCategory + " id = " + child.getId());
                }
                executeFlowNode(child);
            }
        }
    }

    private List<SFlowNodeInstance> interruptChildrenFlowNodeInstances(final long processInstanceId, final SStateCategory stateCategory)
            throws SBonitaException {
        final List<SFlowNodeInstance> allChildrenToResume = new ArrayList<>();
        List<SFlowNodeInstance> children;
        long count;
        do {
            children = getChildren(processInstanceId);
            count = getNumberOfChildren(processInstanceId);

            final List<SFlowNodeInstance> childrenToResume = interruptFlowNodeInstances(children, stateCategory);
            allChildrenToResume.addAll(childrenToResume);
        } while (count > children.size());

        return allChildrenToResume;
    }

    private List<SFlowNodeInstance> interruptChildrenFlowNodeInstances(final long processInstanceId, final SStateCategory stateCategory,
            final long exceptionChildId)
            throws SBonitaException {
        final List<SFlowNodeInstance> allChildrenToResume = new ArrayList<>();
        List<SFlowNodeInstance> children;
        long count;
        do {
            children = getChildrenExcept(processInstanceId, exceptionChildId);
            count = getNumberOfChildrenExcept(processInstanceId, exceptionChildId);

            final List<SFlowNodeInstance> childrenToResume = interruptFlowNodeInstances(children, stateCategory);
            allChildrenToResume.addAll(childrenToResume);
        } while (count > children.size());

        return allChildrenToResume;
    }

    private List<SFlowNodeInstance> getChildren(final long processInstanceId) throws SBonitaException {
        return flowNodeInstanceService.searchFlowNodeInstances(SFlowNodeInstance.class,
                getQueryOptions(processInstanceId));
    }

    private long getNumberOfChildren(final long processInstanceId) throws SBonitaReadException {
        final QueryOptions countOptions = new QueryOptions(0, 1, null, getFilterOptions(processInstanceId), null);
        return flowNodeInstanceService.getNumberOfFlowNodeInstances(SFlowNodeInstance.class, countOptions);
    }

    private List<SFlowNodeInstance> getChildrenExcept(final long processInstanceId, final long childExceptionId) throws SBonitaException {
        return flowNodeInstanceService.searchFlowNodeInstances(SFlowNodeInstance.class,
                getQueryOptions(processInstanceId, childExceptionId));
    }

    private long getNumberOfChildrenExcept(final long processInstanceId, final long childExceptionId) throws SBonitaReadException {
        final QueryOptions countOptions = new QueryOptions(0, 1, null, getFilterOptions(processInstanceId, childExceptionId), null);
        return flowNodeInstanceService.getNumberOfFlowNodeInstances(SFlowNodeInstance.class, countOptions);
    }

    private List<SFlowNodeInstance> interruptFlowNodeInstances(final List<SFlowNodeInstance> children, final SStateCategory stateCategory)
            throws SBonitaException {
        final List<SFlowNodeInstance> childrenToResume = new ArrayList<>();
        for (final SFlowNodeInstance child : children) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                        "Put element in " + stateCategory + ", id= " + child.getId() + " name = " + child.getName() + " state = " + child.getStateName());
            }
            flowNodeInstanceService.setStateCategory(child, stateCategory);
            if (child.mustExecuteOnAbortOrCancelProcess()) {
                childrenToResume.add(child);
            }
        }
        return childrenToResume;
    }

    private QueryOptions getQueryOptions(final long processInstanceId) {
        final int numberOfResults = 100;

        final List<OrderByOption> orderByOptions = new ArrayList<>(1);
        orderByOptions.add(new OrderByOption(SFlowNodeInstance.class, BuilderFactory.get(SUserTaskInstanceBuilderFactory.class).getNameKey(), OrderByType.ASC));

        final List<FilterOption> filterOptions = getFilterOptions(processInstanceId);
        return new QueryOptions(0, numberOfResults, orderByOptions, filterOptions, null);
    }

    private QueryOptions getQueryOptions(final long processInstanceId, final long childExceptionId) {
        final int numberOfResults = 100;

        final List<OrderByOption> orderByOptions = new ArrayList<>(1);
        orderByOptions.add(new OrderByOption(SFlowNodeInstance.class, BuilderFactory.get(SUserTaskInstanceBuilderFactory.class).getNameKey(), OrderByType.ASC));

        final List<FilterOption> filterOptions = getFilterOptions(processInstanceId, childExceptionId);
        return new QueryOptions(0, numberOfResults, orderByOptions, filterOptions, null);
    }

    private List<FilterOption> getFilterOptions(final long processInstanceId) {
        final SFlowNodeInstanceBuilderFactory flowNodeInstanceKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        final List<FilterOption> filterOptions = new ArrayList<>(3);
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getParentProcessInstanceKey(), processInstanceId));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getStateCategoryKey(), SStateCategory.NORMAL.name()));
        return filterOptions;
    }

    private List<FilterOption> getFilterOptions(final long processInstanceId, final long childExceptionId) {
        final SFlowNodeInstanceBuilderFactory flowNodeInstanceKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        final List<FilterOption> filterOptions = new ArrayList<>(3);
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getParentProcessInstanceKey(), processInstanceId));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getStateCategoryKey(), SStateCategory.NORMAL.name()));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getIdKey(), childExceptionId, FilterOperationType.DIFFERENT));
        return filterOptions;
    }

}
