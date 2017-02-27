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

import org.bonitasoft.engine.identity.model.impl.SRoleImpl;

/**
 * @author Danila Mazour
 */
public class RoleBuilder extends PersistentObjectBuilder<SRoleImpl, RoleBuilder> {

    private String name;

    public static RoleBuilder aRole() {
        return new RoleBuilder();
    }

    @Override
    RoleBuilder getThisBuilder() {
        return this;
    }

    @Override
    SRoleImpl _build() {

        SRoleImpl role = new SRoleImpl();
        role.setName(this.name);
        role.setId(this.id);
        return role;
    }

    public RoleBuilder forRoleName(String name) {
        this.name = name;
        return this;
    }

    public RoleBuilder forRoleId(Long id) {
        this.id = id;
        return this;
    }
}
