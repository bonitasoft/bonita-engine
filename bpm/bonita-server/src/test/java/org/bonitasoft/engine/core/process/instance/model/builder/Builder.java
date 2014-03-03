package org.bonitasoft.engine.core.process.instance.model.builder;

import java.util.Random;

import org.bonitasoft.engine.persistence.PersistentObject;


public abstract class Builder<T extends PersistentObject> {
    static final long DEFAULT_TENANT_ID = 1L;
    
    protected long id = new Random().nextLong();
    
    public T build() {
        T t = _build();
        t.setId(id);
        t.setTenantId(DEFAULT_TENANT_ID);
        return t;
    }
    
    abstract T _build();
    
}
