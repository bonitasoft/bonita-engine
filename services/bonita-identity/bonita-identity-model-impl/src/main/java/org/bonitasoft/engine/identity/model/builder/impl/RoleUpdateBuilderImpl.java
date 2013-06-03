/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import org.bonitasoft.engine.identity.model.builder.RoleUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class RoleUpdateBuilderImpl implements RoleUpdateBuilder {

    private final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public RoleUpdateBuilder updateName(final String name) {
        descriptor.addField(RoleBuilderImpl.NAME, name);
        return this;
    }

    @Override
    public RoleUpdateBuilder updateDisplayName(final String displayName) {
        descriptor.addField(RoleBuilderImpl.DISPLAY_NAME, displayName);
        return this;
    }

    @Override
    public RoleUpdateBuilder updateDescription(final String description) {
        descriptor.addField(RoleBuilderImpl.DESCRIPTION, description);
        return this;
    }

    @Override
    public RoleUpdateBuilder updateIconName(final String iconName) {
        descriptor.addField(RoleBuilderImpl.ICON_NAME, iconName);
        return this;
    }

    @Override
    public RoleUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(RoleBuilderImpl.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public RoleUpdateBuilder updateCreatedBy(final long createdBy) {
        descriptor.addField(RoleBuilderImpl.CREATED_BY, createdBy);
        return this;
    }

    @Override
    public RoleUpdateBuilder updateCreationDate(final long creationDate) {
        descriptor.addField(RoleBuilderImpl.CREATION_DATE, creationDate);
        return this;
    }

    @Override
    public RoleUpdateBuilder updateLastUpdate(final long lastUpdate) {
        descriptor.addField(RoleBuilderImpl.LAST_UPDATE, lastUpdate);
        return this;
    }

}
