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

import java.util.UUID;

import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;

/**
 * @author Christophe Havard
 */
public class TestHandlerCallback implements SHandler<SEvent> {

    private static final long serialVersionUID = 1L;

    private final String identifier;

    public TestHandlerCallback() {
        this(UUID.randomUUID().toString());
    }

    public TestHandlerCallback(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void execute(final SEvent event) {
        TestEvent testEvent = (TestEvent) event;
        testEvent.flag();
    }

    @Override
    public boolean isInterested(final SEvent event) {
        return "INTERESTING".equals(event.getType());
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

}
