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
package org.bonitasoft.engine.core.process.instance.model.event.handling.impl;

import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SWaitingSignalEventImpl extends SWaitingEventImpl implements SWaitingSignalEvent {

    private static final long serialVersionUID = -2199062045232102189L;

    private String signalName;

    public SWaitingSignalEventImpl() {
        super();
    }

    public SWaitingSignalEventImpl(final SBPMEventType eventType, final long processdefinitionId, final String processName, final long flowNodeDefinitionId,
            final String flowNodeName, final String signalName) {
        super(eventType, processdefinitionId, processName, flowNodeDefinitionId, flowNodeName);
        this.signalName = signalName;
    }

    @Override
    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(final String signalName) {
        this.signalName = signalName;
    }

    @Override
    public String getDiscriminator() {
        return SWaitingSignalEvent.class.getName();
    }

    @Override
    public SEventTriggerType getEventTriggerType() {
        return SEventTriggerType.SIGNAL;
    }

}
