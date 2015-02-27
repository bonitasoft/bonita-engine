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

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;


public class ProcessInstanceBuilder extends PersistentObjectBuilder<SProcessInstanceImpl, ProcessInstanceBuilder> {

    private long processDefinitionId;

    private String name;

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

    private long interruptingEventId = -1;

    private SStateCategory stateCategory;

    @Override
    ProcessInstanceBuilder getThisBuilder() {
        return this;
    }

    public static ProcessInstanceBuilder aProcessInstance() {
        return new ProcessInstanceBuilder();
    }
    
    @Override
    SProcessInstanceImpl _build() {
        SProcessInstanceImpl processInstance = new SProcessInstanceImpl(name, processDefinitionId);
        processInstance.setCallerId(this.callerId);
        processInstance.setCallerType(this.callerType);
        processInstance.setContainerId(this.containerId);
        processInstance.setDescription(description);
        processInstance.setEndDate(endDate);
        processInstance.setInterruptingEventId(interruptingEventId);
        processInstance.setLastUpdate(lastUpdate);
        processInstance.setRootProcessInstanceId(rootProcessInstanceId);
        processInstance.setStartDate(startDate);
        processInstance.setStartedBy(startedBy);
        processInstance.setStartedBySubstitute(startedBySubstitute);
        processInstance.setStateCategory(stateCategory);
        processInstance.setStateId(stateId);
        return processInstance;
    }

    public ProcessInstanceBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public ProcessInstanceBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProcessInstanceBuilder withProcessDefinitionId(long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }
    
    public ProcessInstanceBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public ProcessInstanceBuilder withStateId(final int stateId) {
        this.stateId = stateId;
        return this;
    }

    public ProcessInstanceBuilder withStartDate(final long startDate) {
        this.startDate = startDate;
        return this;
    }

    public ProcessInstanceBuilder withStartedBy(final long startedBy) {
        this.startedBy = startedBy;
        return this;
    }

    public ProcessInstanceBuilder withStartedBySubstitute(final long startedBySubstitute) {
        this.startedBySubstitute = startedBySubstitute;
        return this;
    }

    public ProcessInstanceBuilder withEndDate(final long endDate) {
        this.endDate = endDate;
        return this;
    }

    public ProcessInstanceBuilder withLastUpdate(final long lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public ProcessInstanceBuilder withContainerId(final long containerId) {
        this.containerId = containerId;
        return this;
    }

    public ProcessInstanceBuilder withRootProcessInstanceId(final long rootProcessInstanceId) {
        this.rootProcessInstanceId = rootProcessInstanceId;
        return this;
    }

    public ProcessInstanceBuilder withCallerId(final long callerId) {
        this.callerId = callerId;
        return this;
    }

    public ProcessInstanceBuilder withCallerType(final SFlowNodeType callerType) {
        this.callerType = callerType;
        return this;
    }

    public ProcessInstanceBuilder withInterruptingEventId(final long interruptingEventId) {
        this.interruptingEventId = interruptingEventId;
        return this;
    }

    public ProcessInstanceBuilder withStateCategory(final SStateCategory processStateCategory) {
        stateCategory = processStateCategory;
        return this;
    }
}
