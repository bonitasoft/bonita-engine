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
package org.bonitasoft.engine.bpm.process.impl;

import java.util.Date;

import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessInstanceImpl;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ProcessInstanceBuilder {

    protected final ProcessInstanceImpl processInstance;

    private ProcessInstanceBuilder() {
        this.processInstance = null;
    }
    
    private ProcessInstanceBuilder(final ProcessInstanceImpl processInstance) {
        super();
        this.processInstance = processInstance;
    }

    public ProcessInstance done() {
        return processInstance;
    }

    public ProcessInstanceBuilder createNewInstance(final String name) {
        final ProcessInstanceImpl processInstance = new ProcessInstanceImpl(name);
        return new ProcessInstanceBuilder(processInstance);
    }

    public ProcessInstanceBuilder setState(final String state) {
        processInstance.setState(state);
        return this;
    }

    public ProcessInstanceBuilder setStartDate(final long startDate) {
        processInstance.setStartDate(new Date(startDate));
        return this;
    }

    public ProcessInstanceBuilder setStartedBy(final long startedBy) {
        processInstance.setStartedBy(startedBy);
        return this;
    }

    public ProcessInstanceBuilder setStartedBySubstitute(final long startedBySubstitute) {
        processInstance.setStartedBySubstitute(startedBySubstitute);
        return this;
    }

    public ProcessInstanceBuilder setEndDate(final long endDate) {
        processInstance.setEndDate(new Date(endDate));
        return this;
    }

    public ProcessInstanceBuilder setLastUpdate(final long lastUpdate) {
        processInstance.setLastUpdate(new Date(lastUpdate));
        return this;
    }

    public ProcessInstanceBuilder setProcessDefinitionId(final long processDefinitionId) {
        processInstance.setProcessDefinitionId(processDefinitionId);
        return this;
    }

    public ProcessInstanceBuilder setDescription(final String description) {
        processInstance.setDescription(description);
        return this;
    }

    public ProcessInstanceBuilder setId(final long id) {
        processInstance.setId(id);
        return this;
    }

    public ProcessInstanceBuilder setRootProcessInstanceId(final long rootProcessInstanceId) {
        processInstance.setRootProcessInstanceId(rootProcessInstanceId);
        return this;
    }

    public ProcessInstanceBuilder setCallerId(final long callerId) {
        processInstance.setCallerId(callerId);
        return this;
    }

    public ProcessInstanceBuilder setStringIndex1(final String stringIndex1) {
        processInstance.setStringIndex1(stringIndex1);
        return this;
    }

    public ProcessInstanceBuilder setStringIndex2(final String stringIndex2) {
        processInstance.setStringIndex2(stringIndex2);
        return this;
    }

    public ProcessInstanceBuilder setStringIndex3(final String stringIndex3) {
        processInstance.setStringIndex3(stringIndex3);
        return this;
    }

    public ProcessInstanceBuilder setStringIndex4(final String stringIndex4) {
        processInstance.setStringIndex4(stringIndex4);
        return this;
    }

    public ProcessInstanceBuilder setStringIndex5(final String stringIndex5) {
        processInstance.setStringIndex5(stringIndex5);
        return this;
    }

    public void setStringIndexLabel(final int index, final String stringIndexLabel) {
        processInstance.setStringIndexLabel(index, stringIndexLabel);
    }

    public static ProcessInstanceBuilder getInstance() {
        return new ProcessInstanceBuilder(null);
    }

}
