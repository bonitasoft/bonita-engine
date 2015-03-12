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
package org.bonitasoft.engine.core.process.instance.model.impl;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProcessInstanceImpl extends SNamedElementImpl implements SProcessInstance {

    private static final long serialVersionUID = -6774293249236742878L;

    private static final long DEFAULT_INTERRUPTING_EVENT_ID = -1L;

    private long processDefinitionId;

    private String description;

    private int stateId;

    private long startDate;

    private long startedBy;

    private long startedBySubstitute;

    private long endDate;

    private long lastUpdate;

    private long containerId;

    private long rootProcessInstanceId = -1;

    private long callerId = -1;

    private SFlowNodeType callerType;

    private long interruptingEventId = DEFAULT_INTERRUPTING_EVENT_ID;

    private SStateCategory stateCategory;

    private String stringIndex1;

    private String stringIndex2;

    private String stringIndex3;

    private String stringIndex4;

    private String stringIndex5;

    private long migrationPlanId;

    public SProcessInstanceImpl() {
        super();
    }

    public SProcessInstanceImpl(final String name, final long processDefinitionId) {
        super(name);
        this.processDefinitionId = processDefinitionId;
    }

    public SProcessInstanceImpl(final SProcessDefinition definition) {
        super(definition.getName());
        processDefinitionId = definition.getId();
    }

    @Override
    public void setId(final long id) {
        super.setId(id);
        if (rootProcessInstanceId == -1) {
            rootProcessInstanceId = id;
        }
    }

    @Override
    public String getDiscriminator() {
        return SProcessInstance.class.getName();
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public int getStateId() {
        return stateId;
    }

    public void setStateId(final int stateId) {
        this.stateId = stateId;
    }

    @Override
    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(final long startDate) {
        this.startDate = startDate;
    }

    @Override
    public long getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(final long startedBy) {
        this.startedBy = startedBy;
    }

    @Override
    public long getStartedBySubstitute() {
        return startedBySubstitute;
    }

    public void setStartedBySubstitute(final long startedBySubstitute) {
        this.startedBySubstitute = startedBySubstitute;
    }

    @Override
    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(final long endDate) {
        this.endDate = endDate;
    }

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public long getContainerId() {
        return containerId;
    }

    public void setContainerId(final long containerId) {
        this.containerId = containerId;
    }

    @Override
    public long getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public void setRootProcessInstanceId(final long rootProcessInstanceId) {
        this.rootProcessInstanceId = rootProcessInstanceId;
    }

    @Override
    public long getCallerId() {
        return callerId;
    }

    public void setCallerId(final long callerId) {
        this.callerId = callerId;
    }

    @Override
    public SFlowNodeType getCallerType() {
        return callerType;
    }

    public void setCallerType(final SFlowNodeType callerType) {
        this.callerType = callerType;
    }

    @Override
    public long getInterruptingEventId() {
        return interruptingEventId;
    }

    public void setInterruptingEventId(final long interruptingEventId) {
        this.interruptingEventId = interruptingEventId;
    }

    @Override
    public SStateCategory getStateCategory() {
        return stateCategory;
    }

    public void setStateCategory(final SStateCategory processStateCategory) {
        stateCategory = processStateCategory;
    }

    @Override
    public SFlowElementsContainerType getContainerType() {
        return SFlowElementsContainerType.PROCESS;
    }

    @Override
    public long getMigrationPlanId() {
        return migrationPlanId;
    }

    public void setMigrationPlanId(final long migrationPlanId) {
        this.migrationPlanId = migrationPlanId;
    }

    @Override
    public String getStringIndex1() {
        return stringIndex1;
    }

    public void setStringIndex1(final String stringIndex1) {
        this.stringIndex1 = stringIndex1;
    }

    @Override
    public String getStringIndex2() {
        return stringIndex2;
    }

    public void setStringIndex2(final String stringIndex2) {
        this.stringIndex2 = stringIndex2;
    }

    @Override
    public String getStringIndex3() {
        return stringIndex3;
    }

    public void setStringIndex3(final String stringIndex3) {
        this.stringIndex3 = stringIndex3;
    }

    @Override
    public String getStringIndex4() {
        return stringIndex4;
    }

    public void setStringIndex4(final String stringIndex4) {
        this.stringIndex4 = stringIndex4;
    }

    @Override
    public String getStringIndex5() {
        return stringIndex5;
    }

    public void setStringIndex5(final String stringIndex5) {
        this.stringIndex5 = stringIndex5;
    }

    @Override
    public boolean hasBeenInterruptedByEvent() {
        return getInterruptingEventId() != -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SProcessInstanceImpl)) return false;
        if (!super.equals(o)) return false;

        SProcessInstanceImpl that = (SProcessInstanceImpl) o;

        if (callerId != that.callerId) return false;
        if (containerId != that.containerId) return false;
        if (endDate != that.endDate) return false;
        if (interruptingEventId != that.interruptingEventId) return false;
        if (lastUpdate != that.lastUpdate) return false;
        if (migrationPlanId != that.migrationPlanId) return false;
        if (processDefinitionId != that.processDefinitionId) return false;
        if (rootProcessInstanceId != that.rootProcessInstanceId) return false;
        if (startDate != that.startDate) return false;
        if (startedBy != that.startedBy) return false;
        if (startedBySubstitute != that.startedBySubstitute) return false;
        if (stateId != that.stateId) return false;
        if (callerType != that.callerType) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (stateCategory != that.stateCategory) return false;
        if (stringIndex1 != null ? !stringIndex1.equals(that.stringIndex1) : that.stringIndex1 != null) return false;
        if (stringIndex2 != null ? !stringIndex2.equals(that.stringIndex2) : that.stringIndex2 != null) return false;
        if (stringIndex3 != null ? !stringIndex3.equals(that.stringIndex3) : that.stringIndex3 != null) return false;
        if (stringIndex4 != null ? !stringIndex4.equals(that.stringIndex4) : that.stringIndex4 != null) return false;
        if (stringIndex5 != null ? !stringIndex5.equals(that.stringIndex5) : that.stringIndex5 != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (processDefinitionId ^ (processDefinitionId >>> 32));
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + stateId;
        result = 31 * result + (int) (startDate ^ (startDate >>> 32));
        result = 31 * result + (int) (startedBy ^ (startedBy >>> 32));
        result = 31 * result + (int) (startedBySubstitute ^ (startedBySubstitute >>> 32));
        result = 31 * result + (int) (endDate ^ (endDate >>> 32));
        result = 31 * result + (int) (lastUpdate ^ (lastUpdate >>> 32));
        result = 31 * result + (int) (containerId ^ (containerId >>> 32));
        result = 31 * result + (int) (rootProcessInstanceId ^ (rootProcessInstanceId >>> 32));
        result = 31 * result + (int) (callerId ^ (callerId >>> 32));
        result = 31 * result + (callerType != null ? callerType.hashCode() : 0);
        result = 31 * result + (int) (interruptingEventId ^ (interruptingEventId >>> 32));
        result = 31 * result + (stateCategory != null ? stateCategory.hashCode() : 0);
        result = 31 * result + (stringIndex1 != null ? stringIndex1.hashCode() : 0);
        result = 31 * result + (stringIndex2 != null ? stringIndex2.hashCode() : 0);
        result = 31 * result + (stringIndex3 != null ? stringIndex3.hashCode() : 0);
        result = 31 * result + (stringIndex4 != null ? stringIndex4.hashCode() : 0);
        result = 31 * result + (stringIndex5 != null ? stringIndex5.hashCode() : 0);
        result = 31 * result + (int) (migrationPlanId ^ (migrationPlanId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "SProcessInstanceImpl{" +
                "processDefinitionId=" + processDefinitionId +
                ", description='" + description + '\'' +
                ", stateId=" + stateId +
                ", startDate=" + startDate +
                ", startedBy=" + startedBy +
                ", startedBySubstitute=" + startedBySubstitute +
                ", endDate=" + endDate +
                ", lastUpdate=" + lastUpdate +
                ", containerId=" + containerId +
                ", rootProcessInstanceId=" + rootProcessInstanceId +
                ", callerId=" + callerId +
                ", callerType=" + callerType +
                ", interruptingEventId=" + interruptingEventId +
                ", stateCategory=" + stateCategory +
                ", stringIndex1='" + stringIndex1 + '\'' +
                ", stringIndex2='" + stringIndex2 + '\'' +
                ", stringIndex3='" + stringIndex3 + '\'' +
                ", stringIndex4='" + stringIndex4 + '\'' +
                ", stringIndex5='" + stringIndex5 + '\'' +
                ", migrationPlanId=" + migrationPlanId +
                '}';
    }
}
