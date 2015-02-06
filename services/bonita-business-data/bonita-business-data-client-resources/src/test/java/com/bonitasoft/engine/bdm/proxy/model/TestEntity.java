/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entity == null) ? 0 : entity.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof TestEntity) {
            TestEntity other = (TestEntity) obj;
            if (entity == null) {
                if (other.entity != null)
                    return false;
            } else if (!entity.equals(other.entity))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }
        return false;
    }

}
