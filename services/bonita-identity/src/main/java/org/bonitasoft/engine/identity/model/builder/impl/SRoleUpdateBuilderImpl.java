/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
 **/
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.builder.SRoleUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class SRoleUpdateBuilderImpl implements SRoleUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SRoleUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        super();
        this.descriptor = descriptor;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SRoleUpdateBuilder updateName(final String name) {
        descriptor.addField(SRole.NAME, name);
        return this;
    }

    @Override
    public SRoleUpdateBuilder updateDisplayName(final String displayName) {
        descriptor.addField(SRole.DISPLAY_NAME, displayName);
        return this;
    }

    @Override
    public SRoleUpdateBuilder updateDescription(final String description) {
        descriptor.addField(SRole.DESCRIPTION, description);
        return this;
    }

    @Override
    public SRoleUpdateBuilder updateCreatedBy(final long createdBy) {
        descriptor.addField(SRole.CREATED_BY, createdBy);
        return this;
    }

    @Override
    public SRoleUpdateBuilder updateCreationDate(final long creationDate) {
        descriptor.addField(SRole.CREATION_DATE, creationDate);
        return this;
    }

    @Override
    public SRoleUpdateBuilder updateLastUpdate(final long lastUpdate) {
        descriptor.addField(SRole.LAST_UPDATE, lastUpdate);
        return this;
    }
}
