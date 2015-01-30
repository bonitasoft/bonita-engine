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
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
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

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class AbstractProcessInstanceInterruptor {

    private final TechnicalLoggerService logger;

    public AbstractProcessInstanceInterruptor(final TechnicalLoggerService technicalLoggerService) {
        super();
        logger = technicalLoggerService;
    }

    public void interruptProcessInstance(final long processInstanceId, final SStateCategory stateCategory, final long userId)
            throws SProcessInstanceNotFoundException, SBonitaException {
        setProcessStateCategory(processInstanceId, stateCategory);
        final List<Long> stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory);
        if (stableChildrenIds != null) {
            for (final Long childId : stableChildrenIds) {
                resumeChildExecution(childId, processInstanceId, userId);
            }
        }
    }

    public void interruptProcessInstance(final long processInstanceId, final SStateCategory stateCategory, final long userId, final long exceptionChildId)
            throws SProcessInstanceNotFoundException, SBonitaException {
        setProcessStateCategory(processInstanceId, stateCategory);
        final List<Long> stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory, exceptionChildId);
        if (stableChildrenIds != null) {
            for (final Long childId : stableChildrenIds) {
                resumeChildExecution(childId, processInstanceId, userId);
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
                resumeChildExecution(childId, processInstanceId, userId);
            }
        }
    }

    protected abstract void setProcessStateCategory(long processInstanceId, SStateCategory stateCategory) throws SProcessInstanceNotFoundException,
            SBonitaException;

    protected abstract void resumeChildExecution(long childId, long processInstanceId, long userId) throws SBonitaException;

    private List<Long> interruptChildrenFlowNodeInstances(final long processInstanceId, final SStateCategory stateCategory) throws SBonitaException {
        final List<Long> allChildrenToResume = new ArrayList<Long>();
        List<SFlowNodeInstance> children;
        long count = 0;
        do {
            children = getChildren(processInstanceId);
            count = getNumberOfChildren(processInstanceId);

            final List<Long> childrenToResume = interruptFlowNodeInstances(children, stateCategory);
            allChildrenToResume.addAll(childrenToResume);
        } while (count > children.size());

        return allChildrenToResume;
    }

    private List<Long> interruptChildrenFlowNodeInstances(final long processInstanceId, final SStateCategory stateCategory, final long exceptionChildId)
            throws SBonitaException {
        final List<Long> allChildrenToResume = new ArrayList<Long>();
        List<SFlowNodeInstance> children;
        long count = 0;
        do {
            children = getChildrenExcept(processInstanceId, exceptionChildId);
            count = getNumberOfChildrenExcept(processInstanceId, exceptionChildId);

            final List<Long> childrenToResume = interruptFlowNodeInstances(children, stateCategory);
            allChildrenToResume.addAll(childrenToResume);
        } while (count > children.size());

        return allChildrenToResume;
    }

    protected abstract List<SFlowNodeInstance> getChildren(final long processInstanceId) throws SBonitaException;

    protected abstract long getNumberOfChildren(final long processInstanceId) throws SBonitaReadException;

    protected abstract List<SFlowNodeInstance> getChildrenExcept(final long processInstanceId, final long exceptionChildId) throws SBonitaException;

    protected abstract long getNumberOfChildrenExcept(final long processInstanceId, final long exceptionChildId) throws SBonitaReadException;

    private List<Long> interruptFlowNodeInstances(final List<SFlowNodeInstance> children, final SStateCategory stateCategory) throws SBonitaException {
        final List<Long> childrenToResume = new ArrayList<Long>();
        for (final SFlowNodeInstance child : children) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                        "Put element in " + stateCategory + ", id= " + child.getId() + " name = " + child.getName() + " state = " + child.getStateName());
            }
            setChildStateCategory(child.getId(), stateCategory);
            if (child.mustExecuteOnAbortOrCancelProcess()) {
                childrenToResume.add(child.getId());
            }
        }
        return childrenToResume;
    }

    protected abstract void setChildStateCategory(long flowNodeInstanceId, SStateCategory stateCategory) throws SBonitaException;

    protected QueryOptions getQueryOptions(final long processInstanceId) {
        final int numberOfResults = 100;

        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>(1);
        orderByOptions.add(new OrderByOption(SFlowNodeInstance.class, BuilderFactory.get(SUserTaskInstanceBuilderFactory.class).getNameKey(), OrderByType.ASC));

        final List<FilterOption> filterOptions = getFilterOptions(processInstanceId);
        return new QueryOptions(0, numberOfResults, orderByOptions, filterOptions, null);
    }

    protected QueryOptions getQueryOptions(final long processInstanceId, final long childExceptionId) {
        final int numberOfResults = 100;

        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>(1);
        orderByOptions.add(new OrderByOption(SFlowNodeInstance.class, BuilderFactory.get(SUserTaskInstanceBuilderFactory.class).getNameKey(), OrderByType.ASC));

        final List<FilterOption> filterOptions = getFilterOptions(processInstanceId, childExceptionId);
        return new QueryOptions(0, numberOfResults, orderByOptions, filterOptions, null);
    }

    protected List<FilterOption> getFilterOptions(final long processInstanceId) {
        final SFlowNodeInstanceBuilderFactory flowNodeInstanceKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        final List<FilterOption> filterOptions = new ArrayList<FilterOption>(3);
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getParentProcessInstanceKey(), processInstanceId));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getTerminalKey(), false));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getStateCategoryKey(), SStateCategory.NORMAL.name()));
        return filterOptions;
    }

    protected List<FilterOption> getFilterOptions(final long processInstanceId, final long childExceptionId) {
        final SFlowNodeInstanceBuilderFactory flowNodeInstanceKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        final List<FilterOption> filterOptions = new ArrayList<FilterOption>(3);
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getParentProcessInstanceKey(), processInstanceId));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getTerminalKey(), false));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getStateCategoryKey(), SStateCategory.NORMAL.name()));
        filterOptions.add(new FilterOption(SFlowNodeInstance.class, flowNodeInstanceKeyProvider.getIdKey(), childExceptionId, FilterOperationType.DIFFERENT));
        return filterOptions;
    }

}
