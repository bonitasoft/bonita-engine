package com.bonitasoft.engine.expression;

import com.bonitasoft.engine.bdm.Entity;

/**
 * Simple Business Data test class
 */
class SimpleBizData implements Entity {

    private static final long serialVersionUID = 1L;

    private final Long id;

    public SimpleBizData(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Long getPersistenceId() {
        return null;
    }

    @Override
    public Long getPersistenceVersion() {
        return null;
    }
}
