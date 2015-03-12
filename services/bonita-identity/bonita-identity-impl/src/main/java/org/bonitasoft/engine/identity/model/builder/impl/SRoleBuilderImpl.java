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

import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.builder.SRoleBuilder;
import org.bonitasoft.engine.identity.model.impl.SRoleImpl;

/**
 * @author Baptiste Mesta
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class SRoleBuilderImpl implements SRoleBuilder {

    private final SRoleImpl role;

    public SRoleBuilderImpl(final SRoleImpl role) {
        super();
        this.role = role;
    }

    @Override
    public SRoleBuilder setName(final String name) {
        role.setName(name);
        return this;
    }

    @Override
    public SRoleBuilder setDisplayName(final String displayName) {
        role.setDisplayName(displayName);
        return this;
    }

    @Override
    public SRoleBuilder setDescription(final String description) {
        role.setDescription(description);
        return this;
    }

    @Override
    public SRoleBuilder setIconName(final String iconName) {
        role.setIconName(iconName);
        return this;
    }

    @Override
    public SRoleBuilder setIconPath(final String iconPath) {
        role.setIconPath(iconPath);
        return this;
    }

    @Override
    public SRoleBuilder setCreatedBy(final long createdBy) {
        role.setCreatedBy(createdBy);
        return this;
    }

    @Override
    public SRoleBuilder setCreationDate(final long creationDate) {
        role.setCreationDate(creationDate);
        return this;
    }

    @Override
    public SRoleBuilder setLastUpdate(final long lastUpdate) {
        role.setLastUpdate(lastUpdate);
        return this;
    }

    @Override
    public SRoleBuilder setId(final long id) {
        role.setId(id);
        return this;
    }

    @Override
    public SRole done() {
        return role;
    }
}
