package org.bonitasoft.engine.events;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;

/**
 * @author Christophe Havard
 */
public class TestHandler implements SHandler<SEvent> {

    private static final long serialVersionUID = 1L;

    private boolean isCalled = false;

    private List<SEvent> receivedEvent;

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

}
