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

import org.bonitasoft.engine.bpm.flownode.BPMEventType;
import org.bonitasoft.engine.bpm.flownode.WaitingEvent;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public abstract class WaitingEventImpl implements WaitingEvent {

    private static final long serialVersionUID = -4479053804299166959L;

    private BPMEventType eventType;

    private String processName;

    private long flowNodeDefinitionId = -1;

    private long processDefinitionId;

    private long rootProcessInstanceId = -1;

    private long parentProcessInstanceId = -1;

    private long flowNodeInstanceId = -1;

    private boolean active = true;

    public WaitingEventImpl() {
    }

    public WaitingEventImpl(final BPMEventType eventType, final long processDefinitionId, final String processName, final long flowNodeDefinitionId) {
        this.eventType = eventType;
        this.processName = processName;
        this.flowNodeDefinitionId = flowNodeDefinitionId;
        this.processDefinitionId = processDefinitionId;
    }

    public WaitingEventImpl(final BPMEventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public BPMEventType getEventType() {
        return eventType;
    }

    public void setEventType(final BPMEventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String getProcessName() {
        return processName;
    }

    @Override
    public long getFlowNodeDefinitionId() {
        return flowNodeDefinitionId;
    }

    public void setProcessName(final String processName) {
        this.processName = processName;
    }

    public void setFlowNodeDefinitionId(final long flowNodeDefinitionId) {
        this.flowNodeDefinitionId = flowNodeDefinitionId;
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(final long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public long getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public void setRootProcessInstanceId(final long processInstanceId) {
        rootProcessInstanceId = processInstanceId;
    }

    @Override
    public long getParentProcessInstanceId() {
        return parentProcessInstanceId;
    }

    public void setParentProcessInstanceId(final long parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
    }

    @Override
    public long getFlowNodeInstanceId() {
        return flowNodeInstanceId;
    }

    public void setFlowNodeInstanceId(final long flowNodeInstanceId) {
        this.flowNodeInstanceId = flowNodeInstanceId;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

}
