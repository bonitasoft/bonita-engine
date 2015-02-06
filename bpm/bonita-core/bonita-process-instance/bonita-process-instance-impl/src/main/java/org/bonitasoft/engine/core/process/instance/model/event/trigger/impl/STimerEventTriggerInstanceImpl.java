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

import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public class STimerEventTriggerInstanceImpl extends SEventTriggerInstanceImpl implements STimerEventTriggerInstance {

    private static final long serialVersionUID = -5079198882177095287L;

    private long executionDate;

    private String jobTriggerName;

    private String eventInstanceName;

    public STimerEventTriggerInstanceImpl() {
    }

    public STimerEventTriggerInstanceImpl(final long eventInstanceId, final String eventInstanceName, final long executionDate, final String jobTriggerName) {
        super(eventInstanceId);
        this.eventInstanceName = eventInstanceName;
        this.setExecutionDate(executionDate);
        this.setJobTriggerName(jobTriggerName);
    }

    @Override
    public String getDiscriminator() {
        return STimerEventTriggerInstance.class.getName();
    }

    @Override
    public SEventTriggerType getEventTriggerType() {
        return SEventTriggerType.TIMER;
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
