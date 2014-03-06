package org.bonitasoft.engine.events.impl;

import org.bonitasoft.engine.events.model.SEvent;

public class TestEvent implements SEvent {

    private boolean flagged = false;
    private final String type;

    public TestEvent(final String type) {
        this.type = type;
    }

    public void flag() {
        this.flagged = true;
    }

    public boolean isFlagged() {
        return flagged;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Object getObject() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setObject(final Object ob) {
        // TODO Auto-generated method stub

    }

}
