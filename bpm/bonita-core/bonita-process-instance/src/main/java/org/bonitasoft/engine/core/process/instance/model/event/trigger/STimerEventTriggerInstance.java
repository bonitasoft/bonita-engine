/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.instance.model.event.trigger;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Elias Ricken de Medeiros
 */
@Data
@NoArgsConstructor
@Entity
@IdClass(PersistentObjectId.class)
@Table(name = "event_trigger_instance")
public class STimerEventTriggerInstance implements PersistentObject {

    public static final String EXECUTION_DATE = "executionDate";

    @Id
    private long id;
    @Id
    private long tenantId;
    private long eventInstanceId;

    /**
     * @return The date of the execution of the trigger
     * @since 6.4.0
     */
    private long executionDate;

    /**
     * @return The name of the trigger of the job
     * @since 6.4.0
     */
    private String jobTriggerName;

    /**
     * @return The name of the {@link org.bonitasoft.engine.core.process.instance.model.event.SEventInstance}
     * @since 6.4.0
     */
    private String eventInstanceName;

    public STimerEventTriggerInstance(final long eventInstanceId, final String eventInstanceName,
            final long executionDate, final String jobTriggerName) {
        this.eventInstanceId = eventInstanceId;
        this.eventInstanceName = eventInstanceName;
        this.executionDate = executionDate;
        this.jobTriggerName = jobTriggerName;
    }
}
