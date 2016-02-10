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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilder;
import org.bonitasoft.engine.identity.model.impl.SGroupImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class SGroupBuilderImpl implements SGroupBuilder {

    private final SGroupImpl group;

    public SGroupBuilderImpl(final SGroupImpl group) {
        super();
        this.group = group;
    }

    @Override
    public SGroupBuilder setName(final String name) {
        group.setName(name);
        return this;
    }

    @Override
    public SGroupBuilder setDisplayName(final String displayName) {
        group.setDisplayName(displayName);
        return this;
    }

    @Override
    public SGroupBuilder setDescription(final String description) {
        group.setDescription(description);
        return this;
    }

    @Override
    public SGroupBuilder setParentPath(final String parentPath) {
        group.setParentPath(parentPath);
        return this;
    }

    @Override
    public SGroup done() {
        return group;
    }

    @Override
    public SGroupBuilder setId(final long id) {
        group.setId(id);
        return this;
    }

    @Override
    public SGroupBuilder setIconName(final String iconName) {
        group.setIconName(iconName);
        return this;
    }

    @Override
    public SGroupBuilder setIconPath(final String iconPath) {
        group.setIconPath(iconPath);
        return this;
    }

    @Override
    public SGroupBuilder setCreatedBy(final long createdBy) {
        group.setCreatedBy(createdBy);
        return this;
    }

    @Override
    public SGroupBuilder setCreationDate(final long creationDate) {
        group.setCreationDate(creationDate);
        return this;
    }

    @Override
    public SGroupBuilder setLastUpdate(final long lastUpdate) {
        group.setLastUpdate(lastUpdate);
        return this;
    }

}
