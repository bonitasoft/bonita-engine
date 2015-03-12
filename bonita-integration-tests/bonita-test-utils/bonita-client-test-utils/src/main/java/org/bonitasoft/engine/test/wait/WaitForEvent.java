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
package org.bonitasoft.engine.test.wait;

import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.EventCriterion;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.WaitUntil;

@Deprecated
public class WaitForEvent extends WaitUntil {

    private final String eventName;

    private final long processInstanceId;

    private String state = null;

    private EventInstance result;

    private final ProcessAPI processAPI;

    @Deprecated
    public WaitForEvent(final int repeatEach, final int timeout, final String eventName, final long processInstanceId, final ProcessAPI processAPI) {
        super(repeatEach, timeout);
        this.eventName = eventName;
        this.processInstanceId = processInstanceId;
        this.processAPI = processAPI;
    }

    @Deprecated
    public WaitForEvent(final int repeatEach, final int timeout, final String eventName, final long processInstanceId, final TestStates state,
            final ProcessAPI processAPI) {
        this(repeatEach, timeout, eventName, processInstanceId, processAPI);
        this.state = state.getStateName();
    }

    @Override
    protected boolean check() {
        final List<EventInstance> eventInstances = processAPI.getEventInstances(processInstanceId, 0, 10, EventCriterion.NAME_ASC);
        boolean found = false;
        final Iterator<EventInstance> iterator = eventInstances.iterator();
        while (iterator.hasNext() && !found) {
            final EventInstance eventInstance = iterator.next();
            if (eventInstance.getName().equals(eventName)) {
                if (state == null) {
                    found = true;
                    result = eventInstance;
                } else {
                    found = state.equals(eventInstance.getState());
                    result = eventInstance;
                }
            }
        }
        return found;
    }

    public EventInstance getResult() {
        return result;
    }
}
