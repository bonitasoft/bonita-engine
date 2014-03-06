package org.bonitasoft.engine.events.impl;

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
