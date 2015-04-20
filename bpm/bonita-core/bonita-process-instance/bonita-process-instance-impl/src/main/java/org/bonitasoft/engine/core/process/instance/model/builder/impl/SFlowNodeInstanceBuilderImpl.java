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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeInstanceImpl;

/**
 * @author Baptiste Mesta
 */
public abstract class SFlowNodeInstanceBuilderImpl implements SFlowNodeInstanceBuilder {

    protected final SFlowNodeInstanceImpl entity;

    public SFlowNodeInstanceBuilderImpl(final SFlowNodeInstanceImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SFlowNodeInstanceBuilder setState(final FlowNodeState state) {
        entity.setStateId(state.getId());
        entity.setStateName(state.getName());
        entity.setStable(state.isStable());
        entity.setTerminal(state.isTerminal());
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setLastUpdateDate(final long lastUpdateDate) {
        this.entity.setLastUpdateDate(lastUpdateDate);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setReachedStateDate(final long reachedStateDate) {
        this.entity.setReachedStateDate(reachedStateDate);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setRootContainerId(final long containerId) {
        this.entity.setRootContainerId(containerId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setParentContainerId(final long processInstanceId) {
        this.entity.setParentContainerId(processInstanceId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setProcessDefinitionId(final long processDefinitionId) {
        this.entity.setLogicalGroup(SFlowNodeInstanceBuilderFactoryImpl.PROCESS_DEFINITION_INDEX, processDefinitionId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setRootProcessInstanceId(final long processInstanceId) {
        this.entity.setLogicalGroup(SFlowNodeInstanceBuilderFactoryImpl.ROOT_PROCESS_INSTANCE_INDEX, processInstanceId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setParentProcessInstanceId(final long parentProcessInstanceId) {
        this.entity.setLogicalGroup(SFlowNodeInstanceBuilderFactoryImpl.PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setParentActivityInstanceId(final long activityInstanceId) {
        this.entity.setLogicalGroup(SFlowNodeInstanceBuilderFactoryImpl.PARENT_ACTIVITY_INSTANCE_INDEX, activityInstanceId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setLoopCounter(final int loopCounter) {
        this.entity.setLoopCounter(loopCounter);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setStateCategory(final SStateCategory stateCategory) {
        this.entity.setStateCategory(stateCategory);
        return this;
    }

    @Override
    public SFlowNodeType getFlowNodeType() {
        return this.entity.getType();
    }
}
