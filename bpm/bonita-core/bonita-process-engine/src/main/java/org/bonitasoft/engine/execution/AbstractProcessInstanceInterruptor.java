/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilder;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.search.FilterOperationType;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class AbstractProcessInstanceInterruptor {

    private final BPMInstanceBuilders bpmInstanceBuilders;

    private final LockService lockService;

    private final TechnicalLoggerService logger;

    public AbstractProcessInstanceInterruptor(final BPMInstanceBuilders bpmInstanceBuilders, final LockService lockService,
            final TechnicalLoggerService technicalLoggerService) {
        super();
        this.bpmInstanceBuilders = bpmInstanceBuilders;
        this.lockService = lockService;
        logger = technicalLoggerService;
    }

    public void interruptProcessInstance(final long processInstanceId, final SStateCategory stateCategory, final long userId) throws SBonitaException {
        List<Long> stableChildrenIds = null;
        // lock process execution
        final String objectType = SFlowElementsContainerType.PROCESS.name();
        lockService.lock(processInstanceId, objectType);
        try {
            setProcessStateCategory(processInstanceId, stateCategory);
            stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory);
        } finally {
            // unlock process execution
            lockService.unlock(processInstanceId, objectType);
        }
        if (stableChildrenIds != null) {
            for (final Long childId : stableChildrenIds) {
                resumeStableChildExecution(childId, processInstanceId, userId);
            }
        }
    }

    public void interruptProcessInstance(final long processInstanceId, final SStateCategory stateCategory, final long userId, final long exceptionChildId)
            throws SBonitaException {
        List<Long> stableChildrenIds = null;
        // lock process execution
        final String objectType = SFlowElementsContainerType.PROCESS.name();
        lockService.lock(processInstanceId, objectType);
        try {
            setProcessStateCategory(processInstanceId, stateCategory);
            stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory, exceptionChildId);
        } finally {
            // unlock process execution
            lockService.unlock(processInstanceId, objectType);
        }
        if (stableChildrenIds != null) {
            for (final Long childId : stableChildrenIds) {
                resumeStableChildExecution(childId, processInstanceId, userId);
            }
        }
    }

    public void interruptChildrenOnly(final long processInstanceId, final SStateCategory stateCategory, final long userId, final long interruptorChildId)
            throws SBonitaException {
        final List<Long> stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory, interruptorChildId);
        if (stableChildrenIds != null) {
            for (final Long childId : stableChildrenIds) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Resume child in stateCategory " + stateCategory + " id = " + childId);
                }
                resumeStableChildExecution(childId, processInstanceId, userId);
            }
        }
    }

    protected abstract void setProcessStateCategory(long processInstanceId, SStateCategory stateCategory) throws SBonitaException;

    protected abstract void resumeStableChildExecution(long childId, long processInstanceId, long userId) throws SBonitaException;

    private List<Long> interruptChildrenFlowNodeInstances(final long processInstanceId, final SStateCategory stateCategory) throws SBonitaException {
        final List<Long> stableChildrenIds = new ArrayList<Long>();
        List<SFlowNodeInstance> children;
        long count = 0;
        do {
            children = getChildren(processInstanceId);
            count = getNumberOfChildren(processInstanceId);

            final List<Long> stableIds = interruptFlowNodeInstances(children, stateCategory);
            stableChildrenIds.addAll(stableIds);
        } while (count > children.size());

        return stableChildrenIds;
    }

    private List<Long> interruptChildrenFlowNodeInstances(final long processInstanceId, final SStateCategory stateCategory, final long exceptionChildId)
            throws SBonitaException {
        final List<Long> stableChildrenIds = new ArrayList<Long>();
        List<SFlowNodeInstance> children;
        long count = 0;
        do {
            children = getChildrenExcept(processInstanceId, exceptionChildId);
            count = getNumberOfChildrenExcept(processInstanceId, exceptionChildId);

            final List<Long> stableIds = interruptFlowNodeInstances(children, stateCategory);
            stableChildrenIds.addAll(stableIds);
        } while (count > children.size());

        return stableChildrenIds;
    }

    protected abstract List<SFlowNodeInstance> getChildren(final long processInstanceId) throws SBonitaException;

    protected abstract long getNumberOfChildren(final long processInstanceId) throws SBonitaSearchException;

    protected abstract List<SFlowNodeInstance> getChildrenExcept(final long processInstanceId, final long exceptionChildId) throws SBonitaException;

    protected abstract long getNumberOfChildrenExcept(final long processInstanceId, final long exceptionChildId) throws SBonitaSearchException;

    private List<Long> interruptFlowNodeInstances(final List<SFlowNodeInstance> children, final SStateCategory stateCategory) throws SBonitaException {
        final List<Long> stableChildrenIds = new ArrayList<Long>();
        for (final SFlowNodeInstance child : children) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                        "Put element in " + stateCategory + ", id= " + child.getId() + " name = " + child.getName() + " state = " + child.getStateName());
            }
            setChildStateCategory(child.getId(), stateCategory);
            if (child.isStable()) {
                stableChildrenIds.add(child.getId());
            }
        }
        return stableChildrenIds;
    }

    protected abstract void setChildStateCategory(long flowNodeInstanceId, SStateCategory stateCategory) throws SBonitaException;

    protected QueryOptions getQueryOptions(final long processInstanceId) {
        final SFlowNodeInstanceBuilder flowNodeInstanceKeyProvider = bpmInstanceBuilders.getSUserTaskInstanceBuilder();
        final int numberOfResults = 100;

        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>(1);
        orderByOptions.add(new OrderByOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getNameKey(), OrderByType.ASC));

        final List<FilterOption> filterOptions = getFilterOptions(processInstanceId, flowNodeInstanceKeyProvider);
        return new QueryOptions(0, numberOfResults, orderByOptions, filterOptions, null);
    }

    protected QueryOptions getQueryOptions(final long processInstanceId, final long childExceptionId) {
        final SFlowNodeInstanceBuilder flowNodeInstanceKeyProvider = bpmInstanceBuilders.getSUserTaskInstanceBuilder();
        final int numberOfResults = 100;

        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>(1);
        orderByOptions.add(new OrderByOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getNameKey(), OrderByType.ASC));

        final List<FilterOption> filterOptions = getFilterOptions(processInstanceId, childExceptionId, flowNodeInstanceKeyProvider);
        return new QueryOptions(0, numberOfResults, orderByOptions, filterOptions, null);
    }

    protected List<FilterOption> getFilterOptions(final long processInstanceId, final SFlowNodeInstanceBuilder flowNodeInstanceKeyProvider) {
        final List<FilterOption> filterOptions = new ArrayList<FilterOption>(3);
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getParentProcessInstanceKey(), processInstanceId));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getTerminalKey(), false));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getStateCategoryKey(), SStateCategory.NORMAL.name()));
        return filterOptions;
    }

    protected List<FilterOption> getFilterOptions(final long processInstanceId, final long childExceptionId,
            final SFlowNodeInstanceBuilder flowNodeInstanceKeyProvider) {
        final List<FilterOption> filterOptions = new ArrayList<FilterOption>(3);
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getParentProcessInstanceKey(), processInstanceId));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getTerminalKey(), false));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getStateCategoryKey(), SStateCategory.NORMAL.name()));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getIdKey(), childExceptionId, FilterOperationType.DIFFERENT));
        return filterOptions;
    }

    protected BPMInstanceBuilders getBpmInstanceBuilders() {
        return bpmInstanceBuilders;
    }

}
