package org.bonitasoft.engine.business.data.proxy;

import org.bonitasoft.engine.bdm.Entity;

public class A implements Entity {

    private Long persistenceId;
    private Long persistenceVersion;
    private B b;
    private A a;


    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }
}
