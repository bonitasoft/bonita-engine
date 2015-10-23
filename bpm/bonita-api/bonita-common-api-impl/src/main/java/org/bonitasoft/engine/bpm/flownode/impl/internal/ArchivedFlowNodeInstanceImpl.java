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
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public abstract class ArchivedFlowNodeInstanceImpl extends NamedElementImpl implements ArchivedFlowNodeInstance {

    private static final long serialVersionUID = -6573747806944970703L;
    private long parentContainerId;

    private Date archiveDate;

    private String state;

    private long rootContainerId;

    private long processDefinitionId;

    private long processInstanceId;

    private long parentActivityInstanceId;

    private String displayName;

    private String displayDescription;

    private long sourceObjectId;

    private String description;

    private long executedBy;

    private long executedBySubstitute;

    private long flownodeDefinitionId;

    private boolean terminal;

    protected Date reachedStateDate;

    protected Date lastUpdateDate;

    public ArchivedFlowNodeInstanceImpl(final String name) {
        super(name);
    }

    @Override
    public long getParentContainerId() {
        return parentContainerId;
    }

    public void setParentContainerId(long parentContainerId) {
        this.parentContainerId = parentContainerId;
    }

    @Override
    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public long getParentActivityInstanceId() {
        return parentActivityInstanceId;
    }

    public void setParentActivityInstanceId(long parentActivityInstanceId) {
        this.parentActivityInstanceId = parentActivityInstanceId;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDisplayDescription() {
        return displayDescription;
    }

    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
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
    public void setExecutedByDelegate(long executedBySubstitute) {
        setExecutedBySubstitute(executedBySubstitute);
    }

    @Override
    public long getFlownodeDefinitionId() {
        return flownodeDefinitionId;
    }

    public void setFlownodeDefinitionId(long flownodeDefinitionId) {
        this.flownodeDefinitionId = flownodeDefinitionId;
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    public Date getReachedStateDate() {
        return reachedStateDate;
    }

    public void setReachedStateDate(Date reachedStateDate) {
        this.reachedStateDate = reachedStateDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parentContainerId, archiveDate, state, rootContainerId, processDefinitionId, processInstanceId,
                parentActivityInstanceId, displayName, displayDescription, sourceObjectId, description, executedBy, executedBySubstitute, flownodeDefinitionId,
                terminal, reachedStateDate, lastUpdateDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ArchivedFlowNodeInstanceImpl))
            return false;
        if (!super.equals(o))
            return false;
        ArchivedFlowNodeInstanceImpl that = (ArchivedFlowNodeInstanceImpl) o;
        return Objects.equals(parentContainerId, that.parentContainerId) &&
                Objects.equals(rootContainerId, that.rootContainerId) &&
                Objects.equals(processDefinitionId, that.processDefinitionId) &&
                Objects.equals(processInstanceId, that.processInstanceId) &&
                Objects.equals(parentActivityInstanceId, that.parentActivityInstanceId) &&
                Objects.equals(sourceObjectId, that.sourceObjectId) &&
                Objects.equals(executedBy, that.executedBy) &&
                Objects.equals(executedBySubstitute, that.executedBySubstitute) &&
                Objects.equals(flownodeDefinitionId, that.flownodeDefinitionId) &&
                Objects.equals(terminal, that.terminal) &&
                Objects.equals(archiveDate, that.archiveDate) &&
                Objects.equals(state, that.state) &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(displayDescription, that.displayDescription) &&
                Objects.equals(description, that.description) &&
                Objects.equals(reachedStateDate, that.reachedStateDate) &&
                Objects.equals(lastUpdateDate, that.lastUpdateDate);
    }

    @Override
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(final Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("parentContainerId", parentContainerId)
                .append("archiveDate", archiveDate)
                .append("state", state)
                .append("rootContainerId", rootContainerId)
                .append("processDefinitionId", processDefinitionId)
                .append("processInstanceId", processInstanceId)
                .append("parentActivityInstanceId", parentActivityInstanceId)
                .append("displayName", displayName)
                .append("displayDescription", displayDescription)
                .append("sourceObjectId", sourceObjectId)
                .append("description", description)
                .append("executedBy", executedBy)
                .append("executedBySubstitute", executedBySubstitute)
                .append("flownodeDefinitionId", flownodeDefinitionId)
                .append("terminal", terminal)
                .append("reachedStateDate", reachedStateDate)
                .append("lastUpdateDate", lastUpdateDate)
                .toString();
    }
}
