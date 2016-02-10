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

import org.bonitasoft.engine.bpm.flownode.BPMEventType;
import org.bonitasoft.engine.bpm.flownode.WaitingSignalEvent;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class WaitingSignalEventImpl extends WaitingEventImpl implements WaitingSignalEvent {

    private static final long serialVersionUID = 6899269154378362388L;

    private String signalName;

    public WaitingSignalEventImpl() {
        super();
    }

    public WaitingSignalEventImpl(final BPMEventType eventType, final long processdefinitionId, final String processName, final long flowNodeDefinitionId,
            final String signalName) {
        super(eventType, processdefinitionId, processName, flowNodeDefinitionId);
        this.signalName = signalName;
    }

    @Override
    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(final String signalName) {
        this.signalName = signalName;
    }

}
