package com.bonitasoft.engine.operation;

import com.bonitasoft.engine.bdm.Entity;

public class Address implements Entity {

    private static final long serialVersionUID = -7765232426654390190L;

    @Override
    public Long getPersistenceId() {
        return 45L;
    }

    @Override
    public Long getPersistenceVersion() {
        return 4687634L;
    }

}
