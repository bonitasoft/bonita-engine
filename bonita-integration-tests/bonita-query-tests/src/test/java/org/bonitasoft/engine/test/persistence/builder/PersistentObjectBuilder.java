package org.bonitasoft.engine.test.persistence.builder;

import java.util.Random;

import org.bonitasoft.engine.persistence.PersistentObject;

public abstract class PersistentObjectBuilder<T extends PersistentObject, B extends PersistentObjectBuilder<T, B>> {

    public static final long DEFAULT_TENANT_ID = 1L;

    protected long id = new Random().nextLong();

    protected T persistentObject;

    protected B thisBuilder;

    public PersistentObjectBuilder() {
        thisBuilder = getThisBuilder();
    }

    public T build() {
        persistentObject = _build();
        fill(persistentObject);
        return persistentObject;
    }

    protected T fill(T persistent) {
        persistent.setId(id);
        persistent.setTenantId(DEFAULT_TENANT_ID);
        return persistent;
    }

    abstract B getThisBuilder();
    abstract T _build();

}
