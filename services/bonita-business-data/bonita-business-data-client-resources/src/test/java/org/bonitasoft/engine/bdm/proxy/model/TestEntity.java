/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bdm.proxy.model;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.lazy.LazyLoaded;

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
