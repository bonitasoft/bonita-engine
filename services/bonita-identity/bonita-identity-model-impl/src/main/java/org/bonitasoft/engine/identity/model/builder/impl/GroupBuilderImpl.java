/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.identity.model.builder.GroupBuilder;
import org.bonitasoft.engine.identity.model.impl.SGroupImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class GroupBuilderImpl implements GroupBuilder {

    private static final String PARENT_PATH = "parentPath";

    private SGroupImpl group;

    static final String ID = "id";

    static final String NAME = "name";

    static final String DESCRIPTION = "description";

    static final String DISPLAY_NAME = "displayName";

    static final String ICON_NAME = "iconName";

    static final String ICON_PATH = "iconPath";

    static final String CREATED_BY = "createdBy";

    static final String CREATION_DATE = "creationDate";

    static final String LAST_UPDATE = "lastUpdate";

    @Override
    public GroupBuilder setName(final String name) {
        group.setName(name);
        return this;
    }

    @Override
    public GroupBuilder setDisplayName(final String displayName) {
        group.setDisplayName(displayName);
        return this;
    }

    @Override
    public GroupBuilder setDescription(final String description) {
        group.setDescription(description);
        return this;
    }

    @Override
    public GroupBuilder createNewInstance() {
        group = new SGroupImpl();
        return this;
    }

    @Override
    public GroupBuilder setParentPath(final String parentPath) {
        group.setParentPath(parentPath);
        return this;
    }

    @Override
    public SGroup done() {
        return group;
    }

    @Override
    public GroupBuilder setId(final long id) {
        group.setId(id);
        return this;
    }

    @Override
    public GroupBuilder setIconName(final String iconName) {
        group.setIconName(iconName);
        return this;
    }

    @Override
    public GroupBuilder setIconPath(final String iconPath) {
        group.setIconPath(iconPath);
        return this;
    }

    @Override
    public GroupBuilder setCreatedBy(final long createdBy) {
        group.setCreatedBy(createdBy);
        return this;
    }

    @Override
    public GroupBuilder setCreationDate(final long creationDate) {
        group.setCreationDate(creationDate);
        return this;
    }

    @Override
    public GroupBuilder setLastUpdate(final long lastUpdate) {
        group.setLastUpdate(lastUpdate);
        return this;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getNameKey() {
        return NAME;
    }

    @Override
    public String getDisplayNameKey() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION;
    }

    @Override
    public String getIconNameKey() {
        return ICON_NAME;
    }

    @Override
    public String getIconPathKey() {
        return ICON_PATH;
    }

    @Override
    public String getCreatedByKey() {
        return CREATED_BY;
    }

    @Override
    public String getCreationDateKey() {
        return CREATION_DATE;
    }

    @Override
    public String getLastUpdateKey() {
        return LAST_UPDATE;
    }

    @Override
    public String getParentPathKey() {
        return PARENT_PATH;
    }

}
