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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;

/**
 * @author Elias Ricken de Medeiros
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("message")
public class SWaitingMessageEvent extends SWaitingEvent {

    private String messageName;
    private boolean locked = false;
    private int progress = 0;
    private String correlation1;
    private String correlation2;
    private String correlation3;
    private String correlation4;
    private String correlation5;

    public SWaitingMessageEvent(final SBPMEventType eventType, final long processdefinitionId, final String processName,
            final long flowNodeDefinitionId,
            final String flowNodeName, final String messageName) {
        super(eventType, processdefinitionId, processName, flowNodeDefinitionId, flowNodeName);
        this.messageName = messageName;
    }

    @Override
    public SEventTriggerType getEventTriggerType() {
        return SEventTriggerType.MESSAGE;
    }

}
