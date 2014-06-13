package com.bonitasoft.engine.bdm.proxy;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;

public class TestEntity implements Entity {

    private TestEntity entity;

    @LazyLoaded
    public TestEntity getNotLoadedEntity() {
        return null;
    }

    @LazyLoaded
    public TestEntity getAlreadyLoadedEntity() {
        return new TestEntity();
    }

    public TestEntity getEagerEntity() {
        return null;
    }

    public void setEntity(TestEntity entity) {
        this.entity = entity;
    }

    @LazyLoaded
    public TestEntity getEntity() {
        return entity;
    }

    @Override
    public Long getPersistenceId() {
        return 0L;
    }

    @Override
    public Long getPersistenceVersion() {
        return 0L;
    }

}