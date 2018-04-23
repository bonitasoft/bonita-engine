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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.StateCategory;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public abstract class FlowNodeInstanceImpl extends NamedElementImpl implements FlowNodeInstance {

    private static final long serialVersionUID = -6573747806944970703L;

    private long parentContainerId;

    private String state;

    private StateCategory stateCategory;

    private long rootContainerId;

    private long processDefinitionId;

    private long parentProcessInstanceId;

    private String displayDescription;

    private String displayName;

    private String description;

    private long executedBy;

    private long executedBySubstitute;

    private long flownodeDefinitionId;

    private Date reachedStateDate;

    private Date lastUpdateDate;

    public FlowNodeInstanceImpl(final String name, final long flownodeDefinitionId) {
        super(name);

        this.flownodeDefinitionId = flownodeDefinitionId;
    }

    @Override
    public long getParentContainerId() {
        return parentContainerId;
    }

    public void setParentContainerId(long parentContainerId) {
        this.parentContainerId = parentContainerId;
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public StateCategory getStateCategory() {
        return stateCategory;
    }

    public void setStateCategory(StateCategory stateCategory) {
        this.stateCategory = stateCategory;
    }

    @Override
    public long getRootContainerId() {
        return rootContainerId;
    }

    public void setRootContainerId(long rootContainerId) {
        this.rootContainerId = rootContainerId;
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public long getParentProcessInstanceId() {
        return parentProcessInstanceId;
    }

    public void setParentProcessInstanceId(long parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
    }

    @Override
    public String getDisplayDescription() {
        return displayDescription;
    }

    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public long getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(long executedBy) {
        this.executedBy = executedBy;
    }

    @Override
    public long getExecutedBySubstitute() {
        return executedBySubstitute;
    }

    public void setExecutedBySubstitute(long executedBySubstitute) {
        this.executedBySubstitute = executedBySubstitute;
    }

    @Deprecated
    @Override
    public long getExecutedByDelegate() {
        return getExecutedBySubstitute();
    }

    @Deprecated
    public void setExecutedByDelegate(long executedByDelegate) {
        setExecutedBySubstitute(executedByDelegate);
    }

    @Override
    public long getFlownodeDefinitionId() {
        return flownodeDefinitionId;
    }

    public void setFlownodeDefinitionId(long flownodeDefinitionId) {
        this.flownodeDefinitionId = flownodeDefinitionId;
    }

    public Date getReachedStateDate() {
        return reachedStateDate;
    }

    public void setReachedSateDate(final Date reachedStateDate) {
        this.reachedStateDate = reachedStateDate;
    }

    @Override
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(final Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parentContainerId, state, stateCategory, rootContainerId, processDefinitionId, parentProcessInstanceId,
                displayDescription, displayName, description, executedBy, executedBySubstitute, flownodeDefinitionId, reachedStateDate, lastUpdateDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FlowNodeInstanceImpl))
            return false;
        if (!super.equals(o))
            return false;
        FlowNodeInstanceImpl that = (FlowNodeInstanceImpl) o;
        return Objects.equals(parentContainerId, that.parentContainerId) &&
                Objects.equals(rootContainerId, that.rootContainerId) &&
                Objects.equals(processDefinitionId, that.processDefinitionId) &&
                Objects.equals(parentProcessInstanceId, that.parentProcessInstanceId) &&
                Objects.equals(executedBy, that.executedBy) &&
                Objects.equals(executedBySubstitute, that.executedBySubstitute) &&
                Objects.equals(flownodeDefinitionId, that.flownodeDefinitionId) &&
                Objects.equals(state, that.state) &&
                Objects.equals(stateCategory, that.stateCategory) &&
                Objects.equals(displayDescription, that.displayDescription) &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(description, that.description) &&
                Objects.equals(reachedStateDate, that.reachedStateDate) &&
                Objects.equals(lastUpdateDate, that.lastUpdateDate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("parentContainerId", parentContainerId)
                .append("state", state)
                .append("stateCategory", stateCategory)
                .append("rootContainerId", rootContainerId)
                .append("processDefinitionId", processDefinitionId)
                .append("parentProcessInstanceId", parentProcessInstanceId)
                .append("displayDescription", displayDescription)
                .append("displayName", displayName)
                .append("description", description)
                .append("executedBy", executedBy)
                .append("executedBySubstitute", executedBySubstitute)
                .append("flownodeDefinitionId", flownodeDefinitionId)
                .append("reachedStateDate", reachedStateDate)
                .append("lastUpdateDate", lastUpdateDate)
                .toString();
    }
}
