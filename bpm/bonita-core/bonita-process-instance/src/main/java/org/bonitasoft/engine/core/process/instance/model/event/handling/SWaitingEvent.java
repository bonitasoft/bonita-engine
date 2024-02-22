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
package org.bonitasoft.engine.core.process.instance.model.event.handling;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(PersistentObjectId.class)
@Table(name = "waiting_event")
@DiscriminatorColumn(name = "kind")
public abstract class SWaitingEvent implements PersistentObject {

    @Id
    private long id;
    @Id
    private long tenantId;
    @Enumerated(EnumType.STRING)
    private SBPMEventType eventType;
    private String processName;
    private long processDefinitionId;
    private long flowNodeDefinitionId;
    private String flowNodeName;
    private long subProcessId = -1;
    private long parentProcessInstanceId = -1;
    private long rootProcessInstanceId = -1;
    private long flowNodeInstanceId = -1;
    private boolean active = true;

    public SWaitingEvent(final SBPMEventType eventType, final long processdefinitionId, final String processName,
            final long flowNodeDefinitionId,
            final String flowNodeName) {
        this.eventType = eventType;
        this.processName = processName;
        this.flowNodeDefinitionId = flowNodeDefinitionId;
        this.flowNodeName = flowNodeName;
        this.processDefinitionId = processdefinitionId;
    }

    public abstract SEventTriggerType getEventTriggerType();
}
