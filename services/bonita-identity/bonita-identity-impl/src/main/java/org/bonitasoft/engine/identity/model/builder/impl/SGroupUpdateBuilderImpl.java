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

import org.bonitasoft.engine.identity.model.builder.SGroupUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SGroupUpdateBuilderImpl implements SGroupUpdateBuilder {

    private static final String PARENT_PATH = "parentPath";

    private final EntityUpdateDescriptor descriptor;

    public SGroupUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        super();
        this.descriptor = descriptor;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SGroupUpdateBuilder updateName(final String name) {
        descriptor.addField(SGroupBuilderFactoryImpl.NAME, name);
        return this;
    }

    @Override
    public SGroupUpdateBuilder updateDisplayName(final String displayName) {
        descriptor.addField(SGroupBuilderFactoryImpl.DISPLAY_NAME, displayName);
        return this;
    }

    @Override
    public SGroupUpdateBuilder updateDescription(final String description) {
        descriptor.addField(SGroupBuilderFactoryImpl.DESCRIPTION, description);
        return this;
    }

    @Override
    public SGroupUpdateBuilder updateParentPath(final String parentPath) {
        descriptor.addField(PARENT_PATH, parentPath);
        return this;
    }

    @Override
    public SGroupUpdateBuilder updateIconName(final String iconName) {
        descriptor.addField(SGroupBuilderFactoryImpl.ICON_NAME, iconName);
        return this;
    }

    @Override
    public SGroupUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(SGroupBuilderFactoryImpl.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public SGroupUpdateBuilder updateCreatedBy(final long createdBy) {
        descriptor.addField(SGroupBuilderFactoryImpl.CREATED_BY, createdBy);
        return this;
    }

    @Override
    public SGroupUpdateBuilder updateCreationDate(final long creationDate) {
        descriptor.addField(SGroupBuilderFactoryImpl.CREATION_DATE, creationDate);
        return this;
    }

    @Override
    public SGroupUpdateBuilder updateLastUpdate(final long lastUpdate) {
        descriptor.addField(SGroupBuilderFactoryImpl.LAST_UPDATE, lastUpdate);
        return this;
    }

}
