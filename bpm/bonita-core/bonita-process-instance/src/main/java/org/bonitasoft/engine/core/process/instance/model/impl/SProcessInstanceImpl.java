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

import java.util.Objects;

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
    public boolean isRootInstance() {
        return callerId <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        SProcessInstanceImpl that = (SProcessInstanceImpl) o;
        return Objects.equals(processDefinitionId, that.processDefinitionId) &&
                Objects.equals(stateId, that.stateId) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(startedBy, that.startedBy) &&
                Objects.equals(startedBySubstitute, that.startedBySubstitute) &&
                Objects.equals(endDate, that.endDate) &&
                Objects.equals(lastUpdate, that.lastUpdate) &&
                Objects.equals(containerId, that.containerId) &&
                Objects.equals(rootProcessInstanceId, that.rootProcessInstanceId) &&
                Objects.equals(callerId, that.callerId) &&
                Objects.equals(interruptingEventId, that.interruptingEventId) &&
                Objects.equals(description, that.description) &&
                Objects.equals(callerType, that.callerType) &&
                Objects.equals(stateCategory, that.stateCategory) &&
                Objects.equals(stringIndex1, that.stringIndex1) &&
                Objects.equals(stringIndex2, that.stringIndex2) &&
                Objects.equals(stringIndex3, that.stringIndex3) &&
                Objects.equals(stringIndex4, that.stringIndex4) &&
                Objects.equals(stringIndex5, that.stringIndex5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), processDefinitionId, description, stateId, startDate, startedBy, startedBySubstitute, endDate, lastUpdate,
                containerId, rootProcessInstanceId, callerId, callerType, interruptingEventId, stateCategory, stringIndex1, stringIndex2, stringIndex3,
                stringIndex4, stringIndex5);
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
                "} " + super.toString();
    }
}
