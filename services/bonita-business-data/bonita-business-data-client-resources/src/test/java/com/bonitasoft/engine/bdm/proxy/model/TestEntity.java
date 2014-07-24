package com.bonitasoft.engine.bdm.proxy.model;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;

@SuppressWarnings("serial")
public class TestEntity implements Entity {

    private TestEntity entity;
    private String name;

    public TestEntity getEagerEntity() {
        return new TestEntity();
    }

    public void setLazyEntity(final TestEntity entity) {
        this.entity = entity;
    }

    @LazyLoaded
    public TestEntity getLazyEntity() {
        return entity;
    }

    @LazyLoaded
    public List<TestEntity> getLazyEntityList() {
        return Collections.emptyList();
    }

    public List<TestEntity> getEagerEntities() {
        return asList(new TestEntity(), new TestEntity());
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<String> getStrings() {
        return asList("aString", "anotherString");
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
