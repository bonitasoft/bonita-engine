package org.bonitasoft.engine.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.engine.ServicesBuilder;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.HandlerUnregistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.junit.Test;

/**
 * @author Christophe Havard
 */
public class EventServiceTest {

    private static ServicesBuilder serviceBuilder;

    private static EventService eventSvc;

    // Test events
    private final String EVT_INTERESTING = "INTERESTING";

    private final String EVT_IRRELEVANT = "IRRELEVANT";

    static {
        serviceBuilder = new ServicesBuilder();
        eventSvc = serviceBuilder.buildEventService();
    }

    @Test(expected = FireEventException.class)
    public void fireNullEvent() throws Exception {
        eventSvc.fireEvent(null);
    }

    @Test(expected = HandlerRegistrationException.class)
    public void addHandlerNull() throws Exception {
        eventSvc.addHandler(EVT_INTERESTING, null);
    }

    @Test
    public void addHandlerInEventfilters() throws Exception {
        final TestHandler h = new TestHandler();
        eventSvc.addHandler(EVT_INTERESTING, h);
        final Set<SHandler<SEvent>> newHandlerSet = new HashSet<SHandler<SEvent>>();
        newHandlerSet.add(h);
        assertTrue(eventSvc.getRegisteredHandlers().containsValue(newHandlerSet));
        eventSvc.removeHandler(EVT_INTERESTING, h);
    }

    @Test
    public void addNewTypeInRegisteredHandlers() throws Exception {
        final TestHandler h = new TestHandler();
        eventSvc.addHandler(EVT_INTERESTING, h);
        assertTrue(eventSvc.getRegisteredHandlers().containsKey(EVT_INTERESTING));
        eventSvc.removeHandler(EVT_INTERESTING, h);
    }

    @Test(expected = HandlerRegistrationException.class)
    public void handlerAlreadyExists() throws Exception {
        final TestHandler h = new TestHandler();
        eventSvc.addHandler(EVT_INTERESTING, h);
        try {
            eventSvc.addHandler(EVT_INTERESTING, h);
        } finally {
            eventSvc.removeHandler(EVT_INTERESTING, h);
        }
    }

    @Test(expected = HandlerUnregistrationException.class)
    public void removeUnknownEvent() throws Exception {
        final TestHandler h = new TestHandler();
        eventSvc.removeHandler(EVT_INTERESTING, h);
    }

    @Test
    public void isEventFiltered() throws HandlerRegistrationException, FireEventException, HandlerUnregistrationException {
        // Register handler on a given event type.
        final TestHandler h = new TestHandler();
        eventSvc.addHandler(EVT_INTERESTING, h);

        // Fire 2 different events
        final SEvent interesting = eventSvc.getEventBuilder().createNewInstance(EVT_INTERESTING).done();
        final SEvent irrelevant = eventSvc.getEventBuilder().createNewInstance(EVT_IRRELEVANT).done();
        eventSvc.fireEvent(interesting);
        eventSvc.fireEvent(irrelevant);

        // Check that only "interesting" events have been received by the registered handler
        assertNotNull(h.getReceivedEvents());
        assertFalse(h.getReceivedEvents().isEmpty());
        for (final SEvent evt : h.getReceivedEvents()) {
            if (!evt.getType().equals(EVT_INTERESTING)) {
                fail("Wrong event received by the handler");
            }
        }

        eventSvc.removeHandler(EVT_INTERESTING, h);
    }

    @Test
    public void isEventReceivedByHandler() throws FireEventException, HandlerRegistrationException, HandlerUnregistrationException {
        final TestHandler h = new TestHandler();
        final SEvent interesting = eventSvc.getEventBuilder().createNewInstance(EVT_INTERESTING).done();

        eventSvc.addHandler(EVT_INTERESTING, h);
        eventSvc.fireEvent(interesting);

        assertEquals(1, h.getReceivedEvents().size());
        eventSvc.removeHandler(EVT_INTERESTING, h);
    }

    @Test
    public void getAllHandlersByEvent() throws HandlerRegistrationException, HandlerUnregistrationException {
        // add 2 different handlers for 1 event type
        final TestHandler h1 = new TestHandler();
        final TestHandler h2 = new TestHandler();

        eventSvc.addHandler(EVT_INTERESTING, h1);
        eventSvc.addHandler(EVT_INTERESTING, h2);

        final Set<SHandler<SEvent>> evtList = eventSvc.getHandlers(EVT_INTERESTING);
        // now i check if the evtList contains my both handlers
        assertTrue(evtList.contains(h1) && evtList.contains(h2));

        eventSvc.removeHandler(EVT_INTERESTING, h1);
        eventSvc.removeHandler(EVT_INTERESTING, h2);
    }

}
