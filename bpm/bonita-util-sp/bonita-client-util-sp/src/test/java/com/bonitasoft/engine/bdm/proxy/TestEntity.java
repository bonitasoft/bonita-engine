package com.bonitasoft.engine.bdm.proxy;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;

@SuppressWarnings("serial")
public class TestEntity implements Entity {

    private TestEntity entity;
    private String name;

    public TestEntity getEagerEntity() {
        return new TestEntity();
    }

    public void setLazyEntity(TestEntity entity) {
        this.entity = entity;
    }

    @LazyLoaded
    public TestEntity getLazyEntity() {
        return entity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
