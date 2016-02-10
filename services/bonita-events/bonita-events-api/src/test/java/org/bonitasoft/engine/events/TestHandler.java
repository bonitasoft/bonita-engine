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
package org.bonitasoft.engine.events;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;

/**
 * @author Christophe Havard
 */
public class TestHandler implements SHandler<SEvent> {

    private static final long serialVersionUID = 1L;

    private boolean isCalled = false;

    private List<SEvent> receivedEvent;

    private final String identifier;

    public TestHandler() {
        this(UUID.randomUUID().toString());
    }

    public TestHandler(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void execute(final SEvent event) {
        setCalled(true);
        if (receivedEvent == null) {
            receivedEvent = new ArrayList<SEvent>();
        }
        receivedEvent.add(event);
    }

    public void setCalled(final boolean isCalled) {
        this.isCalled = isCalled;
    }

    public boolean isCalled() {
        return isCalled;
    }

    public List<SEvent> getReceivedEvents() {
        return receivedEvent;
    }

    @Override
    public boolean isInterested(final SEvent event) {
        return "INTERESTING".equals(event.getType());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isCalled ? 1231 : 1237);
        result = prime * result + ((receivedEvent == null) ? 0 : receivedEvent.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestHandler other = (TestHandler) obj;
        if (isCalled != other.isCalled) {
            return false;
        }
        if (receivedEvent == null) {
            if (other.receivedEvent != null) {
                return false;
            }
        } else if (!receivedEvent.equals(other.receivedEvent)) {
            return false;
        }
        return true;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }



}
