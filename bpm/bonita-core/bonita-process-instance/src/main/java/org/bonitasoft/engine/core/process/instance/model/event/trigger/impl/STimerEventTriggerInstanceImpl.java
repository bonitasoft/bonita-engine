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
package org.bonitasoft.engine.core.process.instance.model.event.trigger.impl;

import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SPersistenceObjectImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class STimerEventTriggerInstanceImpl extends SPersistenceObjectImpl implements STimerEventTriggerInstance {

    private long eventInstanceId;
    private long executionDate;
    private String jobTriggerName;
    private String eventInstanceName;

    public STimerEventTriggerInstanceImpl() {
    }

    public STimerEventTriggerInstanceImpl(final long eventInstanceId, final String eventInstanceName, final long executionDate, final String jobTriggerName) {
        this.eventInstanceId = eventInstanceId;
        this.eventInstanceName = eventInstanceName;
        this.setExecutionDate(executionDate);
        this.setJobTriggerName(jobTriggerName);
    }

    @Override
    public long getEventInstanceId() {
        return this.eventInstanceId;
    }

    protected void setEventInstanceId(final long eventInstanceId) {
        this.eventInstanceId = eventInstanceId;
    }

    @Override
    public String getDiscriminator() {
        return STimerEventTriggerInstance.class.getName();
    }

    @Override
    public long getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(long executionDate) {
        this.executionDate = executionDate;
    }

    @Override
    public String getJobTriggerName() {
        return jobTriggerName;
    }

    public void setJobTriggerName(String jobTriggerName) {
        this.jobTriggerName = jobTriggerName;
    }

    @Override
    public String getEventInstanceName() {
        return eventInstanceName;
    }
}
