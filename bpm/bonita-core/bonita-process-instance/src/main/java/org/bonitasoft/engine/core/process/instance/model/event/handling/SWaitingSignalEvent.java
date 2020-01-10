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
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("signal")
public class SWaitingSignalEvent extends SWaitingEvent {

    private String signalName;

    public SWaitingSignalEvent(final SBPMEventType eventType, final long processdefinitionId, final String processName,
            final long flowNodeDefinitionId,
            final String flowNodeName, final String signalName) {
        super(eventType, processdefinitionId, processName, flowNodeDefinitionId, flowNodeName);
        this.signalName = signalName;
    }

    @Override
    public SEventTriggerType getEventTriggerType() {
        return SEventTriggerType.SIGNAL;
    }

}
