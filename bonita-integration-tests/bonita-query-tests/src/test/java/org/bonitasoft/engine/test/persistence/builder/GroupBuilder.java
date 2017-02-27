/*
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */

package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.identity.model.impl.SGroupImpl;

/**
 * @author Danila Mazour
 */
public class GroupBuilder extends PersistentObjectBuilder<SGroupImpl, GroupBuilder> {

    private String name;

    private String parentPath;

    public static GroupBuilder aGroup() {
        return new GroupBuilder();
    }

    @Override
    GroupBuilder getThisBuilder() {
        return this;
    }

    @Override
    SGroupImpl _build() {
        SGroupImpl group = new SGroupImpl();
        group.setId(this.id);
        group.setName(this.name);
        group.setParentPath(parentPath);
        return group;
    }

    public GroupBuilder forParentPath(String path) {
        this.parentPath = path;
        return this;
    }

    public GroupBuilder forGroupName(String name) {
        this.name = name;
        return this;
    }

    public GroupBuilder forGroupId(Long id) {
        this.id = id;
        return this;
    }

}
