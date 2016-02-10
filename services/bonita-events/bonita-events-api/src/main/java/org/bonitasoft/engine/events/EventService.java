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

import java.util.Set;

import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.HandlerUnregistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;

/**
 * This is the manager of all the events triggered by other services. Handlers are registered into the Event service. When a
 * service fire an event, it calls the right handler corresponding to the given Event.
 *
 * @author Christophe Havard
 * @author Baptiste Mesta
 * @since 6.0
 */
public interface EventService {

    /**
     * Fire the specified Event to the registered handlers.
     *
     * @param event
     *            A specific Event
     */
    void fireEvent(final SEvent event) throws SFireEventException;

    /**
     * Allows to check if an handler is listening to this event type
     *
     * @param eventType
     *            the type of the event
     * @return
     *         true if an handler is interested by the event having type eventType
     */
    boolean hasHandlers(final String eventType, EventActionType actionType);

    /**
     * Add the given handler to the Event Manager's handlers list.
     *
     * @param eventType The type of the event the handler is interested in.
     * @param userHandler
     *            The handler to register in the Event Manager
     * @throws HandlerRegistrationException
     */
    void addHandler(final String eventType, final SHandler<SEvent> userHandler) throws HandlerRegistrationException;

    /**
     * Remove the given handler from the Event Service's handlers lists.
     *
     * @param handler
     *            The handler to remove
     */
    void removeAllHandlers(final SHandler<SEvent> handler) throws HandlerUnregistrationException;

    /**
     * Remove the given handler from the given event type filter
     *
     * @param handler
     *            The handler to remove from the given event type
     */
    void removeHandler(final String eventType, final SHandler<SEvent> handler) throws HandlerUnregistrationException;

    /**
     * Retrieve the list of all registered Handlers or the given EventType
     */
    Set<SHandler<SEvent>> getHandlers(String eventType);

}
