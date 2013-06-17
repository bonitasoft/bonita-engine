/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeInstanceImpl;

/**
 * @author Baptiste Mesta
 */
public abstract class SFlowNodeInstanceBuilderImpl extends SFlowElementInstanceBuilderImpl implements SFlowNodeInstanceBuilder {

    private static final String DISPLAY_DESCRIPTION = "displayDescription";

    private static final String DISPLAY_NAME = "displayName";

    private static final String STATE_ID_KEY = "stateId";

    private static final String STATE_NAME_KEY = "stateName";

    private static final String PREVIOUS_STATE_ID_KEY = "previousStateId";

    private static final String LAST_UPDATE_KEY = "lastUpdateDate";

    private static final String REACHED_STATE_DATE_KEY = "reachedStateDate";

    private static final String EXECUTE_BY_KEY = "executedBy";

    private static final String EXECUTE_BY_DELEGATE_KEY = "executedByDelegate";

    private static final String STATE_EXECUTING_KEY = "stateExecuting";

    protected abstract SFlowNodeInstanceImpl getEntity();

    @Override
    public SFlowNodeInstanceBuilder setState(final int stateId, final boolean stable, final boolean terminal, final String stateName) {
        getEntity().setStateId(stateId);
        getEntity().setStable(stable);
        getEntity().setTerminal(terminal);
        getEntity().setStateName(stateName);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setLastUpdateDate(final long lastUpdateDate) {
        getEntity().setLastUpdateDate(lastUpdateDate);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setReachedStateDate(final long reachedStateDate) {
        getEntity().setReachedStateDate(reachedStateDate);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setRootContainerId(final long containerId) {
        getEntity().setRootContainerId(containerId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setParentContainerId(final long processInstanceId) {
        getEntity().setParentContainerId(processInstanceId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setProcessDefinitionId(final long processDefinitionId) {
        getEntity().setLogicalGroup(PROCESS_DEFINITION_INDEX, processDefinitionId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setRootProcessInstanceId(final long processInstanceId) {
        getEntity().setLogicalGroup(ROOT_PROCESS_INSTANCE_INDEX, processInstanceId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setParentProcessInstanceId(final long parentProcessInstanceId) {
        getEntity().setLogicalGroup(PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setParentActivityInstanceId(final long activityInstanceId) {
        getEntity().setLogicalGroup(PARENT_ACTIVITY_INSTANCE_INDEX, activityInstanceId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setTokenRefId(final Long tokenRefId) {
        getEntity().setTokenRefId(tokenRefId);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setLoopCounter(final int loopCounter) {
        getEntity().setLoopCounter(loopCounter);
        return this;
    }

    @Override
    public SFlowNodeInstanceBuilder setStateCategory(final SStateCategory stateCategory) {
        getEntity().setStateCategory(stateCategory);
        return this;
    }

    @Override
    public SFlowNodeType getFlowNodeType() {
        return getEntity().getType();
    }

    @Override
    public String getDisplayDescriptionKey() {
        return DISPLAY_DESCRIPTION;
    }

    @Override
    public String getDisplayNameKey() {
        return DISPLAY_NAME;
    }

    @Override
    public String getStateExecutingKey() {
        return STATE_EXECUTING_KEY;
    }

    @Override
    public String getExecutedBy() {
        return EXECUTE_BY_KEY;
    }

    @Override
    public String getExecutedByDelegate() {
        return EXECUTE_BY_DELEGATE_KEY;
    }

    @Override
    public String getStateIdKey() {
        return STATE_ID_KEY;
    }

    @Override
    public String getStateNameKey() {
        return STATE_NAME_KEY;
    }

    @Override
    public String getPreviousStateIdKey() {
        return PREVIOUS_STATE_ID_KEY;
    }

    @Override
    public String getLastUpdateDateKey() {
        return LAST_UPDATE_KEY;
    }

    @Override
    public String getReachStateDateKey() {
        return REACHED_STATE_DATE_KEY;
    }
}
