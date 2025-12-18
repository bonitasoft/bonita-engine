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
package org.bonitasoft.engine.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.events.impl.EventServiceImpl;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.HandlerUnregistrationException;
import org.bonitasoft.engine.events.model.SFireEventException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Laurent Vaills
 */
public class EventServiceImplTest {

    protected EventService instantiateEventServiceImplementation() {
        return new EventServiceImpl();
    }

    private EventService eventSvc;

    // Test events
    private final String EVT_INTERESTING = "INTERESTING";

    private final String EVT_IRRELEVANT = "IRRELEVANT";

    @Before
    public void beforeEachTest() {
        eventSvc = instantiateEventServiceImplementation();
    }

    @Test(expected = SFireEventException.class)
    public void fireNullEvent() throws Exception {
        eventSvc.fireEvent(null);
    }

    @Test(expected = HandlerRegistrationException.class)
    public void addHandlerNull() throws Exception {
        eventSvc.addHandler(EVT_INTERESTING, null);
    }

    @Test(expected = HandlerRegistrationException.class)
    public void addTwiceTheSameHandler() throws Exception {
        final TestHandler h = new TestHandler();
        eventSvc.addHandler(EVT_INTERESTING, h);
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).hasSize(1);
        eventSvc.addHandler(EVT_INTERESTING, h);
    }

    @Test
    public void addHandlerInEventFilters() throws Exception {
        final TestHandler h = new TestHandler();
        eventSvc.addHandler(EVT_INTERESTING, h);
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).contains(h);
        eventSvc.removeHandler(EVT_INTERESTING, h);
    }

    @Test
    public void addNewTypeInRegisteredHandlers() throws Exception {
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).isEmpty();

        final TestHandler h = new TestHandler();
        eventSvc.addHandler(EVT_INTERESTING, h);
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).isNotEmpty();

        eventSvc.removeHandler(EVT_INTERESTING, h);
    }

    @Test(expected = HandlerUnregistrationException.class)
    public void removeUnknownHandler() throws Exception {
        final TestHandler h = new TestHandler();
        eventSvc.removeHandler(EVT_INTERESTING, h);
    }

    @Test
    public void removeHandler() throws Exception {
        final TestHandler h = new TestHandler();
        eventSvc.addHandler(EVT_INTERESTING, h);
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).hasSize(1);

        eventSvc.removeHandler(EVT_INTERESTING, h);
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).isEmpty();
    }

    @Test
    public void isEventFiltered()
            throws HandlerRegistrationException, SFireEventException, HandlerUnregistrationException {
        // Register handler on a given event type.
        final TestHandlerCallback h = new TestHandlerCallback();
        eventSvc.addHandler(EVT_INTERESTING, h);

        // Fire 2 different events
        final TestEvent interesting = new TestEvent(EVT_INTERESTING);
        final TestEvent irrelevant = new TestEvent(EVT_IRRELEVANT);
        eventSvc.fireEvent(interesting);
        eventSvc.fireEvent(irrelevant);

        // Check that only "interesting" events have been received by the registered handler
        assertThat(irrelevant.isFlagged()).isFalse();
        assertThat(interesting.isFlagged()).isTrue();

        eventSvc.removeHandler(EVT_INTERESTING, h);
    }

    @Test
    public void isEventReceivedByHandler()
            throws SFireEventException, HandlerRegistrationException, HandlerUnregistrationException {
        final TestHandlerCallback h = new TestHandlerCallback();
        final TestEvent interesting = new TestEvent(EVT_INTERESTING);

        eventSvc.addHandler(EVT_INTERESTING, h);
        eventSvc.fireEvent(interesting);

        assertThat(interesting.isFlagged()).isTrue();
        eventSvc.removeHandler(EVT_INTERESTING, h);
    }

    @Test
    public void getAllHandlersByEvent() throws HandlerRegistrationException, HandlerUnregistrationException {
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).isEmpty();

        // add 2 different handlers for 1 event type
        final TestHandler h1 = new TestHandler();
        final TestHandler h2 = new TestHandler();

        eventSvc.addHandler(EVT_INTERESTING, h1);
        eventSvc.addHandler(EVT_INTERESTING, h2);

        // now i check if the evtList contains my both handlers
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).isNotEmpty();

        eventSvc.removeHandler(EVT_INTERESTING, h1);
        eventSvc.removeHandler(EVT_INTERESTING, h2);
    }

    @Test
    public void registerHandlerIfNotExists_shouldRegisterWhenNoHandlerExists() throws HandlerRegistrationException {
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).isEmpty();

        final TestHandler handler = new TestHandler();
        eventSvc.registerHandlerIfNotExists(EVT_INTERESTING, handler);

        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).hasSize(1);
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).contains(handler);
    }

    @Test
    public void registerHandlerIfNotExists_shouldNotRegisterWhenHandlerAlreadyExists()
            throws HandlerRegistrationException {
        final TestHandler handler = new TestHandler();

        // Register handler first time
        eventSvc.registerHandlerIfNotExists(EVT_INTERESTING, handler);
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).hasSize(1);

        // Try to register same handler again (same identifier)
        eventSvc.registerHandlerIfNotExists(EVT_INTERESTING, handler);

        // Should still have only 1 handler
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).hasSize(1);
    }

    @Test
    public void registerHandlerIfNotExists_shouldRegisterDifferentHandlers() throws HandlerRegistrationException {
        // Create handlers with explicit different identifiers
        final TestHandler handler1 = new TestHandler("handler-1");
        final TestHandler handler2 = new TestHandler("handler-2");

        eventSvc.registerHandlerIfNotExists(EVT_INTERESTING, handler1);
        eventSvc.registerHandlerIfNotExists(EVT_INTERESTING, handler2);

        // Should have 2 handlers with different identifiers
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).hasSize(2);
    }

    @Test
    public void registerHandlerIfNotExists_shouldBeIdempotentAcrossMultipleCalls()
            throws HandlerRegistrationException {
        final TestHandler handler = new TestHandler();

        // Register multiple times
        eventSvc.registerHandlerIfNotExists(EVT_INTERESTING, handler);
        eventSvc.registerHandlerIfNotExists(EVT_INTERESTING, handler);
        eventSvc.registerHandlerIfNotExists(EVT_INTERESTING, handler);

        // Should still have only 1 handler
        assertThat(eventSvc.getHandlers(EVT_INTERESTING)).hasSize(1);
    }

}
