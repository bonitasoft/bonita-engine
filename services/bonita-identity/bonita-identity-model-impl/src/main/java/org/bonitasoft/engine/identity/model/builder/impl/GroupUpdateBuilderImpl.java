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

import org.bonitasoft.engine.identity.model.builder.GroupUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class GroupUpdateBuilderImpl implements GroupUpdateBuilder {

    private static final String PARENT_PATH = "parentPath";

    private final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public GroupUpdateBuilder updateName(final String name) {
        descriptor.addField(GroupBuilderImpl.NAME, name);
        return this;
    }

    @Override
    public GroupUpdateBuilder updateDisplayName(final String displayName) {
        descriptor.addField(GroupBuilderImpl.DISPLAY_NAME, displayName);
        return this;
    }

    @Override
    public GroupUpdateBuilder updateDescription(final String description) {
        descriptor.addField(GroupBuilderImpl.DESCRIPTION, description);
        return this;
    }

    @Override
    public GroupUpdateBuilder updateParentPath(final String parentPath) {
        descriptor.addField(PARENT_PATH, parentPath);
        return this;
    }

    @Override
    public GroupUpdateBuilder updateIconName(final String iconName) {
        descriptor.addField(GroupBuilderImpl.ICON_NAME, iconName);
        return this;
    }

    @Override
    public GroupUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(GroupBuilderImpl.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public GroupUpdateBuilder updateCreatedBy(final long createdBy) {
        descriptor.addField(GroupBuilderImpl.CREATED_BY, createdBy);
        return this;
    }

    @Override
    public GroupUpdateBuilder updateCreationDate(final long creationDate) {
        descriptor.addField(GroupBuilderImpl.CREATION_DATE, creationDate);
        return this;
    }

    @Override
    public GroupUpdateBuilder updateLastUpdate(final long lastUpdate) {
        descriptor.addField(GroupBuilderImpl.LAST_UPDATE, lastUpdate);
        return this;
    }

}
