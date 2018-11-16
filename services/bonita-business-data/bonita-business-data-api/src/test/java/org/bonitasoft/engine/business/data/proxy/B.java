package org.bonitasoft.engine.business.data.proxy;

import org.bonitasoft.engine.bdm.Entity;

public class B implements Entity {

    private Long persistenceId;
    private Long persistenceVersion;
    private A a;

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }
}
