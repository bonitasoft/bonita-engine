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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeInstanceImpl;

public abstract class FlowNodeInstanceBuilder<T extends SFlowNodeInstanceImpl, B extends FlowNodeInstanceBuilder<T, B>> extends PersistentObjectBuilder<T, B> {

    protected int stateId;

    protected String stateName;

    protected long lastUpdateDate;

    protected String name;

    protected int loopCounter;

    protected long executedBy;

    protected long executedBySubstitute;

    protected boolean stateExecuting;

    protected long flowNodeDefinitionId;

    protected long rootContainerId;

    protected long parentContainerId;

    protected SStateCategory stateCategory = SStateCategory.NORMAL;

    protected String description;

    protected long logicalGroup1;

    protected long logicalGroup2;

    protected long logicalGroup3;

    protected long logicalGroup4;

    protected boolean terminal;

    protected boolean stable;

    @Override
    protected T fill(final T persistent) {
        super.fill(persistent);
        persistent.setDescription(description);
        persistent.setExecutedBy(executedBy);
        persistent.setExecutedBySubstitute(executedBySubstitute);
        persistent.setFlowNodeDefinitionId(flowNodeDefinitionId);
        persistent.setLastUpdateDate(lastUpdateDate);
        persistent.setLoopCounter(loopCounter);
        persistent.setName(name);
        persistent.setParentContainerId(parentContainerId);
        persistent.setRootContainerId(rootContainerId);
        persistent.setStable(stable);
        persistent.setStateCategory(stateCategory);
        persistent.setStateExecuting(stateExecuting);
        persistent.setStateId(stateId);
        persistent.setStateName(stateName);
        persistent.setTerminal(terminal);
        persistent.setLogicalGroup(0, logicalGroup1);
        persistent.setLogicalGroup(1, logicalGroup2);
        persistent.setLogicalGroup(2, logicalGroup3);
        persistent.setLogicalGroup(3, logicalGroup4);
        return persistent;
    }

    public B withFlowNodeDefinitionId(final long flowNodeDefinitionId) {
        this.flowNodeDefinitionId = flowNodeDefinitionId;
        return thisBuilder;
    }

    public B withTerminal(final boolean terminal) {
        this.terminal = terminal;
        return thisBuilder;
    }

    public B withStable(final boolean stable) {
        this.stable = stable;
        return thisBuilder;
    }

    public B withStateExecuting(final boolean stateExecuting) {
        this.stateExecuting = stateExecuting;
        return thisBuilder;
    }

    public B withExecutedBy(final long executedBy) {
        this.executedBy = executedBy;
        return thisBuilder;
    }

    public B withExecutedBySubstitute(final long executedBySubstitute) {
        this.executedBySubstitute = executedBySubstitute;
        return thisBuilder;
    }

    public B withId(final long id) {
        this.id = id;
        return thisBuilder;
    }

    public B withName(final String name) {
        this.name = name;
        return thisBuilder;
    }

    public B withStateName(final String stateName) {
        this.stateName = stateName;
        return thisBuilder;
    }

    public B withDescription(final String description) {
        this.description = description;
        return thisBuilder;
    }

    public B withStateId(final int stateId) {
        this.stateId = stateId;
        return thisBuilder;
    }

    public B withLastUpdateDate(final long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
        return thisBuilder;
    }

    public B withRootContainerId(final long containerId) {
        this.rootContainerId = containerId;
        return thisBuilder;
    }

    public B withParentContainerId(final long processInstanceId) {
        this.parentContainerId = processInstanceId;
        return thisBuilder;
    }

    public B withLoopCounter(final int loopCounter) {
        this.loopCounter = loopCounter;
        return thisBuilder;
    }

    public B withStateCategory(final SStateCategory stateCategory) {
        this.stateCategory = stateCategory;
        return thisBuilder;
    }

    public B withLogicalGroup1(final long logicalGroup) {
        this.logicalGroup1 = logicalGroup;
        return thisBuilder;
    }

    public B withLogicalGroup2(final long logicalGroup) {
        this.logicalGroup2 = logicalGroup;
        return thisBuilder;
    }

    public B withLogicalGroup3(final long logicalGroup) {
        this.logicalGroup3 = logicalGroup;
        return thisBuilder;
    }

    public B withLogicalGroup4(final long logicalGroup) {
        this.logicalGroup4 = logicalGroup;
        return thisBuilder;
    }

    public B withProcessDefinition(long processDefinitionId) {
        return withLogicalGroup1(processDefinitionId);
    }
}
