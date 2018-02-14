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
package org.bonitasoft.engine.core.process.instance.model.event.handling.impl;

import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.impl.SPersistenceObjectImpl;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public abstract class SWaitingEventImpl extends SPersistenceObjectImpl implements SWaitingEvent {

    private static final long serialVersionUID = -7335062679913545084L;

    private SBPMEventType eventType;

    private String processName;

    private String flowNodeName;

    private long flowNodeDefinitionId;

    private long processDefinitionId;

    private long rootProcessInstanceId = -1;

    private long parentProcessInstanceId = -1;

    private long flowNodeInstanceId = -1;

    private boolean active = true;

    private long subProcessId = -1;

    public SWaitingEventImpl() {
    }

    public SWaitingEventImpl(final SBPMEventType eventType, final long processdefinitionId, final String processName, final long flowNodeDefinitionId,
            final String flowNodeName) {
        this.eventType = eventType;
        this.processName = processName;
        this.flowNodeDefinitionId = flowNodeDefinitionId;
        this.flowNodeName = flowNodeName;
        this.processDefinitionId = processdefinitionId;
    }

    public SWaitingEventImpl(final SBPMEventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public SBPMEventType getEventType() {
        return eventType;
    }

    public void setEventType(final SBPMEventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String getProcessName() {
        return processName;
    }

    public void setProcessName(final String processName) {
        this.processName = processName;
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(final long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public long getFlowNodeDefinitionId() {
        return flowNodeDefinitionId;
    }

    public void setFlowNodeDefinitionId(long flowNodeDefinitionId) {
        this.flowNodeDefinitionId = flowNodeDefinitionId;
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

    @Override
    public String getFlowNodeName() {
        return flowNodeName;
    }

    public void setFlowNodeName(String flowNodeName) {
        this.flowNodeName = flowNodeName;
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

    @Override
    public long getSubProcessId() {
        return subProcessId;
    }

    public void setSubProcessId(long subProcessId) {
        this.subProcessId = subProcessId;
    }

}
