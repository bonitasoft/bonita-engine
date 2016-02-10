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

import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public class TimerEventTriggerInstanceImpl extends EventTriggerInstanceImpl implements TimerEventTriggerInstance {

    private static final long serialVersionUID = -1000995843357026775L;

    private Date executionDate;

    private final String eventInstanceName;

    public TimerEventTriggerInstanceImpl(final long id, final long eventInstanceId, final String eventInstanceName, final Date executionDate) {
        super(id, eventInstanceId);
        this.eventInstanceName = eventInstanceName;
        setExecutionDate(executionDate);
    }

    @Override
    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(final Date executionDate) {
        this.executionDate = executionDate;
    }

    @Override
    public String getEventInstanceName() {
        return eventInstanceName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (eventInstanceName == null ? 0 : eventInstanceName.hashCode());
        result = prime * result + (executionDate == null ? 0 : executionDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimerEventTriggerInstanceImpl other = (TimerEventTriggerInstanceImpl) obj;
        if (eventInstanceName == null) {
            if (other.eventInstanceName != null) {
                return false;
            }
        } else if (!eventInstanceName.equals(other.eventInstanceName)) {
            return false;
        }
        if (executionDate == null) {
            if (other.executionDate != null) {
                return false;
            }
        } else if (!executionDate.equals(other.executionDate)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("TimerEventTriggerInstanceImpl [id=").append(getId()).append(", executionDate=").append(executionDate).append(", eventInstanceName=")
                .append(eventInstanceName).append(", eventInstanceId=").append(getEventInstanceId()).append("]");
        return builder.toString();
    }

}
