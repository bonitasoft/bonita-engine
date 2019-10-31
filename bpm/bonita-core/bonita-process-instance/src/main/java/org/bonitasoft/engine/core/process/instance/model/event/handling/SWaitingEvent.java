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
package org.bonitasoft.engine.core.process.instance.model.event.handling;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
@Data
@NoArgsConstructor
public abstract class SWaitingEvent implements PersistentObject {

    private long id;
    private long tenantId;
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

    public SWaitingEvent(final SBPMEventType eventType, final long processdefinitionId, final String processName, final long flowNodeDefinitionId,
                             final String flowNodeName) {
        this.eventType = eventType;
        this.processName = processName;
        this.flowNodeDefinitionId = flowNodeDefinitionId;
        this.flowNodeName = flowNodeName;
        this.processDefinitionId = processdefinitionId;
    }

    public abstract SEventTriggerType getEventTriggerType();
}
