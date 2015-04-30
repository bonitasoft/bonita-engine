package com.bonitasoft.engine.business.data.proxy;

import com.bonitasoft.engine.bdm.Entity;


public class Address implements Entity {

    private static final long serialVersionUID = 326174219656126594L;

    @Override
    public Long getPersistenceId() {
        return 0L;
    }

    @Override
    public Long getPersistenceVersion() {
        return 1L;
    }

}
